package com.adfonic.olap;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.concurrent.atomic.AtomicReference;
import mondrian.olap.Connection;

public abstract class MondrianDataSource {
    private static final transient Logger LOGGER = Logger.getLogger(MondrianDataSource.class.getName());
    private static final AtomicReference<MondrianDataSource> SINGLETON_REF = new AtomicReference<MondrianDataSource>();

    protected MondrianDataSource() {
        // If we're the first instance to be created, set up the singleton
        if (SINGLETON_REF.compareAndSet(null, this)) {
            if (LOGGER.isLoggable(Level.INFO)) {
                LOGGER.info("Singleton initialized");
            }
        } else {
            LOGGER.warning("Singleton has already been set!  Behavior unpredictable for this instance");
        }
    }
    
    public static MondrianDataSource getInstance() {
        MondrianDataSource instance = SINGLETON_REF.get();
        if (instance == null) {
            LOGGER.warning("MondrianDataSource has not been initialized");
        }
        return instance;
    }

    public abstract Connection getConnection();
}
