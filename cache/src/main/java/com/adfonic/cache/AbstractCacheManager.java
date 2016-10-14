package com.adfonic.cache;

import java.util.Date;
import java.util.List;

/**
 * Abstract base class for implementations of CacheManager
 */
public abstract class AbstractCacheManager implements CacheManager {
    protected static final double MILLISECONDS_IN_SECOND = 1000.0;

    @Override
    /** {@inheritDoc} */
    public abstract <T> T get(String key, Class<T> clazz) throws CacheException;

    @Override
    /** {@inheritDoc} */
    public abstract <T> T get(String key, String cacheName, Class<T> clazz) throws CacheException;

    @Override
    /** {@inheritDoc} */
    public abstract void set(String key, Object value, String cacheName, int ttlSeconds);

    @Override
    /** {@inheritDoc} */
    public abstract void set(String key, Object value, int ttlSeconds);

    @Override
    /** {@inheritDoc} */
    public abstract void set(String key, Object value, String cacheName, Date expiry);

    @Override
    /** {@inheritDoc} */
    public abstract void set(String key, Object value, Date expiry);

    @Override
    /** {@inheritDoc} */
    public abstract boolean remove(String key);

    @Override
    /** {@inheritDoc} */
    public abstract boolean remove(String key, String cacheName);

    @Override
    /** {@inheritDoc} */
    public abstract boolean remove(String key, Class<?> clazz);
    
    @Override
    /** {@inheritDoc} */
    public abstract long cacheCount() throws CacheException;
    
    @Override
    /** {@inheritDoc} */
    public abstract long cacheCount(String cacheName) throws CacheException;
    
    @Override
    /** {@inheritDoc} */
    public abstract List<String> cacheInfo() throws CacheException;

    /**
     * Calculate the number of seconds until a given date for use as a TTL.
     * @param expiryDate the expiry date
     * @return the number of seconds, rounded up to the next whole second,
     * between now and the expiry date
     */
    public static final int calculateTtlSeconds(Date expiryDate) {
        long delta = expiryDate.getTime() - System.currentTimeMillis();
        if (delta <= 0) {
            return 0;
        } else {
            return (int)Math.ceil(delta / MILLISECONDS_IN_SECOND);
        }
    }
}
