package com.adfonic.adserver.impl.icache;

import java.io.Serializable;
import java.util.Map;

import com.adfonic.adserver.KryoManager;
import com.adfonic.adserver.rtb.RtbBidDetails;
import com.adfonic.adserver.rtb.RtbCacheService;
import com.adfonic.cache.CacheManager;

/**
 * RtbCacheService implementation that can work properly on a shared namespace,
 * i.e. memcached or an in-memory cache like ehcache.
 */
public class SharedNamespaceRtbCacheService implements RtbCacheService {

    private static final String KEY_PREFIX = "RTB.";

    private final CacheManager cacheManager;
    private final KryoManager kryoManager;
    private final int ttlSeconds;

    public SharedNamespaceRtbCacheService(CacheManager cacheManager, KryoManager kryoManager, int ttlSeconds) {
        this.cacheManager = cacheManager;
        this.kryoManager = kryoManager;
        this.ttlSeconds = ttlSeconds;
    }

    // Namespace is shared...need a prefix
    private static String makeNamespaceSafeKey(String key) {
        return KEY_PREFIX + key;
    }

    // We cache the RtbBidDetails as a serializable Map instead of as the
    // actual object itself.  That will help keep us out of deserialization
    // trouble later, in case the RtbBidDetails object changes.

    @Override
    @SuppressWarnings("unchecked")
    public RtbBidDetails getBidDetails(String key) {
        Map<String, Serializable> map = cacheManager.get(makeNamespaceSafeKey(key), Map.class);
        return map == null ? null : RtbBidDetails.fromMap(map, kryoManager);
    }

    @Override
    @SuppressWarnings("unchecked")
    public RtbBidDetails getAndRemoveBidDetails(String key) {
        String namespaceSafeKey = makeNamespaceSafeKey(key);
        Map<String, Serializable> map = cacheManager.get(namespaceSafeKey, Map.class);
        cacheManager.remove(namespaceSafeKey);
        return map == null ? null : RtbBidDetails.fromMap(map, kryoManager);
    }

    @Override
    public void saveBidDetails(String key, RtbBidDetails bidDetails) {
        cacheManager.set(makeNamespaceSafeKey(key), bidDetails.toMap(kryoManager), ttlSeconds);
    }

    @Override
    public boolean removeBidDetails(String key) {
        return cacheManager.remove(makeNamespaceSafeKey(key));
    }
}
