package com.adfonic.adserver.impl.icache;

import java.io.Serializable;
import java.util.Map;

import com.adfonic.adserver.KryoManager;
import com.adfonic.adserver.ParallelModeBidDetails;
import com.adfonic.adserver.ParallelModeCacheService;
import com.adfonic.cache.CacheManager;

/**
 * ParallelModeCacheService implementation that can work properly on a shared namespace,
 * i.e. memcached or an in-memory cache like ehcache.
 */
public class SharedNamespaceParallelModeCacheService implements ParallelModeCacheService {

    private static final String KEY_PREFIX = "par.";

    private final CacheManager cacheManager;
    private final KryoManager kryoManager;
    private final int ttlSeconds;

    public SharedNamespaceParallelModeCacheService(CacheManager cacheManager, KryoManager kryoManager, int ttlSeconds) {
        this.cacheManager = cacheManager;
        this.kryoManager = kryoManager;
        this.ttlSeconds = ttlSeconds;
    }

    // Namespace is shared...need a prefix
    private static String makeNamespaceSafeKey(String key) {
        return KEY_PREFIX + key;
    }

    // We cache the ParallelModeBidDetails as a serializable Map instead of as the
    // actual object itself.  That will help keep us out of deserialization
    // trouble later, in case the ParallelModeBidDetails object changes.

    @Override
    @SuppressWarnings("unchecked")
    public ParallelModeBidDetails getBidDetails(String key) {
        Map<String, Serializable> map = cacheManager.get(makeNamespaceSafeKey(key), Map.class);
        return map == null ? null : ParallelModeBidDetails.fromMap(map, kryoManager);
    }

    @Override
    @SuppressWarnings("unchecked")
    public ParallelModeBidDetails getAndRemoveBidDetails(String key) {
        String namespaceSafeKey = makeNamespaceSafeKey(key);
        Map<String, Serializable> map = cacheManager.get(namespaceSafeKey, Map.class);
        cacheManager.remove(namespaceSafeKey);
        return map == null ? null : ParallelModeBidDetails.fromMap(map, kryoManager);
    }

    @Override
    public void saveBidDetails(String key, ParallelModeBidDetails bidDetails) {
        cacheManager.set(makeNamespaceSafeKey(key), bidDetails.toMap(kryoManager), ttlSeconds);
    }

    @Override
    public boolean removeBidDetails(String key) {
        return cacheManager.remove(makeNamespaceSafeKey(key));
    }
}
