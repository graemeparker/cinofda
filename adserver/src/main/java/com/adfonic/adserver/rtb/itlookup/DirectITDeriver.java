package com.adfonic.adserver.rtb.itlookup;

import com.adfonic.adserver.rtb.nativ.IntegrationTypeLookup;
import com.adfonic.domain.cache.DomainCache;
import com.adfonic.domain.cache.dto.adserver.IntegrationTypeDto;

public class DirectITDeriver implements IntegrationTypeLookup {

    private String systemNameSuffix;

    public DirectITDeriver(String systemNameSuffix) {
        this.systemNameSuffix = systemNameSuffix;
    }

    @Override
    public IntegrationTypeDto deriveBasedOnPrefix(String prefix, DomainCache cache) {
        return cache.getIntegrationTypeBySystemName(prefix + "/" + systemNameSuffix);
    }

}
