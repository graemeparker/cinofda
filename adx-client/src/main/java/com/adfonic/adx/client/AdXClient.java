package com.adfonic.adx.client;

import java.util.Map;

/**
 * Interface for deep integration with Ad-X.  This defines the interfaces
 * with Ad-X for provisioning new creatives, updating existing creatives,
 * tracking clicks, etc.
 */
public interface AdXClient {
    enum Platform { iOS, Android };

    enum Outcome { CREATED, UPDATED };

    /**
     * Provision a creative with Ad-X
     * @param bundleId the unique identifier as reported by the Ad-X in-app SDK
     * @param advertiserExternalId the externalID of the Advertiser
     * @param creativeExternalId the externalID of the Creative
     * @param platform the respective platform
     * @param destinationUrl the complete click-through URL as provided by the end-user
     * @return an Outcome value indicating whether provisioning resulted in the
     * creative being "created" (it hadn't previously been provisioned) or "updated"
     * (it had previously been provisioned but is now updated) on the Ad-X side
     * @throws AdXClientException if provisioning fails for any reason
     */
    Outcome provisionCreative(String bundleId, String advertiserExternalId, String creativeExternalId, Platform platform, String destinationUrl) throws AdXClientException;

    /**
     * Track a click on a provisioned creative
     * @param advertiserExternalId the externalID of the Advertiser
     * @param creativeExternalId the externalID of the Creative
     * @param clickExternalId the externalID of the Click
     * @param deviceIdentifiers the device identifier(s) supplied with the
     * impression and/or click, mapped by type (i.e. "dpid", "hifa", etc.)
     * @throws AdXClientException if provisioning fails for any reason
     */
    void trackClick(String advertiserExternalId, String creativeExternalId, String clickExternalId, Map<String,String> deviceIdentifiers) throws AdXClientException;
}