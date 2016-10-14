package com.adfonic.util;

import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.support.ApplicationObjectSupport;

/** Instantiate this bean if you need ad-hoc access to the ApplicationContext */
public class ApplicationContextUtils extends ApplicationObjectSupport {
    private static final Logger LOGGER = Logger.getLogger(ApplicationContextUtils.class.getName());
    
    private static final AtomicReference<ApplicationContextUtils> SINGLETON = new AtomicReference<ApplicationContextUtils>();

    public ApplicationContextUtils() {
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.fine("***** Constructing a new ApplicationContextUtils");
        }
        if (SINGLETON.get() == null) {
            synchronized (SINGLETON) {
                if (SINGLETON.get() == null) {
                    if (LOGGER.isLoggable(Level.FINE)) {
                        LOGGER.fine("Setting singleton");
                    }
                    SINGLETON.set(this);
                }
            }
        }
    }
    
    public static ApplicationContextUtils getInstance() {
        return SINGLETON.get();
    }

    public <T> T getBean(Class<T> clazz, String name) {
        return (T) getApplicationContext().getBean(name, clazz);
    }

    public <T> T getBean(Class<T> clazz) {
        return getApplicationContext().getBean(clazz);
    }

    public void autowire(Object obj) {
        getApplicationContext().getAutowireCapableBeanFactory().autowireBeanProperties(obj, AutowireCapableBeanFactory.AUTOWIRE_BY_NAME, false);
    }
}
