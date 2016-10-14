package com.adfonic.adserver.rtb.itlookup;

import com.adfonic.domain.cache.DomainCache;
import com.adfonic.domain.cache.dto.adserver.IntegrationTypeDto;

public class OverridingCustomRangeITDeriver extends RangeITDeriver {

    private String customSfx;

    public OverridingCustomRangeITDeriver(String version, String customSfx) {
        super(version);
        this.customSfx = customSfx;
    }

    @Override
    public IntegrationTypeDto deriveBasedOnPrefix(String prefix, DomainCache cache) {
        IntegrationTypeDto integrationType = super.deriveBasedOnPrefix(prefix + "/" + customSfx, cache);
        return integrationType != null ? integrationType : super.deriveBasedOnPrefix(prefix, cache);
    }
}
