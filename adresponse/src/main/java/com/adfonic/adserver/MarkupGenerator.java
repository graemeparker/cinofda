package com.adfonic.adserver;

import java.util.Map;

import com.adfonic.domain.AdSpace.ColorScheme;
import com.adfonic.domain.cache.dto.adserver.adspace.AdSpaceDto;
import com.adfonic.domain.cache.dto.adserver.adspace.PublicationDto;
import com.adfonic.domain.cache.dto.adserver.creative.CampaignDto;
import com.adfonic.domain.cache.dto.adserver.creative.CreativeDto;

public interface MarkupGenerator {

    ColorScheme DEFAULT_COLOR_SCHEME = ColorScheme.grey;

    /**
     * Generate HTML markup to be used in an ad response
     * 
     * @param adComponents
     *            the AdComponents that have already been generated, which are
     *            generally used by markup templates to render the ad content
     *            (also used as a fallback to determine the FormatDto for test
     *            ad markup)
     * @param context
     *            the TargetingContext in use
     * @param creative
     *            the Creative that was selected by targeting
     * @param impression
     *            the Impression corresponding to this ad being served
     * @param renderBeacons
     *            whether or not beacons should be rendered
     * @return an HTML string
     * @throws java.io.IOException
     *             if an error occurs while processing a template
     */
    String generateMarkup(AdComponents adComponents, TargetingContext context, AdSpaceDto adSpace, CreativeDto creative, Impression impression, boolean renderBeacons)
            throws java.io.IOException;

    /**
     * Just a helper for MacroUtil.getVelocityMacroProps that pulls values from dtos and context
     */
    public static Map<String, String> getVelocityMacroProps(TargetingContext context, AdSpaceDto adSpace, CreativeDto creative, Impression impression) {

        String adSpaceExternalID = null;
        String publicationExternalID = null;
        String publisherExternalID = null;
        if (adSpace != null) {
            PublicationDto publication = adSpace.getPublication();
            adSpaceExternalID = adSpace.getExternalID();
            publicationExternalID = publication.getExternalID();
            publisherExternalID = publication.getPublisher().getExternalId();
        }

        String creativeExternalID = null;
        String campaignExternalID = null;
        String advertiserExternalID = null;
        if (creative != null) {
            CampaignDto campaign = creative.getCampaign();
            creativeExternalID = creative.getExternalID();
            campaignExternalID = campaign.getExternalID();
            advertiserExternalID = campaign.getAdvertiser().getExternalID();
        }

        Map<String, Long> didTypeIdsBySystemName = context.getDomainCache().getDeviceIdentifierTypeIdsBySystemName();
        Map<String, String> deviceProperties = context.getAttribute(TargetingContext.DEVICE_PROPERTIES);
        boolean trackingDisabled = context.isFlagTrue(TargetingContext.TRACKING_DISABLED);

        return MacroTractor.getVelocityMacroProps(adSpaceExternalID, publicationExternalID, publisherExternalID, creativeExternalID, campaignExternalID, advertiserExternalID,
                impression, didTypeIdsBySystemName, deviceProperties, trackingDisabled);
    }

    public static String resolveMacros(String targetUrl, TargetingContext context, AdSpaceDto adSpace, CreativeDto creative, Impression impression) {
        return resolveMacros(targetUrl, adSpace, creative, impression, context, null, true, null);
    }

    public static String resolveMacros(String val, AdSpaceDto adSpace, CreativeDto creative, Impression impression, TargetingContext context, String destination,
            boolean doProcDest, Boolean isTracked) {

        String adSpaceExternalID = null;
        String publicationExternalID = null;
        String publisherExternalID = null;
        if (adSpace != null) {
            PublicationDto publication = adSpace.getPublication();
            adSpaceExternalID = adSpace.getExternalID();
            publicationExternalID = publication.getExternalID();
            publisherExternalID = publication.getPublisher().getExternalId();
        }

        String creativeExternalID = null;
        String campaignExternalID = null;
        String advertiserExternalID = null;
        if (creative != null) {
            CampaignDto campaign = creative.getCampaign();
            creativeExternalID = creative.getExternalID();
            campaignExternalID = campaign.getExternalID();
            advertiserExternalID = campaign.getAdvertiser().getExternalID();
        }

        Map<String, Long> didTypes = context.getDomainCache().getDeviceIdentifierTypeIdsBySystemName();
        Map<String, String> deviceProperties = context.getAttribute(TargetingContext.DEVICE_PROPERTIES);
        boolean trackingDisabled = context.isFlagTrue(TargetingContext.TRACKING_DISABLED);

        return MacroTractor.resolveMacros(val, publisherExternalID, adSpaceExternalID, creativeExternalID, campaignExternalID, advertiserExternalID, publicationExternalID,
                impression, didTypes, deviceProperties, trackingDisabled, destination, doProcDest, isTracked);
    }
}
