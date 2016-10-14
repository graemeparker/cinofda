package com.adfonic.adserver.impl;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.adfonic.adserver.InvalidIpAddressException;
import com.adfonic.adserver.TargetingContext;
import com.adfonic.adserver.TargetingContextFactory;
import com.adfonic.adserver.delegator.AdserverCacheDelegator;
import com.adfonic.adserver.deriver.DeriverManager;
import com.adfonic.data.cache.AdserverDataCache;
import com.adfonic.data.cache.AdserverDataCacheManager;
import com.adfonic.domain.cache.AdserverDomainCacheManager;
import com.adfonic.domain.cache.DomainCacheManager;
import com.adfonic.domain.cache.ext.AdserverDomainCache;
import com.adfonic.geo.PostalCodeIdManager;

@Component
public class TargetingContextFactoryImpl implements TargetingContextFactory {

    private final DomainCacheManager domainCacheManager;
    private final AdserverDataCacheManager adserverDataCacheManager;
    private final AdserverDomainCacheManager adserverDomainCacheManager;
    private final DeriverManager deriverManager;
    private final PostalCodeIdManager postalCodeIdManager;

    private AdserverDomainCache adserverDomainCache;
    private AdserverDataCache adserverDataCache;

    @Autowired
    public TargetingContextFactoryImpl(DomainCacheManager domainCacheManager, AdserverDataCacheManager adserverDataCacheManager,
            AdserverDomainCacheManager adserverDomainCacheManager, DeriverManager deriverManager, PostalCodeIdManager postalCodeIdManager) {

        this.domainCacheManager = domainCacheManager;
        this.adserverDataCacheManager = adserverDataCacheManager;
        this.adserverDomainCacheManager = adserverDomainCacheManager;
        this.deriverManager = deriverManager;
        this.postalCodeIdManager = postalCodeIdManager;
    }

    /** @{inheritDoc} */
    @Override
    public TargetingContextImpl createTargetingContext() {
        // Instead of autowiring the TargetingContext, we simply pass into
        // the constructor the beans it will need.  This should improve
        // performance considerably, given the fact that we construct a
        // bazillion contexts per second...
        //
        // Grab the latest instance of the domain caches.  They'll be set
        // on the TargetingContext so that the entire targeting process
        // can use a "consistent read" of the cached domain.
        AdserverDomainCache helpAdserverDomainCache = this.adserverDomainCacheManager.getCache();
        if (helpAdserverDomainCache != this.adserverDomainCache)
            this.adserverDomainCache = helpAdserverDomainCache;

        AdserverDataCache helpAdserverDataCache = this.adserverDataCacheManager.getCache();
        if (helpAdserverDataCache != this.adserverDataCache)
            this.adserverDataCache = helpAdserverDataCache;

        return new TargetingContextImpl(this.domainCacheManager.getCache(), new AdserverCacheDelegator(this.adserverDomainCache, this.adserverDataCache), deriverManager,
                postalCodeIdManager);

    }

    @Override
    public TargetingContext createTargetingContext(HttpServletRequest request, boolean useHttpHeaders) throws InvalidIpAddressException {
        TargetingContextImpl context = createTargetingContext();
        context.populateAttributes(request, useHttpHeaders);
        return context;
    }

    @Override
    public TargetingContext buildForDevice(HttpServletRequest request) throws InvalidIpAddressException {
        return createTargetingContext(request, true);
    }

    @Override
    public TargetingContext buildForServer(HttpServletRequest request) throws InvalidIpAddressException {
        return createTargetingContext(request, false);
    }
}
