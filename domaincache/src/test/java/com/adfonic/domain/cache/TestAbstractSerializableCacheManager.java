package com.adfonic.domain.cache;

import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

public class TestAbstractSerializableCacheManager {
    private static final class TestCacheImpl implements SerializableCache {
        public void logCounts(String description, Logger logger, Level level) {
            logger.log(level, "Yo");
        }
    }

    private static final class TestCacheManager extends AbstractSerializableCacheManager {
        private TestCacheManager(File rootDir, String label, boolean useMemory) {
            super(TestCacheImpl.class, rootDir, label, useMemory);
        }
    }

    @Test
    public void testGenerateBatchId() {
        String label = "label";
        Date dateGenerated = new Date();
        String result = AbstractSerializableCacheManager.generateBatchId(TestCacheImpl.class, label, dateGenerated);
        assertEquals("TestAbstractSerializableCacheManager.TestCache-" + label + "-" + AbstractSerializableCacheManager.TIMESTAMP_FORMAT.format(dateGenerated), result);
    }

    @Test
    public void testGetShortCacheClassName() {
        String result = AbstractSerializableCacheManager.getShortCacheClassName(TestCacheImpl.class);
        assertEquals("TestAbstractSerializableCacheManager.TestCache", result);
    }

    @Test
    public void testConstructor() {
        new TestCacheManager(new File("."), "label", false);
    }

    @Test(expected = java.io.FileNotFoundException.class)
    public void testInitialize_rootDir_doesNotExist() throws java.io.IOException, ClassNotFoundException {
        new TestCacheManager(new File("/total/gibberish/non/existing"), "label", false).initialize();
    }

    @Test(expected = java.io.FileNotFoundException.class)
    public void testInitialize_rootDir_exists_no_cache() throws java.io.IOException, ClassNotFoundException {
        new TestCacheManager(new File("nonExistingfile"), "label", false).initialize();
    }

    @Test
    public void testInitialize_rootDir_exists_cache_exists() throws java.io.IOException, ClassNotFoundException {
        TestCacheImpl cache = new TestCacheImpl();
        String label = "label";
        Date dateGenerated = new Date();
        String batchId = AbstractSerializableCacheManager.generateBatchId(TestCacheImpl.class, label, dateGenerated);
        File datFile = new File(batchId + ".dat");
        try {
            FileOutputStream fos = new FileOutputStream(datFile);
            new ObjectOutputStream(fos).writeObject(cache);
            fos.close();

            new TestCacheManager(new File("."), label, false).initialize();
        } finally {
            datFile.delete();
        }
    }

    @Test
    public void testInitialize_rootDir_exists_multiple_caches_exists() throws java.io.IOException, ClassNotFoundException, InterruptedException {
        TestCacheImpl cache = new TestCacheImpl();
        String label = "label";
        Set<File> filesToDelete = new HashSet<File>();
        try {
            for (int k = 0; k < 3; ++k) {
                if (k > 0) {
                    Thread.sleep(2000);
                }
                Date dateGenerated = new Date();
                String batchId = AbstractSerializableCacheManager.generateBatchId(TestCacheImpl.class, label, dateGenerated);
                File datFile = new File(batchId + ".dat");
                FileOutputStream fos = new FileOutputStream(datFile);
                new ObjectOutputStream(fos).writeObject(cache);
                fos.close();
                filesToDelete.add(datFile);
            }

            new TestCacheManager(new File("."), label, false).initialize();
        } finally {
            for (File fileToDelete : filesToDelete) {
                fileToDelete.delete();
            }
        }
    }

    @Test
    public void testGetTimeSinceLastReload() throws java.io.IOException, ClassNotFoundException {
        TestCacheImpl cache = new TestCacheImpl();
        String label = "label";
        Date dateGenerated = new Date();
        String batchId = AbstractSerializableCacheManager.generateBatchId(TestCacheImpl.class, label, dateGenerated);
        File datFile = new File(batchId + ".dat");
        try {
            FileOutputStream fos = new FileOutputStream(datFile);
            new ObjectOutputStream(fos).writeObject(cache);
            fos.close();

            TestCacheManager mgr = new TestCacheManager(new File("."), label, false);
            mgr.initialize();
            mgr.getTimeSinceLastReload();
        } finally {
            datFile.delete();
        }
    }

