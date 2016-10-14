package com.adfonic.adserver.rtb.itlookup;

import com.adfonic.adserver.rtb.nativ.IntegrationTypeLookup;
import com.adfonic.domain.cache.DomainCache;
import com.adfonic.domain.cache.dto.adserver.IntegrationTypeDto;

public class FallbackDirectITDeriver implements IntegrationTypeLookup {

    private final String[] levels;

    public FallbackDirectITDeriver(String... levels) {
        this.levels = levels;
    }

    @Override
    public IntegrationTypeDto deriveBasedOnPrefix(String prefix, DomainCache cache) {
        return deriveDeepest(prefix, 0, cache);
    }

    private IntegrationTypeDto deriveDeepest(String currentPath, int idx, DomainCache cache) {
        IntegrationTypeDto deeperDerived = null;

        if (idx < levels.length) {
            deeperDerived = deriveDeepest(currentPath + "/" + levels[idx], idx + 1, cache);
        }

        return deeperDerived != null ? deeperDerived : cache.getIntegrationTypeBySystemName(currentPath);

    }

}
