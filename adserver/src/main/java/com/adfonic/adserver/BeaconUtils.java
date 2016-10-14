package com.adfonic.adserver;

import com.adfonic.domain.Feature;
import com.adfonic.domain.cache.dto.adserver.IntegrationTypeDto;

public final class BeaconUtils {

    private BeaconUtils() {
    }

    public static boolean shouldUseBeacons(IntegrationTypeDto integrationType) {

        // Force beacons for anything without an integration type.
        if (integrationType == null) {
            return true;
        } else {
            return integrationType.getSupportedFeatures().contains(Feature.BEACON);
        }
    }
}
