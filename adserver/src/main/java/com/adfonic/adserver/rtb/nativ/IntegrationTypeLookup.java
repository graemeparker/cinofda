package com.adfonic.adserver.rtb.nativ;

import com.adfonic.domain.cache.DomainCache;
import com.adfonic.domain.cache.dto.adserver.IntegrationTypeDto;

public interface IntegrationTypeLookup {

    public IntegrationTypeDto deriveBasedOnPrefix(String prefix, DomainCache cache);
}
