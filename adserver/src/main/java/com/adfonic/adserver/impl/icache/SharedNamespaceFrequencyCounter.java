package com.adfonic.adserver.impl.icache;

import java.util.Date;

import com.adfonic.adserver.impl.AbstractFrequencyCounter;
import com.adfonic.cache.CacheManager;
import com.adfonic.util.stats.CounterManager;

/**
 * FrequencyCounter implementation that can work properly on a shared namespace,
 * i.e. memcached or an in-memory cache like ehcache.
 */
public class SharedNamespaceFrequencyCounter extends AbstractFrequencyCounter {

    private static final String KEY_PREFIX = "f.";

    private final CacheManager cacheManager;

    private final CounterManager counterManager;

    public SharedNamespaceFrequencyCounter(CacheManager cacheManager, CounterManager counterManager) {
        this.cacheManager = cacheManager;
        this.counterManager = counterManager;
    }

    @Override
    protected String makeKey(final String uniqueIdentifier, final long entityId, FrequencyEntity frequencyEntity) {
        // We need the prefix here since we're sharing the namespace
        return KEY_PREFIX + super.makeKey(uniqueIdentifier, entityId, frequencyEntity);
    }

    @Override
    protected String getValue(String key) {
        String value;
        long statTime = System.currentTimeMillis();
        try {
            value = cacheManager.get(key, String.class);
        } finally {
            long endTime = System.currentTimeMillis();
            counterManager.incrementCounter("CitrustLeafSharedNamSpaceFrequencyCounterGetTotalTime", endTime - statTime);
            counterManager.incrementCounter("CitrustLeafSharedNamSpaceFrequencyCounterGetTotalCall");
        }
        return value;
    }

    @Override
    protected void setValue(String key, String value, long expireTimestamp) {
        long statTime = System.currentTimeMillis();
        try {
            cacheManager.set(key, value, new Date(expireTimestamp));
        } finally {
            long endTime = System.currentTimeMillis();
            counterManager.incrementCounter("CitrustLeafSharedNamSpaceFrequencyCounterSetTotalTime", endTime - statTime);
            counterManager.incrementCounter("CitrustLeafSharedNamSpaceFrequencyCounterSetTotalCall");
        }

    }
}
