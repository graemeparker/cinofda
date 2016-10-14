package com.adfonic.retargeting;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.adfonic.dmp.cache.DmpCacheManager;

public abstract class AbstractDmpAudienceCacheManager implements DmpCacheManager {

    static final String SET_NAME = "DMP";

    public AbstractDmpAudienceCacheManager() {
        super();
    }

    protected abstract void delteFromCache(String deviceIdKey) throws CacheException;

    protected abstract Set<Long> extractTargetedCampaignIds(Map<Long, String> deviceIdentifiers, List<String> keys);

}
