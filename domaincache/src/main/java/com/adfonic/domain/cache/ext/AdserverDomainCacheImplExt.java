package com.adfonic.domain.cache.ext;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.adfonic.domain.cache.service.AdSpaceService;
import com.adfonic.domain.cache.service.CreativeService;
import com.adfonic.domain.cache.service.RtbCacheServiceImpl;

public class AdserverDomainCacheImplExt extends AdserverDomainCacheImpl implements AdserverDomainCacheExt, Serializable {

    private static final long serialVersionUID = 1L;

    private transient TransientDataExt transientDataExt = new TransientDataExt();
    private transient Map<String, Long> transientAdserverPluginExpectedResponseTimesByPluginName = new HashMap<String, Long>();

    public AdserverDomainCacheImplExt() {

    }

    public AdserverDomainCacheImplExt(AdserverDomainCacheImplExt copy) {
        super(copy);
    }

    public AdserverDomainCacheImplExt(Date populationStartedAt) {
        super(populationStartedAt);
    }

    @Override
    public TransientDataExt getTransientData() {
        return transientDataExt;
    }

    @Override
    public void addTransientAdserverPluginExpectedResponseTimesByPluginName(String name, Long responseTime) {
        transientAdserverPluginExpectedResponseTimesByPluginName.put(name, responseTime);
    }

    @Override
    public Long getTransientAdserverPluginExpectedResponseTimesByPluginName(String name) {
        return transientAdserverPluginExpectedResponseTimesByPluginName.get(name);
    }

    @Override
    public void clearTransientData() {
        transientDataExt = null;
        transientAdserverPluginExpectedResponseTimesByPluginName = null;

    }

    @Override
    public void setAdspaceService(AdSpaceService adSpaceService) {
        super.adSpaceService = adSpaceService;

    }

    @Override
    public void setCreativeService(CreativeService creativeService) {
        super.creativeService = creativeService;
    }

    @Override
    public void setRtbEnabled(boolean rtbEnabled) {

        ((RtbCacheServiceImpl) rtbCacheService).setRtbEnabled(rtbEnabled);
    }

}
