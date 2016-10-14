package com.adfonic.adserver.impl.icache;

import java.io.Serializable;
import java.util.HashMap;

import com.adfonic.adserver.KryoManager;
import com.adfonic.adserver.rtb.RtbBidDetails;
import com.adfonic.adserver.rtb.RtbCacheService;
import com.adfonic.cache.CacheManager;

public class NamespaceAwareRtbCacheService implements RtbCacheService {

    private static final String RTB_BID_DETAILS_CACHE_NAME = "RtbBidDetails";

    private final CacheManager cacheManager;
    private final KryoManager kryoManager;
    private final int ttlSeconds;

    public NamespaceAwareRtbCacheService(CacheManager cacheManager, KryoManager kryoManager, int ttlSeconds) {
        this.cacheManager = cacheManager;
        this.kryoManager = kryoManager;
        this.ttlSeconds = ttlSeconds;
    }

    // We cache the RtbBidDetails as a serializable Map instead of as the
    // actual object itself.  That will help keep us out of deserialization
    // trouble later, in case the RtbBidDetails object changes.

    @Override
    @SuppressWarnings("unchecked")
    public RtbBidDetails getBidDetails(String key) {
        HashMap<String, Serializable> map = cacheManager.get(key, RTB_BID_DETAILS_CACHE_NAME, HashMap.class);
        return map == null ? null : RtbBidDetails.fromMap(map, kryoManager);
    }

    @Override
    @SuppressWarnings("unchecked")
    public RtbBidDetails getAndRemoveBidDetails(String key) {
        HashMap<String, Serializable> map = cacheManager.get(key, RTB_BID_DETAILS_CACHE_NAME, HashMap.class);
        cacheManager.remove(key, RTB_BID_DETAILS_CACHE_NAME);
        return map == null ? null : RtbBidDetails.fromMap(map, kryoManager);
    }

    @Override
    public void saveBidDetails(String key, RtbBidDetails bidDetails) {
        cacheManager.set(key, bidDetails.toMap(kryoManager), RTB_BID_DETAILS_CACHE_NAME, ttlSeconds);
    }

    @Override
    public boolean removeBidDetails(String key) {
        return cacheManager.remove(key, RTB_BID_DETAILS_CACHE_NAME);
    }

}
