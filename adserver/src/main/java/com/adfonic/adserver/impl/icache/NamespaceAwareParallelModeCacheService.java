package com.adfonic.adserver.impl.icache;

import java.io.Serializable;
import java.util.HashMap;

import com.adfonic.adserver.KryoManager;
import com.adfonic.adserver.ParallelModeBidDetails;
import com.adfonic.adserver.ParallelModeCacheService;
import com.adfonic.cache.CacheManager;

public class NamespaceAwareParallelModeCacheService implements ParallelModeCacheService {

    public static final String PARALLEL_MODE_BID_DETAILS_CACHE_NAME = "ParallelModeBidDetails";

    private final CacheManager cacheManager;
    private final KryoManager kryoManager;
    private final int ttlSeconds;

    public NamespaceAwareParallelModeCacheService(CacheManager cacheManager, KryoManager kryoManager, int ttlSeconds) {
        this.cacheManager = cacheManager;
        this.kryoManager = kryoManager;
        this.ttlSeconds = ttlSeconds;
    }

    // We cache the ParallelModeBidDetails as a serializable Map instead of as the
    // actual object itself.  That will help keep us out of deserialization
    // trouble later, in case the ParallelModeBidDetails object changes.

    @Override
    @SuppressWarnings("unchecked")
    public ParallelModeBidDetails getBidDetails(String key) {
        HashMap<String, Serializable> map = cacheManager.get(key, PARALLEL_MODE_BID_DETAILS_CACHE_NAME, HashMap.class);
        return map == null ? null : ParallelModeBidDetails.fromMap(map, kryoManager);
    }

    @Override
    @SuppressWarnings("unchecked")
    public ParallelModeBidDetails getAndRemoveBidDetails(String key) {
        HashMap<String, Serializable> map = cacheManager.get(key, PARALLEL_MODE_BID_DETAILS_CACHE_NAME, HashMap.class);
        cacheManager.remove(key, PARALLEL_MODE_BID_DETAILS_CACHE_NAME);
        return map == null ? null : ParallelModeBidDetails.fromMap(map, kryoManager);
    }

    @Override
    public void saveBidDetails(String key, ParallelModeBidDetails bidDetails) {
        cacheManager.set(key, bidDetails.toMap(kryoManager), PARALLEL_MODE_BID_DETAILS_CACHE_NAME, ttlSeconds);
    }

    @Override
    public boolean removeBidDetails(String key) {
        return cacheManager.remove(key, PARALLEL_MODE_BID_DETAILS_CACHE_NAME);
    }
}
