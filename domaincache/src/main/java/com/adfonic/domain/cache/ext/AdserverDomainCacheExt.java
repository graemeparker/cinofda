package com.adfonic.domain.cache.ext;

import com.adfonic.domain.cache.service.AdSpaceService;
import com.adfonic.domain.cache.service.CreativeService;

public interface AdserverDomainCacheExt extends AdserverDomainCache, HasTransientData {

    void setAdspaceService(AdSpaceService adSpaceService);

    void setCreativeService(CreativeService creativeService);

    void setRtbEnabled(boolean enabled);
}
