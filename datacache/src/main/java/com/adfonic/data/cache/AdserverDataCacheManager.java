package com.adfonic.data.cache;

import com.adfonic.domain.cache.service.WeightageServices;


public interface AdserverDataCacheManager {
    
    AdserverDataCache getCache();

    WeightageServices getEcpmDataCacheAsWS();
}
