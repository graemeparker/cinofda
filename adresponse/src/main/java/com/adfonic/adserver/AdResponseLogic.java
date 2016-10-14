package com.adfonic.adserver;

import javax.servlet.http.HttpServletRequest;

import com.adfonic.domain.cache.dto.adserver.adspace.AdSpaceDto;
import com.adfonic.domain.cache.dto.adserver.creative.CreativeDto;

/**
 * Encapsulated logic for generating and manipulating ad responses
 */
public interface AdResponseLogic {
    /**
     * Add a background image for "pretty" text ad rendering, only as needed
     */
    void addBackgroundImageAsNeeded(AdComponents adComponents, TargetingContext context);

    /**
     * Add beacons to an ad response only as needed
     */
    void addBeaconsAsNeeded(AdComponents adComponents, CreativeDto creative, AdSpaceDto adSpace, String priceUrlMacro, TargetingContext context, Impression impression,
            HttpServletRequest request);

    /**
     * Add beacons to an ad response
     */
    void addBeacons(AdComponents adComponents, CreativeDto creative, AdSpaceDto adSpace, String priceUrlMacro, TargetingContext context, Impression impression,
            HttpServletRequest request);

    /**
     * Add a specific beacon image URL to an ad response
     */
    void addBeacon(AdComponents adComponents, String beaconImageUrl, TargetingContext context);

    /**
     * Add extended capabilities data to an ad response as needed
     */
    void addExtendedDataAsNeeded(AdComponents adComponents, CreativeDto creative);

    /**
     * Add pricing info to an ad response as needed
     */
    void addPricingAsNeeded(AdComponents adComponents, CreativeDto creative, AdSpaceDto adSpace, TargetingContext context);

    /**
     * Generate a conventional AdComponents ad response, "full" in the sense that
     * beacons, background image, pricing, extended data, etc. will be added if
     * they're needed.
     */
    AdComponents generateFullAdComponents(TargetingContext context, AdSpaceDto adSpace, CreativeDto creative, ProxiedDestination pd, Impression impression,
            HttpServletRequest request);

    /**
     * Generate a conventional AdComponents ad response, "bare" in the sense that
     * nothing additional (beacons, pricing, etc.) will be added.
     */
    AdComponents generateBareAdComponents(TargetingContext context, AdSpaceDto adSpace, CreativeDto creative, ProxiedDestination pd, Impression impression,
            HttpServletRequest request);

    /**
     * Generate a test AdComponents ad response
     */
    AdComponents generateTestAdComponents(TargetingContext context, AdSpaceDto adSpace, HttpServletRequest request) throws java.io.IOException;

    /**
     * Post-process the AdComponents if needed, doing any text substitution
     * as required, i.e. for %man% and %phn%, etc.  This is generally called
     * at ad request time.
     */
    void postProcessAdComponents(AdComponents adComponents, TargetingContext context);

}
