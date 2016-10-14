package com.adfonic.audit;

import java.util.Arrays;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;

/**
 * Manages the set of classes and fields to be audited by an Auditor instance.
 */
@SuppressWarnings("rawtypes")
public class AuditorConfig {
    
	private Map<Class, List<String>> watchedClassMap;

    public AuditorConfig() {
        watchedClassMap = new HashMap<Class, List<String>>();
    }

    public void setAuditedProperties(Properties props)  {
        for (String className : props.stringPropertyNames()) {
            try {
                watchedClassMap.put(Class.forName(className), Arrays.asList(StringUtils.split(props.getProperty(className), ',')));
            } catch (Exception e) {
                throw new IllegalStateException("Failed to set audited properties", e);
            }
        }
    }

    public Map<Class,List<String>> getWatchedClassMap() {
        return watchedClassMap;
    }
}