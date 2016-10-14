package com.adfonic.util;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class DaemonThreadFactory implements ThreadFactory {

    private static DaemonThreadFactory singleton = new DaemonThreadFactory();

    public static DaemonThreadFactory getInstance() {
        return singleton;
    }

    private final String namePrefix;
    private final AtomicInteger threadCounter = new AtomicInteger();

    public DaemonThreadFactory(String namePrefix) {
        this.namePrefix = namePrefix;

    }

    public DaemonThreadFactory() {
        // Default constructor for that silly sigleton above 
        this.namePrefix = null;
    }

    // Create threads as daemon threads
    @Override
    public Thread newThread(Runnable r) {
        Thread thread = new Thread(r);
        thread.setDaemon(true);
        if (namePrefix != null) {
            thread.setName(namePrefix + threadCounter.incrementAndGet());
        }
        return thread;
    }
}
