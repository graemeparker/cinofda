package com.adfonic.adserver.deriver;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.stereotype.Component;

@Component
public class DeriverManager {
    private static final transient Logger LOG = Logger.getLogger(DeriverManager.class.getName());
    private final Map<String,Deriver> deriverMap = new HashMap<String,Deriver>();

    /** Register a deriver for use with a given attribute */
    public void registerDeriver(String attribute, Deriver deriver) {
        if (LOG.isLoggable(Level.INFO)) {
            LOG.info("Registering " + deriver.getClass().getName() + " for use with attribute: " + attribute);
        }
        deriverMap.put(attribute, deriver);
    }

    /**
     * @return a deriver that can derive the given attribute, or null if there
     * is no deriver available for that attribute
     */
    public Deriver getDeriver(String attribute) {
        return deriverMap.get(attribute);
    }
}
