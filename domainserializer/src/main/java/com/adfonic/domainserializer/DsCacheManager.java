package com.adfonic.domainserializer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.concurrent.atomic.AtomicReference;
import java.util.zip.GZIPOutputStream;

import javax.annotation.PostConstruct;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import com.adfonic.domain.cache.SerializableCache;
import com.adfonic.util.SerializationUtils;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;

public class DsCacheManager {

    private final Logger LOG = LoggerFactory.getLogger(getClass());

    private static final String CONTENT_SHA1 = "Content-SHA1";

    @Value("${domainserializer.s3.accessKey:AKIAJRQDOV4DMY47DKPQ}")
    private String s3AccessKey;
    @Value("${domainserializer.s3.secretKey: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}")
    private String s3SecretKey;
    @Value("${domainserializer.s3.endpoint:https://s3.amazonaws.com}")
    private String s3EndPoint;
    @Value("${domainserializer.s3.bucket:byydcache}")
    private String s3Bucket;
    @Value("${domainserializer.s3.path:adserver/v1/}")
    private String s3BasePath;
    @Value("${domainserializer.s3.maxRetries:3}")
    private int maxRetries;
    @Value("${domainserializer.s3.initDelay:1}")
    private int initDelay;

    private int maxOldCount = 1000;

    private ThreadLocal<AmazonS3Client> s3Client = new ThreadLocal<>();

    @Value("${DomainSerializer.cacheRootDir}")
    private File cacheRootDir;

    @PostConstruct
    public void init() {
        LOG.info("Initializing AWS S3 client. bucket: " + s3Bucket + ", path: " + s3BasePath);
        getS3Client();
    }

    private AmazonS3Client getS3Client() {
        if (s3Client.get() == null) {
            s3Client.set(new AmazonS3Client(new BasicAWSCredentials(s3AccessKey, s3SecretKey)));
            s3Client.get().setEndpoint(s3EndPoint);
        }
        return s3Client.get();
    }

    public String getS3Bucket() {
        return s3Bucket;
    }

    public String getS3BasePath() {
        return s3BasePath;
    }

    /**
     * Serialize the given cache batch to a file. This method will write the
     * cache to cacheRootDir/batchId.dat.
     * 
     * @param cache
     *            the cache to serialize
     * @param cacheBatchId
     *            the generated batch id
     * @return the File where the serialized cache data was written
     */
    public File distribute(SerializableCache cache, String cacheBatchId, String shard, AtomicReference<String> oldHash, CacheBuildStats stats) throws IOException {

        stats.setSerializationStartedAt(new Date());
        // Serialize cache into file
        File cacheFile = new File(cacheRootDir, cacheBatchId + ".dat");
        SerializationUtils.serialize(cache, cacheFile, true);

        if (DomainSerializerS3.skipS3upload) {
            // Just leave cache there...
            return cacheFile;
        }

        File gzipFile = gzipFile(cacheBatchId, cacheFile);

        FileInputStream fis = new FileInputStream(gzipFile);
        String sha1 = hashSHA1(fis);
        fis.close();

        if (false) { // sha1.equals(oldHash.get())) {
            LOG.info("Skipping cache dump without changes: " + cacheBatchId + " / " + sha1);
        } else {
            String basePath = uploadFile(gzipFile, cacheBatchId, shard, sha1, stats);
            oldHash.set(sha1);
            movePreviousCacheWithRetry(cacheBatchId, basePath);
        }

        cacheFile.delete();
        FileOutputStream fos = new FileOutputStream(new File(cacheRootDir, cacheBatchId + ".dat"));
        fos.write(new Date().toString().getBytes());
        fos.close();

        return cacheFile;
    }

    private void movePreviousCacheWithRetry(final String cacheBatchId, final String basePath) {
        LOG.info("S3 cleanup for new cache: " + cacheBatchId);
        Thread thread = new Thread() {
            @Override
            public void run() {
                int retry = maxRetries;
                int delay = initDelay;
                do {
                    try {
                        movePreviousCaches(cacheBatchId, basePath, delay);
                        break;
                    } catch (Throwable t) {
                        LOG.warn("Unable to backup old files on s3: " + t + " retrying: " + retry + " after " + delay);
                    }
                    retry--;
                    delay *= 2;
                } while (retry >= 0);
            };
        };
        thread.setDaemon(true);
        thread.setName("DsS3OldRemove");
        thread.start();
    }

