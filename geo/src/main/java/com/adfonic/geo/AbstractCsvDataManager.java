package com.adfonic.geo;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import au.com.bytecode.opencsv.CSVReader;

import com.adfonic.util.FileUpdateMonitor;

/** Abstract base class for CSV-based data.  This class handles the management
    of the data file, watching for file updates, atomic access to the cache,
    CSV reading, etc.  Subclasses simply need to implement a method that takes
    a CSV line and maps it to an object that extends Coordinates.  This class
    provides "get nearest" functionality.
*/
public abstract class AbstractCsvDataManager<T> {
    private static final transient Logger LOG = Logger.getLogger(AbstractCsvDataManager.class.getName());

    private static final int TO_MILLISECS = 1000000;

    private final File dataFile;
    private final int checkForUpdatesPeriodSec;
    private FileUpdateMonitor fileUpdateMonitor;
    private final AtomicReference<Map<String, T>> cacheRef = new AtomicReference<Map<String, T>>();
    private final boolean forceLowerCaseLookups;

    protected AbstractCsvDataManager(File dataFile, int checkForUpdatesPeriodSec) {
        this(false, dataFile, checkForUpdatesPeriodSec);
    }

    protected AbstractCsvDataManager(boolean forceLowerCaseLookups, File dataFile, int checkForUpdatesPeriodSec) {
        this.forceLowerCaseLookups = forceLowerCaseLookups;
        this.dataFile = dataFile;
        this.checkForUpdatesPeriodSec = checkForUpdatesPeriodSec;
    }

    @PostConstruct
    public void initialize() throws java.io.IOException {
        reloadData();

        // Start up a file update monitor to watch for changes to the data file
        fileUpdateMonitor = new FileUpdateMonitor(dataFile, checkForUpdatesPeriodSec, new Runnable() {
            @Override
            public void run() {
                try {
                    reloadData();
                } catch (java.io.IOException e) {
                    LOG.log(Level.SEVERE, "Failed to reload data", e);
                }
            }
        });
        fileUpdateMonitor.start();
    }

    @PreDestroy
    public void destroy() {
        if (fileUpdateMonitor != null) {
            fileUpdateMonitor.stop();
        }
    }

    private void reloadData() throws java.io.IOException {
        LOG.info("Reloading data from " + dataFile.getAbsolutePath());
        FileReader fileReader = new FileReader(dataFile);
        CSVReader reader = null;
        try {
            long start = System.nanoTime();

            reader = new CSVReader(fileReader);
            String[] line;
            Map<String, T> map = new HashMap<String, T>();
            for (int k = 0; k < getNumberOfHeaderLinesToSkip(); ++k) {
                reader.readNext(); // skip the header line
            }
            while ((line = reader.readNext()) != null) {
                processCsvLine(line, map);
            }
            // Swap in the new cache
            cacheRef.set(map);

            LOG.info("Read " + map.size() + " objects in " + (System.nanoTime() - start) / TO_MILLISECS + " ms.");

            // Call the "after data reloaded" hook
            afterDataReloaded(map);

            LOG.info("Data ready in " + (System.nanoTime() - start) / TO_MILLISECS + " ms.");
        } finally {
            fileReader.close();
            closeCSVReader(reader);
        }
    }

    private void closeCSVReader(CSVReader reader) {
        if (reader != null) {
            try {
                reader.close();
            } catch (IOException ioe) {
                //do nothing
            }
        }
    }

    /**
     * Override this if you need to do anything after data is reloaded
     */
    protected void afterDataReloaded(Map<String, T> map) {
        // Nothing to do here, subclass can override
    }

    /** Return the number of CSV header lines to skip */
    protected int getNumberOfHeaderLinesToSkip() {
        return 0;
    }

    /** Process the CSV line and add a representative object to the map */
    protected abstract void processCsvLine(String[] line, Map<String, T> map);

    /** Provide access to the cache map for subclasses */
    protected Map<String, T> getCache() {
        return cacheRef.get();
    }

    /** Get a value by primary lookup key */
    public T get(String key) {
        if (forceLowerCaseLookups) {
            return cacheRef.get().get(key.toLowerCase());
        } else {
            return cacheRef.get().get(key);
        }
    }
}