    @Test
    public void testReloadCache_not_exists() throws java.io.IOException, ClassNotFoundException {
        TestCacheImpl cache = new TestCacheImpl();
        String label = "label";
        Date dateGenerated = new Date();
        String batchId = AbstractSerializableCacheManager.generateBatchId(TestCacheImpl.class, label, dateGenerated);
        File datFile = new File(batchId + ".dat");
        try {
            FileOutputStream fos = new FileOutputStream(datFile);
            new ObjectOutputStream(fos).writeObject(cache);
            fos.close();

            TestCacheManager mgr = new TestCacheManager(new File("."), label, false);
            mgr.initialize();
            mgr.reloadCache("totally invalid batch id");
        } finally {
            datFile.delete();
        }
    }

    @Test
    public void testReloadCache_exists() throws java.io.IOException, ClassNotFoundException {
        TestCacheImpl cache = new TestCacheImpl();
        String label = "label";
        Date dateGenerated = new Date();
        String batchId = AbstractSerializableCacheManager.generateBatchId(TestCacheImpl.class, label, dateGenerated);
        File datFile = new File(batchId + ".dat");
        try {
            FileOutputStream fos = new FileOutputStream(datFile);
            new ObjectOutputStream(fos).writeObject(cache);
            fos.close();

            TestCacheManager mgr = new TestCacheManager(new File("."), label, false);
            mgr.initialize();
            mgr.reloadCache(batchId);
        } finally {
            datFile.delete();
        }
    }

    @Test
    public void testGetCache() throws java.io.IOException, ClassNotFoundException {
        TestCacheImpl cache = new TestCacheImpl();
        String label = "label";
        Date dateGenerated = new Date();
        String batchId = AbstractSerializableCacheManager.generateBatchId(TestCacheImpl.class, label, dateGenerated);
        File datFile = new File(batchId + ".dat");
        try {
            FileOutputStream fos = new FileOutputStream(datFile);
            new ObjectOutputStream(fos).writeObject(cache);
            fos.close();

            TestCacheManager mgr = new TestCacheManager(new File("."), label, false);
            mgr.initialize();
            assertNotNull(mgr.getCache());
        } finally {
            datFile.delete();
        }
    }

    @Test
    public void testAutoDeleteOldCaches() throws java.io.IOException, ClassNotFoundException, InterruptedException {
        TestCacheImpl cache = new TestCacheImpl();
        String label = "label";
        Set<File> oldFiles = new HashSet<File>();
        File datFile = null;
        try {
            int numTotal = 3;
            for (int k = 0; k < numTotal; ++k) {
                if (k > 0) {
                    Thread.sleep(2000);
                }
                Date dateGenerated = new Date();
                String batchId = AbstractSerializableCacheManager.generateBatchId(TestCacheImpl.class, label, dateGenerated);
                datFile = new File(batchId + ".dat");
                FileOutputStream fos = new FileOutputStream(datFile);
                new ObjectOutputStream(fos).writeObject(cache);
                fos.close();
                if (k < (numTotal - 1)) {
                    oldFiles.add(datFile);
                }
            }

            TestCacheManager mgr = new TestCacheManager(new File("."), label, false);
            mgr.initialize();
            mgr.autoDeleteOldCaches();
            for (File oldFile : oldFiles) {
                assertFalse(oldFile.exists());
            }
        } finally {
            if (datFile != null) {
                datFile.delete();
            }
            for (File fileToDelete : oldFiles) {
                fileToDelete.delete();
            }
        }
    }

    @Test
    public void testOnCacheReserialized_not_exists() throws java.io.IOException, ClassNotFoundException {
        TestCacheImpl cache = new TestCacheImpl();
        String label = "label";
        Date dateGenerated = new Date();
        String batchId = AbstractSerializableCacheManager.generateBatchId(TestCacheImpl.class, label, dateGenerated);
        File datFile = new File(batchId + ".dat");
        try {
            FileOutputStream fos = new FileOutputStream(datFile);
            new ObjectOutputStream(fos).writeObject(cache);
            fos.close();

            TestCacheManager mgr = new TestCacheManager(new File("."), label, false);
            mgr.initialize();
            mgr.onCacheReserialized("totally invalid batch id");
        } finally {
            datFile.delete();
        }
    }

    @Test
    public void testOnCacheReserialized_exists() throws java.io.IOException, ClassNotFoundException {
        TestCacheImpl cache = new TestCacheImpl();
        String label = "label";
        Date dateGenerated = new Date();
        String batchId = AbstractSerializableCacheManager.generateBatchId(TestCacheImpl.class, label, dateGenerated);
        File datFile = new File(batchId + ".dat");
        try {
            FileOutputStream fos = new FileOutputStream(datFile);
            new ObjectOutputStream(fos).writeObject(cache);
            fos.close();

            TestCacheManager mgr = new TestCacheManager(new File("."), label, false);
            mgr.initialize();
            mgr.onCacheReserialized(batchId);
        } finally {
            datFile.delete();
        }
    }
}