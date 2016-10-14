package com.adfonic.ddr.amazon;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;

public class AmazonS3Service {

    private static final transient Logger LOG = Logger.getLogger(AmazonS3Service.class.getName());

    private final AmazonS3Client s3Client;

    public AmazonS3Service(String s3AccessKey, String s3SecretKey) {
        if (StringUtils.isBlank(s3AccessKey) && StringUtils.isBlank(s3SecretKey)) {
            LOG.info("Connecting to AWS using Instance Profile Credentials");
            s3Client = new AmazonS3Client();
        } else {
            LOG.info("Connecting to AWS using access key and secret key");
            s3Client = new AmazonS3Client(new BasicAWSCredentials(s3AccessKey, s3SecretKey));
        }
    }

    public Date getObjectModificationDate(final String bucket, final String key) {
        Date modificationDate = null;

        try {
            LOG.finer("Retrieving modification date for key " + key + " and bucket " + bucket);
            ObjectMetadata metadata = s3Client.getObjectMetadata(bucket, key);
            modificationDate = metadata.getLastModified();
        } catch (AmazonS3Exception s3e) {
            throw new IllegalStateException("Can not retrieve modification date for object " + key + " in the bucket " + bucket, s3e);
        }

        return modificationDate;
    }

    public File downloadFile(final String bucket, final String key, final Boolean compressed) throws IOException {
        File file = File.createTempFile(key, null);
        file.deleteOnExit();
        return downloadFile(file.getPath(), bucket, key, compressed);
    }

    public File downloadFile(final String localPath, final String bucket, final String key, final Boolean compressed) throws IOException {
        FileOutputStream fos = null;
        try {
            LOG.finer("Retrieving modification date for key " + key + " and bucket " + bucket);
            S3Object obj = s3Client.getObject(bucket, key);
            fos = new FileOutputStream(localPath);
            InputStream inputStream = obj.getObjectContent();
            if (compressed) {
                inputStream = new GZIPInputStream(inputStream);
            }
            IOUtils.copyLarge(inputStream, fos);
        } catch (AmazonS3Exception s3e) {
            throw new IllegalStateException("Can not retrieve object with key " + key + " in the bucket " + bucket, s3e);
        } finally {
            if (fos != null) {
                fos.close();
            }
        }

        return new File(localPath);
    }
}
