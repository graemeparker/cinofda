package com.adfonic.domain.cache;

import java.io.File;
import java.io.IOException;

import com.adfonic.domain.cache.ext.AdserverDomainCache;

/** Singleton that manages access to a cache of adserver domain objects.
    This manager exposes access to the AdserverDomainCache.
    @see com.adfonic.domain.cache.ext.AdserverDomainCache
    @see com.adfonic.domain.cache.ext.loader.AdserverDomainCacheLoader
*/
public class AdserverDomainCacheManager extends AbstractSerializableCacheS3Manager<AdserverDomainCache> {

    public AdserverDomainCacheManager(File rootDir, String label, boolean useMemory) {
        super(AdserverDomainCache.class, rootDir, label, useMemory);
    }

    @Override
    protected AdserverDomainCache reloadCache(File serializedFile) throws IOException, ClassNotFoundException {
        AdserverDomainCache cache = super.reloadCache(serializedFile);
        // update JMX monitored value
        lastPopulationStartedAt = cache.getPopulationStartedAt();
        return cache;
    }
}
