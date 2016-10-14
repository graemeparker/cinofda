package com.adfonic.util;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

public class FileUpdateMonitorIT {
    private static final transient Logger LOG = Logger.getLogger(FileUpdateMonitorIT.class.getName());

    private static final String TEST_FILE = "FileUpdateMonitor.test";

    @Test
    public void test() throws java.io.IOException, java.lang.InterruptedException {
        final AtomicInteger updateCounter = new AtomicInteger(0);

        FileUpdateMonitor fileUpdateMonitor = null;
        File file = new File(TEST_FILE);
        try {
            LOG.info("Creating " + file.getCanonicalPath());
            updateFile(file);

            // Set up a FileUpdateMonitor that just counts how many times the
            // file was updated
            fileUpdateMonitor = new FileUpdateMonitor(file, 1, new Runnable() {
                @Override
                public void run() {
                    updateCounter.incrementAndGet();
                }
            });
            fileUpdateMonitor.start();

            // Update the file 5 times
            int updatesToDo = 5;
            for (int k = 0; k < updatesToDo; ++k) {
                Thread.sleep(2000);
                LOG.info("Updating " + file.getCanonicalPath());
                updateFile(file);
            }

            Thread.sleep(2000);

            assertEquals(updatesToDo, updateCounter.get());

        } finally {
            if (fileUpdateMonitor != null) {
                fileUpdateMonitor.stop();
            }

            // Clean up after ourselves...
            FileUtils.deleteQuietly(file);
        }
    }

    private static void updateFile(File file) throws java.io.IOException {
        FileOutputStream fos = new FileOutputStream(file);
        new PrintStream(fos).println(System.currentTimeMillis());
        fos.close();
    }
}
