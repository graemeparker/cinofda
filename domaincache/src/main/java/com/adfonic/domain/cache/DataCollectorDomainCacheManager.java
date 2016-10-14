package com.adfonic.domain.cache;

import java.io.File;

/** Singleton that manages access to a cache of domain objects for DataCollector.
    This manager exposes access to the DataCollectorDomainCache.
    @see com.adfonic.domain.cache.DataCollectorDomainCache
    @see com.adfonic.domain.cache.DataCollectorDomainCacheLoader
*/
public class DataCollectorDomainCacheManager extends AbstractSerializableCacheS3Manager<DataCollectorDomainCache> {
    public DataCollectorDomainCacheManager(File rootDir, String label, boolean useMemory) {
        super(DataCollectorDomainCache.class, rootDir, label, useMemory);
    }
    /*
     * There are no diagnostic timestamps in DataCollectorDomainCache...
    @Override
    protected DataCollectorDomainCache reloadCache(File serializedFile) throws IOException, ClassNotFoundException {
        DataCollectorDomainCache cache = super.reloadCache(serializedFile);
        // update JMX monitored value
        lastPopulationStartedAt = cache.getPopulationStartedAt();
        return cache;
    }
    */
}
