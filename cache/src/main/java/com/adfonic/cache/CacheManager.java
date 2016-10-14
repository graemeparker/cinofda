package com.adfonic.cache;

import java.util.Date;
import java.util.List;

public interface CacheManager {
    /**
     * Generic retrieval method
     * @param key the key of the object to retrieve
     * @param clazz the class of the object to retrieve
     * @return the object if found, otherwise null
     * @throws CacheException if the cache provider call fails
     */
    <T> T get(String key, Class<T> clazz) throws CacheException;

    /**
     * Generic retrieval method
     * @param key the key of the object to retrieve
     * @param cacheName the name of the target cache
     * @param clazz the class of the object to retrieve
     * @return the object if found, otherwise null
     * @throws CacheException if the cache provider call fails
     */
    <T> T get(String key, String cacheName, Class<T> clazz) throws CacheException;

    /**
     * Generic storage method
     * @param key the key of the object to store
     * @param value the value of the object to store
     * @param cacheName the target cache
     * @param ttlSeconds the TTL in seconds
     * @throws CacheException if the cache provider call fails
     */
    void set(String key, Object value, String cacheName, int ttlSeconds);

    /**
     * Generic storage method
     * @param key the key of the object to store
     * @param value the value of the object to store
     * @param ttlSeconds the TTL in seconds
     * @throws CacheException if the cache provider call fails
     */
    void set(String key, Object value, int ttlSeconds);

    /**
     * Generic storage method
     * @param key the key of the object to store
     * @param value the value of the object to store
     * @param cacheName the target cache
     * @param expiry the exact time at which the value should expire
     * @throws CacheException if the cache provider call fails
     */
    void set(String key, Object value, String cacheName, Date expiry);

    /**
     * Generic storage method
     * @param key the key of the object to store
     * @param value the value of the object to store
     * @param expiry the exact time at which the value should expire
     * @throws CacheException if the cache provider call fails
     */
    void set(String key, Object value, Date expiry);

    /**
     * Generic removal method
     * @param key the key of the object to remove
     * @return true if the object was removed
     * @throws CacheException if the cache provider call fails
     */
    boolean remove(String key);
 
    /**
     * Generic removal method
     * @param key the key of the object to remove
     * @param cacheName the target cache
     * @return true if the object was removed
     * @throws CacheException if the cache provider call fails
     */
    boolean remove(String key, String cacheName);
    
    /**
     * Generic removal method
     * @param key the key of the object to remove
     * @param cacheName the target cache
     * @param clazz the class of the object to retrieve
     * @return true if the object was removed
     * @throws CacheException if the cache provider call fails
     */
    boolean remove(String key, Class<?> clazz) ;
    
    long cacheCount() throws CacheException;
    long cacheCount(String cacheName) throws CacheException;
    List<String> cacheInfo() throws CacheException;
}
