package com.adfonic.domain.cache.service;

import com.adfonic.domain.cache.SerializableCache;

public interface BaseCache extends SerializableCache {

    /**
     * This must be called from adserver or anywhere you deserialize cache in memory
     */
    void afterDeserialize();

    void beforeSerialization();
}
