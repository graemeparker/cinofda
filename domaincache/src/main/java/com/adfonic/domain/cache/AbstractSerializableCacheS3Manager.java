package com.adfonic.domain.cache;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StreamCorruptedException;
import java.util.Date;
import java.util.concurrent.atomic.AtomicReference;
import java.util.zip.GZIPInputStream;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jmx.export.annotation.ManagedResource;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.internal.MD5DigestCalculatingInputStream;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;

/** Base class for singleton access to a SerializableCache */
@ManagedResource
public abstract class AbstractSerializableCacheS3Manager<T extends SerializableCache> extends AbstractSerializableCacheManager<T> {

    // We use AtomicReference to provide access to the cache
    // so that as it gets reloaded, the reloaded cache can be
    // swapped in and out in a thread-safe manner.
    private final AtomicReference<String> lastCacheTag = new AtomicReference<String>();

    protected AmazonS3Client s3Client;
    @Value("${adserver.s3.path:adserver/v1/}")
    protected String s3Path = "adserver/v1/";
    @Value("${adserver.s3.accessKey:AKIAJRQDOV4DMY47DKPQ}")
    protected String s3AccessKey = "AKIAJRQDOV4DMY47DKPQ";
    @Value("${adserver.s3.secretKey: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}")
    protected String s3SecretKey = "kldAIrcq+lUVbWqOMO6FDOoIbhTC3syuOAliqBq/";
    @Value("${adserver.s3.endpoint:https://s3.amazonaws.com}")
    protected String s3EndPoint = "https://s3.amazonaws.com";
    @Value("${adserver.s3.bucket:byydcache}")
    protected String s3Bucket = "byydcache";

    private UpdateThread updateThread;

    protected AbstractSerializableCacheS3Manager(Class<T> cacheClass, File rootDir, String label, boolean useMemory) {
        super(cacheClass, rootDir, label, useMemory);
    }

    @Override
    @PostConstruct
    public void initialize() throws java.io.IOException, java.lang.ClassNotFoundException {
        LOG.info("S3: Attempting to discover most recent cache for " + shortCacheClassName + " (" + label + ")");
        s3Client = new AmazonS3Client(new BasicAWSCredentials(s3AccessKey, s3SecretKey));
        if (!rootDir.exists()) {
            throw new FileNotFoundException(rootDir.getCanonicalPath());
        }
        // Pre-load the domain cache
        File mostRecent = discoverMostRecentCache();
        if (mostRecent != null) {
            reloadCache(mostRecent);
        } else {
            //Not throwing exceptionhere will allow AdServer to start but every request will explode
            throw new IllegalStateException("Unable to initialize cache from S3 bucket: " + s3Bucket + ", label: " + label);
        }

        updateThread = new UpdateThread();
        updateThread.setName(shortCacheClassName);
        updateThread.setDaemon(true);
        updateThread.start();
    }

    @PreDestroy
    public void shutdown() {
        updateThread.keepRunning = false;
        updateThread.interrupt();
    }

    @Override
    protected void reloadCache(String batchId) throws java.io.IOException, java.lang.ClassNotFoundException {
        // verify and download latest
        File cacheFile = new File(rootDir, batchId + ".dat");
        if (!cacheFile.exists()) {
            String s3KeyName = s3Path + batchId + ".dat.gz";

            cacheFile = downloadS3cache(s3KeyName, rootDir + "/" + batchId + ".dat");
            if (!cacheFile.exists()) {
            } else {
                LOG.severe("Cache file does not exist: " + cacheFile.getAbsolutePath() + ", not on S3 fallback: " + s3Bucket + ":" + s3KeyName);
                return;
            }
        }
        reloadCache(cacheFile);
    }

    private File downloadS3cache(String s3KeyName, String fileName) throws IOException, StreamCorruptedException {
        LOG.info("Downloading: " + s3KeyName + " to " + fileName);
        S3Object s3obj = s3Client.getObject(s3Bucket, s3KeyName);
        if (s3obj == null) {
            throw new IOException("S3Object null for bucket: " + s3Bucket + " key: " + s3KeyName);
        }
        ObjectMetadata s3Metadata = s3obj.getObjectMetadata();
        // To minimize chance of partialy downloaded (broken) cache file
        // stream into .tmp file and rename it to .dat when finished   
        File tmpFile = new File(fileName + ".tmp");
        File datFile = new File(fileName);
        InputStream input = null;
        OutputStream output = null;
        String clientMd5;
        try {
            output = new FileOutputStream(tmpFile);
            MD5DigestCalculatingInputStream md5stream = new MD5DigestCalculatingInputStream(s3obj.getObjectContent());
            input = new GZIPInputStream(md5stream);
            IOUtils.copyLarge(input, output);
            clientMd5 = Hex.encodeHexString(md5stream.getMd5Digest());
        } finally {
            IOUtils.closeQuietly(input);
            IOUtils.closeQuietly(output);
        }
        tmpFile.renameTo(datFile);
        lastCacheTag.set(s3obj.getKey());
        String serverMd5 = s3Metadata.getETag();
        LOG.info("Downloaded: " + s3KeyName + " (size: " + s3Metadata.getContentLength() + ", md5: " + serverMd5 + ") to " + datFile.getAbsolutePath() + " (size: "
                + datFile.length() + ", md5: " + clientMd5 + ")");
        if (!clientMd5.equals(serverMd5)) {
            throw new StreamCorruptedException("MD5 does not match for: " + s3KeyName + ", server: " + serverMd5 + ", client: " + clientMd5);
        }
        return datFile;
    }

