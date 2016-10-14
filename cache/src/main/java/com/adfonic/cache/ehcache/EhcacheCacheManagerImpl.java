package com.adfonic.cache.ehcache;

import java.util.Date;
import java.util.List;

import org.apache.commons.lang.NotImplementedException;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;
import net.sf.ehcache.config.CacheConfiguration;

import com.adfonic.cache.AbstractCacheManager;
import com.adfonic.cache.CacheException;

/**
 * In-memory, in-JVM implementation of CacheManager using Ehcache
 */
public class EhcacheCacheManagerImpl extends AbstractCacheManager {
    private final Cache cache;
    
    public EhcacheCacheManagerImpl() {
        this(0); // 0 == no limit
    }
    
    public EhcacheCacheManagerImpl(int maxElementsInMemory) {
        // More configurability could be added if the need ever arises, but
        // for now we can probably get away with uber-simple config since
        // this impl is probably only going to be used as a stub in testing
        // or in local developer mode for apps like adserver.
        cache = new Cache(new CacheConfiguration("EhcacheCacheManagerImpl", 0).maxEntriesLocalHeap(maxElementsInMemory));
        cache.initialise(); // cache is unusable without this call
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(String key, Class<T> clazz) throws CacheException {
        Element element = cache.get(key);
        return element == null ? null : (T)element.getValue();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(String key, String cacheName, Class<T> clazz) throws CacheException {
        return (T)get(cacheName + "." + key, Object.class);
    }

    @Override
    public void set(String key, Object value, String cacheName, int ttlSeconds) {
        set(cacheName + "." + key, value, ttlSeconds);
    }

    @Override
    public void set(String key, Object value, int ttlSeconds) {
        cache.put(new Element(key, value, false, ttlSeconds, ttlSeconds));
    }

    @Override
    public void set(String key, Object value, String cacheName, Date expiry) {
        set(cacheName + "." + key, value, expiry);
    }

    @Override
    public void set(String key, Object value, Date expiry) {
        int ttlSeconds = calculateTtlSeconds(expiry);
        if (ttlSeconds > 0) {
            set(key, value, ttlSeconds);
        }
    }

    @Override
    public boolean remove(String key) {
        return cache.remove(key);
    }

    @Override
    public boolean remove(String key, String cacheName) {
        return remove(cacheName + "." + key);
    }

    @Override
    public boolean remove(String key, Class<?> clazz) {
        return remove(key);
    }

    @Override
    public long cacheCount() throws CacheException {
        return cacheCount((String) null);
    }

    @Override
    public long cacheCount(String cacheName) throws CacheException {
        throw new NotImplementedException("Method not yet implemented");        
    }
    
    @Override
    public List<String> cacheInfo() throws CacheException{
        throw new NotImplementedException("Method not yet implemented");
    }
}
