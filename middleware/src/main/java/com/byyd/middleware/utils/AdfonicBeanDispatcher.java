package com.byyd.middleware.utils;

import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.context.support.ApplicationObjectSupport;

public class AdfonicBeanDispatcher extends ApplicationObjectSupport {

    private static final AtomicReference<AdfonicBeanDispatcher> SINGLETON = new AtomicReference<AdfonicBeanDispatcher>();
    private static final transient Logger LOG = Logger.getLogger(AdfonicBeanDispatcher.class.getName());

    public AdfonicBeanDispatcher() {
        LOG.info("***** Constructing a new AdfonicBeanDispatcher");
        if (SINGLETON.get() == null) {
            synchronized (SINGLETON) {
                if (SINGLETON.get() == null) {
                    if (LOG.isLoggable(Level.FINE)) {
                        LOG.fine("Setting SINGLETON");
                    }
                    SINGLETON.set(this);
                }
            }
        }
    }
    
    public static AdfonicBeanDispatcher getInstance() {
        return SINGLETON.get();
    }

    /** For unit testing */
    public static void setInstance(AdfonicBeanDispatcher instance) {
        SINGLETON.set(instance);
    }

    public static Object getBean(String name) {
        return getInstance().getApplicationContext().getBean(name);
    }

    public static <T> T getBean(Class<T> clazz) {
        return getInstance().getApplicationContext().getBean(clazz);
    }
}