    /**
     * Discover the most recent cache by scanning for cache files in the
     * rootDir. This method gets used only at context startup. After that,
     * freshly updated cache batchIds are explicitly passed to us via messages
     * on a JMS topic.
     * 
     * @return the most recent cache file
     * @throws Exception
     *             if the most recent cache file can't be determined
     */
    @Override
    protected File discoverMostRecentCache() throws FileNotFoundException {

        // Not to be used on production adserver, but handy on local dev 
        // because takes few minutes to download adserver cache (on adserver start)
        if ("true".equals(System.getProperty("cache.prefer.local"))) {
            try {
                File file = super.discoverMostRecentCache();
                LOG.info("S3: Local cache file preferred: " + file + " for label " + label);
                return file;
            } catch (FileNotFoundException fnf) {
                LOG.info("Local cache not found for label " + label);
            }
        }
        return discoverS3cache();
    }

    /**
     * May return null
     */
    private File discoverS3cache() {
        String s3prefix = s3Path + label + "/";
        S3ObjectSummary latestS3o = null;
        Date latestS3Date = null;
        ObjectListing list = s3Client.listObjects(s3Bucket, s3prefix);
        for (S3ObjectSummary s3o : list.getObjectSummaries()) {
            String[] path = s3o.getKey().split("/");
            String name = path[path.length - 1];

            if (name.startsWith(shortCacheClassName) && !s3o.getKey().contains("/old/")) {
                if (latestS3Date == null || latestS3Date.getTime() < s3o.getLastModified().getTime()) {
                    latestS3Date = s3o.getLastModified();
                    latestS3o = s3o;
                }
            }
        }

        if (latestS3o == null) {
            LOG.severe("No cache found in S3 bucket: " + s3Bucket + ", prefix: " + s3prefix);
        } else if (lastCacheTag.get() == null || !lastCacheTag.get().equals(latestS3o.getKey())) {
            String[] path = latestS3o.getKey().split("/");
            String name = path[path.length - 1];
            // Sometimes we have java.io.OptionalDataException thrown from deserialization and theory is that file might be corupted while S3 download
            // Also sometimes S3 download fails on java.net.SocketException: Connection reset
            int itry = 0;
            while (++itry <= 3) {
                try {
                    return downloadS3cache(latestS3o.getKey(), rootDir + "/" + name.replaceAll("\\.dat\\.gz", "\\.dat"));
                } catch (IOException iox) {
                    LOG.warning("Try #" + itry + " failed to download file: " + iox.getMessage());
                }
            }
        }
        return null;
    }

    //  was unstable as execution threads did not perform scheduled operations over longer periode of time
    //  @Scheduled(fixedDelay = 10000)
    @Override
    public void verifyUpdate() {
        this.lastCheckAt = new Date();
        LOG.fine("Discovering cache: " + shortCacheClassName + "/" + label);
        try {
            File latestCacheFile = discoverMostRecentCache();
            if (latestCacheFile != null && (loadedCacheFile == null || !latestCacheFile.getName().equals(loadedCacheFile.getName()))) {
                reloadCache(latestCacheFile);
            }
        } catch (Throwable t) {
            LOG.warning("Unable to discover cache: " + t);
        }
    }

    public class UpdateThread extends Thread {

        private boolean keepRunning = true;

        @Override
        public void run() {
            LOG.info("Cache UpdateThread started: " + label);
            while (keepRunning) {
                try {
                    verifyUpdate();
                } catch (Throwable t) {
                    LOG.warning("Unable to update cache: " + t);
                }

                try {
                    Thread.sleep(10000);
                } catch (InterruptedException ie) {
                }
            }
            LOG.info("Cache UpdateThread exited: " + label);
        }
    }
}
