package com.adfonic.adserver.impl.icache;

import java.util.Date;

import com.adfonic.adserver.impl.AbstractFrequencyCounter;
import com.adfonic.cache.CacheManager;
import com.adfonic.util.stats.CounterManager;

public class NamespaceAwareFrequencyCounter extends AbstractFrequencyCounter {

    private static final String FREQUENCY_COUNTER_CACHE_NAME = "FrequencyCounter";

    private final CacheManager cacheManager;

    private final CounterManager counterManager;

    public NamespaceAwareFrequencyCounter(CacheManager cacheManager, CounterManager counterManager) {
        this.cacheManager = cacheManager;
        this.counterManager = counterManager;
    }

    @Override
    protected String getValue(String key) {
        String value;
        long statTime = System.currentTimeMillis();
        try {
            value = cacheManager.get(key, FREQUENCY_COUNTER_CACHE_NAME, String.class);
        } finally {
            long endTime = System.currentTimeMillis();
        }
        return value;
    }

    @Override
    protected void setValue(String key, String value, long expireTimestamp) {
        long statTime = System.currentTimeMillis();
        try {
            cacheManager.set(key, value, FREQUENCY_COUNTER_CACHE_NAME, new Date(expireTimestamp));
        } finally {
            long endTime = System.currentTimeMillis();
        }

    }
}
