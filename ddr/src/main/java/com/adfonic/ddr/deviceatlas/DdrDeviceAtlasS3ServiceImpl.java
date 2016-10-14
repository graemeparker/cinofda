package com.adfonic.ddr.deviceatlas;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

import mobi.mtld.da.Api;
import mobi.mtld.da.exception.JsonException;

import com.adfonic.ddr.AbstractDdrService;
import com.adfonic.ddr.amazon.AmazonS3Service;

@SuppressWarnings("rawtypes")
public class DdrDeviceAtlasS3ServiceImpl extends AbstractDdrService {
    private static final transient Logger LOG = Logger.getLogger(DdrDeviceAtlasS3ServiceImpl.class.getName());

    private static final int NUMBER_OF_TRIES = 3;
    private static final long WAIT_TIMEOUT = 1000;

    protected AmazonS3Service amazonS3Service;

    protected final String s3Bucket;
    protected final String s3Key;
    protected final Boolean s3Compressed;

    protected static AtomicReference<Map> treeRef = new AtomicReference<Map>();
    protected static AtomicReference<Date> dateRef = new AtomicReference<Date>();

    public DdrDeviceAtlasS3ServiceImpl(AmazonS3Service amazonS3Service, String s3Bucket, String s3Key, Boolean s3Compressed) {
        this.amazonS3Service = amazonS3Service;
        this.s3Bucket = s3Bucket;
        this.s3Key = s3Key;
        this.s3Compressed = s3Compressed;
        loadData();
    }

    @Override
    @SuppressWarnings("unchecked")
    protected Map<String, String> doGetDdrProperties(String userAgent) {
        return Api.getProperties((HashMap) getTreeRef(), userAgent);
    }

    public final void loadData() {
        if (LOG.isLoggable(Level.INFO)) {
            LOG.fine("Checking new version of Device Atlas file.");
        }

        Date currentModDate = dateRef.get();
        Date lastModDate = amazonS3Service.getObjectModificationDate(s3Bucket, s3Key);

        File file = null;
        if ((currentModDate == null) || (lastModDate.compareTo(currentModDate) != 0)) {
            Map tree;
            try {
                if (LOG.isLoggable(Level.INFO)) {
                    LOG.info("Loading Device Atlas data from Amazon S3: bucket=" + s3Bucket + " key=" + s3Key);
                }

                file = amazonS3Service.downloadFile(s3Bucket, s3Key, s3Compressed);

                if (LOG.isLoggable(Level.INFO)) {
                    LOG.info("Parsing Device Atlas data from " + file.getPath());
                }
                tree = getTree(file);
            } catch (Exception e) {
                throw new IllegalStateException("Failed to load device atlas from S3 using key " + s3Key + " for bucket " + s3Bucket, e);
            } finally {
                deleteTempfile(file);
            }
            treeRef.set(tree);
            dateRef.set(lastModDate);

            if (LOG.isLoggable(Level.INFO)) {
                LOG.info("Tree revision " + getTreeRevision(tree) + ", generation date: " + getTreeGeneration(tree));
            }
        }
    }

    protected Map getTree(File file) throws JsonException, IOException {
        return Api.getTreeFromFile(file.getPath(), true, true);
    }

    protected int getTreeRevision(Map tree) {
        return Api.getTreeRevision((HashMap) tree);
    }

    protected String getTreeGeneration(Map tree) {
        return Api.getTreeGeneration((HashMap) tree);
    }

    public Map getTreeRef() {
        Map ret = treeRef.get();
        if (ret == null) {
            int nTries = 0;
            while (ret == null && nTries < NUMBER_OF_TRIES - 1) {
                try {
                    Thread.sleep(WAIT_TIMEOUT);
                } catch (InterruptedException ie) {
                    LOG.warning("Interrupted exception waiting to do another try to retrieve Device Atlas information.");
                }
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.fine("Accessing to Device Atlas information (try " + nTries + ")");
                }
                ret = treeRef.get();

                nTries++;
            }
            if (ret == null) {
                throw new IllegalStateException("Device Atlas information not loaded.");
            }
        }
        return ret;
    }

    private void deleteTempfile(File file) {
        if (file != null) {
            Path path = file.toPath();
            try {
                Files.deleteIfExists(path);
            } catch (Exception e) {
                LOG.warning("Can not delete temporary device atlas donwloaded file: " + path);
            }
        }
    }
}