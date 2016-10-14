package com.adfonic.adserver.impl;

import java.util.Map;
import java.util.Objects;
import java.util.Properties;

import com.adfonic.adserver.DynamicProperties;
import com.adfonic.data.cache.AdserverDataCacheManager;

/**
 * Data cache DB backed DynamicProperties
 * 
 * @author mvanek
 *
 */
public class DataCacheProperties implements DynamicProperties {

    // primary database stored properties
    private final AdserverDataCacheManager adserverDataCacheManager;

    // fallback file stored properties
    private final Properties properties;

    public DataCacheProperties(AdserverDataCacheManager adserverDataCacheManager, Properties properties) {
        Objects.requireNonNull(adserverDataCacheManager);
        this.adserverDataCacheManager = adserverDataCacheManager;
        Objects.requireNonNull(properties);
        this.properties = properties;
    }

    @Override
    public String getProperty(DcProperty keyName) {
        return getProperty(keyName.getDbKey());
    }

    @Override
    public String getProperty(DcProperty keyName, String defaultValue) {
        return getProperty(keyName.getDbKey(), defaultValue);
    }

    public String getProperty(String propertyName, String defaultValue) {
        String value = getProperty(propertyName);
        if (value != null) {
            return value;
        } else {
            return defaultValue;
        }
    }

    public String getProperty(String propertyName) {
        String value = adserverDataCacheManager.getCache().getProperties().getProperty(propertyName);
        if (value == null) {
            value = properties.getProperty(propertyName);
        }
        return value;
    }

    public Map<String, String> getAllProperties() {
        return adserverDataCacheManager.getCache().getProperties().getAll();
    }
}
