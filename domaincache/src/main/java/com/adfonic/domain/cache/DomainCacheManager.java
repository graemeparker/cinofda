package com.adfonic.domain.cache;

import java.io.File;
import java.io.IOException;

/** Singleton that manages access to a cache of domain objects.
    This manager exposes access to the DomainCache.
    @see com.adfonic.domain.cache.DomainCache
    @see com.adfonic.domain.cache.DomainCacheLoader
*/
public class DomainCacheManager extends AbstractSerializableCacheS3Manager<DomainCache> {

    public DomainCacheManager(File rootDir, String label, boolean useMemory) {
        super(DomainCache.class, rootDir, label, useMemory);
    }

    @Override
    protected DomainCache reloadCache(File serializedFile) throws IOException, ClassNotFoundException {
        DomainCache cache = super.reloadCache(serializedFile);
        // update JMX monitored value
        lastPopulationStartedAt = cache.getPopulationStartedAt();
        return cache;

    }
}