    private void movePreviousCaches(final String cacheBatchId, String basePath, int delay) {
        try {
            ObjectListing listing = getS3Client().listObjects(s3Bucket, basePath);
            int oldCount = 0;
            if (listing != null && listing.getObjectSummaries().size() > 1) {
                for (S3ObjectSummary o : listing.getObjectSummaries()) {
                    if (o.getKey().contains("old") && !o.getKey().endsWith("old/") && !o.getKey().endsWith("old") && oldCount < maxOldCount) {
                        oldCount++;
                        continue;
                    }
                    if (o.getKey().contains(cacheBatchId)) {
                        continue;
                    }

                    S3Object old = getS3Client().getObject(s3Bucket, o.getKey());
                    ObjectMetadata m = new ObjectMetadata();
                    m.setContentLength(old.getObjectMetadata().getContentLength());
                    m.addUserMetadata(CONTENT_SHA1, old.getObjectMetadata().getUserMetaDataOf(CONTENT_SHA1));
                    // TODO: add expiration

                    String newPath = basePath + "old" + o.getKey().substring(o.getKey().lastIndexOf("/"));
                    LOG.info("S3 moving out " + o.getKey() + " -> " + newPath);
                    getS3Client().putObject(s3Bucket, newPath, old.getObjectContent(), m);
                    getS3Client().deleteObject(s3Bucket, old.getKey());
                }
            }
        } catch (Throwable t) {
            LOG.warn("Unable to move backup: " + t.getMessage(), t);
            getS3Client().shutdown();
            s3Client.set(null);
            try {
                Thread.sleep(delay * 1000);
            } catch (InterruptedException e) {
                LOG.warn("Unable to sleep: " + e.getMessage());
            }
        }

        String[] files = cacheRootDir.list(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                String type = name.split("-")[0];
                String batch = cacheBatchId.split("-")[0];
                return batch.equals(type) && !name.startsWith(cacheBatchId);
            }
        });

        for (String f : files) {
            //File c = new File(f);
            //LOG.info("Deleting: " + c);
        }
        getS3Client().shutdown();
        s3Client.set(null);
    }

    private String uploadFileSync(String batchId, String shard, File file, String sha1) throws FileNotFoundException, IOException {
        FileInputStream fis = null;
        int retry = maxRetries;
        int delay = initDelay;
        String basePath = s3BasePath + shard + "/";

        do {
            try {
                fis = new FileInputStream(file);
                ObjectMetadata meta = new ObjectMetadata();
                meta.setContentLength(file.length());
                meta.addUserMetadata(CONTENT_SHA1, sha1);
                LOG.info("Uploading " + file + " to s3 : " + basePath);
                getS3Client().putObject(s3Bucket, basePath + batchId + ".dat.gz", fis, meta);
                LOG.info("Finished");
                break;
            } catch (Throwable t) {
                LOG.warn("Unable to write to s3: " + t + " retrying: " + retry + " after " + delay, t);
                getS3Client().shutdown();
                s3Client.set(null);
                try {
                    Thread.sleep(delay * 1000);
                } catch (InterruptedException e) {
                    LOG.warn("Unable to sleep: " + e.getMessage());
                }
            }
            retry--;
            delay *= 2;
        } while (retry >= 0);
        if (fis != null) {
            fis.close();
        }
        file.delete();

        return basePath;
    }

    private String uploadFile(final File file, final String batchId, final String label, final String sha1, final CacheBuildStats stats) throws IOException {
        final String basePath = s3BasePath + label + "/";
        final String s3key = basePath + batchId + ".dat.gz";
        LOG.info("Uploading to S3 file: " + file.getAbsolutePath() + " to " + s3key);
        stats.setLabel(s3key);
        Thread thread = new Thread() {
            @Override
            public void run() {
                int retry = maxRetries;
                int delay = initDelay;
                do {
                    stats.setDistributionStartedAt(new Date());
                    try (FileInputStream fis = new FileInputStream(file)) {
                        ObjectMetadata meta = new ObjectMetadata();
                        meta.setContentLength(file.length());
                        meta.addUserMetadata(CONTENT_SHA1, sha1);
                        //LOG.info("Writing to s3 : " + basePath);
                        getS3Client().putObject(s3Bucket, s3key, fis, meta);
                        LOG.info("S3 upload finished: " + s3key);
                        break;
                    } catch (Exception x) {
                        LOG.warn("S3 upload failed: " + x + " retry: " + retry + " after " + delay, x);
                        stats.setException(x);
                        getS3Client().shutdown();
                        s3Client.set(null);
                        try {
                            Thread.sleep(delay * 1000);
                        } catch (InterruptedException e) {
                            LOG.warn("Interrupted from sleep: " + e.getMessage());
                        }
                    }
                    retry--;
                    delay *= 2;
                } while (retry >= 0);
                stats.setDistributionCompletedAt(new Date());
                file.delete();
                getS3Client().shutdown();
                s3Client.set(null);
            }
        };
        thread.setDaemon(true);
        thread.setName("DsS3NewUpload");
        thread.start();

        return basePath;
    }

    private File gzipFile(String batchId, File cacheFile) throws IOException {
        File gzipFile = new File(cacheRootDir.getAbsolutePath() + "/" + batchId + ".dat.gz");
        LOG.info("Gzipping " + batchId + " into " + gzipFile.getAbsolutePath());
        FileOutputStream fos = new FileOutputStream(gzipFile);
        GZIPOutputStream gz = new GZIPOutputStream(fos);
        FileInputStream fis = new FileInputStream(cacheFile);
        IOUtils.copyLarge(fis, gz);
        gz.finish();
        gz.close();
        fos.close();
        fis.close();
        return gzipFile;
    }

    private String hashSHA1(FileInputStream fis) throws IOException {
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException nsax) {
            throw new IllegalStateException("SHA-1 MessageDigest not exist", nsax);
        }
        byte[] arr = new byte[4096];
        int r;
        while ((r = fis.read(arr)) != -1) {
            md.update(arr, 0, r);
        }
        return new String(Hex.encodeHex(md.digest()));
    }

}
