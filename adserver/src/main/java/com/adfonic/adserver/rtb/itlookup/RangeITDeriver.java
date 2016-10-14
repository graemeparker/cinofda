package com.adfonic.adserver.rtb.itlookup;

import com.adfonic.adserver.rtb.nativ.IntegrationTypeLookup;
import com.adfonic.domain.cache.DomainCache;
import com.adfonic.domain.cache.dto.adserver.IntegrationTypeDto;

public class RangeITDeriver implements IntegrationTypeLookup {

    String version;

    public RangeITDeriver(String version) {
        this.version = version;
    }

    @Override
    public IntegrationTypeDto deriveBasedOnPrefix(String prefix, DomainCache cache) {
        return com.adfonic.adserver.deriver.impl.IntegrationTypeDeriver.deriveIntegrationTypeBasedOnVersion(prefix, version, cache, "_rtb_request_");
    }

}
