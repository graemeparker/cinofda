package com.adfonic.adserver;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.adfonic.domain.DeviceIdentifierType;

/**
 * Allowed macro placeholders that client can use are listed https://developer.byyd-tech.com/index.php/Click_Tokens (plus we have some unofficial ones)
 * 
 * Macros are used 2 in different ways
 * 1. Standard banners and simple image or textual ads
 * Such ads does not have preexisting makup until we generate it in adserver but clients also submit destination url (click link) and impression trackers (beacons) that may contain %placeholders%
 *  
 * 2. Third party tags (including MRAID and VAST) are submitted by clients and tag markup may contain %placeholders% (most likely in links or javascript variables)
 * When creative is saved in Tools - ExtendedCreativeManagerJpaImpl.processExtendedCreativeTemplateContent(...)
 * tags are preprocessed and %placeholder% are substituted with ${macro.placeholder}
 * Such macro substitutions must be present in CLICK_TOKEN_REFERENCE table
 * 
 * 
 * We need this shared for several platform projects and some of them have no cache DTO's in classpath...
 */
public class MacroTractor {

    private static final String ZERO_STRING = "0";
    private static final String ONE_STRING = "1";

    /**
     * For 3rd party tags (including MRAID and VAST), we use velocity to do the macro expansion and for velocity we need map with right names and values
     */
    public static Map<String, String> getVelocityMacroProps(String adSpaceExtId, String publicationExtId, String publisherExtId, String creativeExtId, String campaignExtId,
            String advertiserExtId, Impression impression, Map<String, Long> didTypeIdsBySystemName, Map<String, String> deviceProperties, boolean trackingDisabled) {
        Map<String, String> macros = new HashMap<>(25);

        macros.put("timestamp", String.valueOf(System.currentTimeMillis()));

        macros.put("publication", StringUtils.defaultString(adSpaceExtId));
        macros.put("publication_id", StringUtils.defaultString(publicationExtId));
        macros.put("pid", StringUtils.defaultString(publicationExtId));
        macros.put("publisher_id", StringUtils.defaultString(publisherExtId));

        macros.put("creative", StringUtils.defaultString(creativeExtId));
        macros.put("campaign", StringUtils.defaultString(campaignExtId));
        macros.put("advertiser", StringUtils.defaultString(advertiserExtId));

        macros.put("platform", getDeviceOs(deviceProperties)); // IOS or ANDROID
        macros.put("device_type", getDeviceType(deviceProperties)); // PHN or TAB

        macros.put("dtd", trackingDisabled ? ONE_STRING : ZERO_STRING); // Device tracking disabled

        if (impression != null) {
            macros.put("click", impression.getExternalID()); // Surprise! click is actually impression external id
            addDeviceIdMacroProps(impression, didTypeIdsBySystemName, macros);
        }

        return macros;
    }

    private static void addDeviceIdMacroProps(Impression impression, Map<String, Long> didTypeIdsBySystemName, Map<String, String> macros) {
        String dpid = getDeviceIdentifier(impression, DeviceIdentifierType.SYSTEM_NAME_DPID, didTypeIdsBySystemName);
        macros.put("dpid", dpid);
        String idfa = getDeviceIdentifier(impression, DeviceIdentifierType.SYSTEM_NAME_IFA, didTypeIdsBySystemName);
        macros.put("ifa", idfa);
        macros.put("idfa", idfa); // idfa or ifa, same value
        String idfa_sha1 = getDeviceIdentifier(impression, DeviceIdentifierType.SYSTEM_NAME_HIFA, didTypeIdsBySystemName);
        macros.put("hifa", idfa_sha1);
        String idfa_md5 = getDeviceIdentifier(impression, DeviceIdentifierType.SYSTEM_NAME_IDFA_MD5, didTypeIdsBySystemName);
        macros.put("idfa_md5", idfa_md5);
        String adid = getDeviceIdentifier(impression, DeviceIdentifierType.SYSTEM_NAME_ADID, didTypeIdsBySystemName);
        macros.put("adid", adid);
        String adid_md5 = getDeviceIdentifier(impression, DeviceIdentifierType.SYSTEM_NAME_ADID_MD5, didTypeIdsBySystemName);
        macros.put("adid_md5", adid_md5);
        String adid_sha1 = getDeviceIdentifier(impression, DeviceIdentifierType.SYSTEM_NAME_ADID_SHA1, didTypeIdsBySystemName);
        macros.put("adid_sha1", adid_sha1);

        // Only raw IFDA/ADID into %device_id% plus legacy dpid
        String device_id = StringUtils.EMPTY;
        if (StringUtils.EMPTY != idfa) {
            device_id = idfa;
        } else if (StringUtils.EMPTY != adid) {
            device_id = adid;
        } else if (StringUtils.EMPTY != dpid) {
            device_id = dpid;
        }
        macros.put("device_id", device_id);
    }

    /**
     * Simple click/beacon url %macro% expansion
     * If some parameter is null, then it's macro will be expanded into empty string
     * If caller do not have access to @param didTypeIdsBySystemName or @param deviceProperties, he must pass Collection.emptyMap()
     */
    public static String resolveMacros(String targetUrl, String publisherExternalID, String adSpaceExternalID, String creativeExternalID, String campaignExternalID,
            String advertiserExternalID, String publicationExternalID, Impression impression, Map<String, Long> didTypeIdsBySystemName, Map<String, String> deviceProperties,
            boolean trackingDisabled) {
        return resolveMacros(targetUrl, publisherExternalID, adSpaceExternalID, creativeExternalID, campaignExternalID, advertiserExternalID, publicationExternalID, impression,
                didTypeIdsBySystemName, deviceProperties, trackingDisabled, null, true, null);
    }

    /**
     * This is complicated version for redirection click urls. 
     * 2 URLs are passed in - @param targetUrl and @param redirectToUrl. Both urls will have macros expanded but then 
     * @param redirectToUrl will be also url escaped and used in @param targetUrl as %destination_escaped% replacement
     * 
     */
    public static String resolveMacros(String targetUrl, String publisherExternalID, String adSpaceExternalID, String creativeExternalID, String campaignExternalID,
            String advertiserExternalID, String publicationExternalID, Impression impression, Map<String, Long> didTypeIdsBySystemName, Map<String, String> deviceProperties,
            boolean trackingDisabled, String redirectToUrl, boolean doProcFinalDest, Boolean isTracked) {

        if (targetUrl == null) {
            return null;
        }

        if (redirectToUrl != null || doProcFinalDest) {

            String timestampString = String.valueOf(System.currentTimeMillis());
            targetUrl = StringUtils.replace(targetUrl, "%timestamp%", timestampString);

            targetUrl = StringUtils.replace(targetUrl, "%publication%", StringUtils.defaultString(adSpaceExternalID));
            targetUrl = StringUtils.replace(targetUrl, "%publisher_id%", StringUtils.defaultString(publisherExternalID));

            publicationExternalID = StringUtils.defaultString(publicationExternalID);
            targetUrl = StringUtils.replace(targetUrl, "%pid%", publicationExternalID);
            targetUrl = StringUtils.replace(targetUrl, "%publication_id%", publicationExternalID);

            targetUrl = StringUtils.replace(targetUrl, "%creative%", StringUtils.defaultString(creativeExternalID));
            targetUrl = StringUtils.replace(targetUrl, "%campaign%", StringUtils.defaultString(campaignExternalID));
            targetUrl = StringUtils.replace(targetUrl, "%advertiser%", StringUtils.defaultString(advertiserExternalID));
            targetUrl = StringUtils.replace(targetUrl, "%platform%", getDeviceOs(deviceProperties)); //IOS or ANDROID
            targetUrl = StringUtils.replace(targetUrl, "%device_type%", getDeviceType(deviceProperties)); //PHN or TAB

            targetUrl = StringUtils.replace(targetUrl, "%dtd%", trackingDisabled ? ONE_STRING : ZERO_STRING);

            targetUrl = resolveDeviceIdMacros(targetUrl, impression, didTypeIdsBySystemName);

            final String clickId;
            final String latitude;
            final String longitude;
            final String locsource;
            if (impression != null) {
                clickId = StringUtils.defaultString(impression.getExternalID()); // clickExternalID == impression.externalID
                latitude = impression.getLatitude() != null ? String.valueOf(impression.getLatitude()) : StringUtils.EMPTY;
                longitude = impression.getLongitude() != null ? String.valueOf(impression.getLongitude()) : StringUtils.EMPTY;
                String locationSource = impression.getLocationSource();
                if (LocationSource.EXPLICIT.name().equals(locationSource)) {
                    locsource = "1";
                } else if (LocationSource.DERIVED.name().equals(locationSource)) {
                    locsource = "2";
                } else {
                    locsource = StringUtils.EMPTY;
                }
            } else {
                clickId = StringUtils.EMPTY;
                latitude = StringUtils.EMPTY;
                longitude = StringUtils.EMPTY;
                locsource = StringUtils.EMPTY;
            }

            targetUrl = StringUtils.replace(targetUrl, "%click%", clickId);
            targetUrl = StringUtils.replace(targetUrl, "%latitude%", latitude);
            targetUrl = StringUtils.replace(targetUrl, "%longitude%", longitude);
            targetUrl = StringUtils.replace(targetUrl, "%locationtype%", locsource);

            if (isTracked != null && targetUrl.indexOf("%willAccount%") != -1) {
                targetUrl = StringUtils.replace(targetUrl, "%willAccount%", isTracked.booleanValue() ? "1" : "0");
            }

        }

        if (redirectToUrl != null) { // note the return below. idea is to allow no more since encoded urls can have these tokens
            try {
                redirectToUrl = resolveMacros(redirectToUrl, publisherExternalID, adSpaceExternalID, creativeExternalID, campaignExternalID, advertiserExternalID,
                        publicationExternalID, impression, didTypeIdsBySystemName, deviceProperties, trackingDisabled, null, doProcFinalDest, isTracked);
                return StringUtils.replace(targetUrl, "%destination_escaped%", URLEncoder.encode(redirectToUrl, "UTF-8"));
            } catch (UnsupportedEncodingException e) {// wont happen UTF-8 being valid
                throw new RuntimeException(e);
            }
        }

        return targetUrl;
    }

    private static String resolveDeviceIdMacros(String targetUrl, Impression impression, Map<String, Long> didTypeIdsBySystemName) {
        // NOTE: by this point it's assumed that any "promotions" have occurred.
        // That is, if there was any way to auto-derive dpid or odin-1, that has
        // already been done.  So we can just look for them directly.

        String dpid = getDeviceIdentifier(impression, DeviceIdentifierType.SYSTEM_NAME_DPID, didTypeIdsBySystemName);
        targetUrl = StringUtils.replace(targetUrl, "%dpid%", dpid);

        String idfa = getDeviceIdentifier(impression, DeviceIdentifierType.SYSTEM_NAME_IFA, didTypeIdsBySystemName);
        targetUrl = StringUtils.replace(targetUrl, "%ifa%", idfa);
        targetUrl = StringUtils.replace(targetUrl, "%idfa%", idfa);

        String idfa_sha1 = getDeviceIdentifier(impression, DeviceIdentifierType.SYSTEM_NAME_HIFA, didTypeIdsBySystemName);
        targetUrl = StringUtils.replace(targetUrl, "%hifa%", idfa_sha1);

        String idfa_md5 = getDeviceIdentifier(impression, DeviceIdentifierType.SYSTEM_NAME_IDFA_MD5, didTypeIdsBySystemName);
        targetUrl = StringUtils.replace(targetUrl, "%idfa_md5%", idfa_md5);

        String adid = getDeviceIdentifier(impression, DeviceIdentifierType.SYSTEM_NAME_ADID, didTypeIdsBySystemName);
        targetUrl = StringUtils.replace(targetUrl, "%adid%", adid);

        String adid_md5 = getDeviceIdentifier(impression, DeviceIdentifierType.SYSTEM_NAME_ADID_MD5, didTypeIdsBySystemName);
        targetUrl = StringUtils.replace(targetUrl, "%adid_md5%", adid_md5);

        String adid_sha1 = getDeviceIdentifier(impression, DeviceIdentifierType.SYSTEM_NAME_ADID_SHA1, didTypeIdsBySystemName);
        targetUrl = StringUtils.replace(targetUrl, "%adid_sha1%", adid_sha1);

        // Only raw IFDA/ADID into %device_id% plus legacy dpid
        String device_id = StringUtils.EMPTY;
        if (StringUtils.EMPTY != idfa) {
            device_id = idfa;
        } else if (StringUtils.EMPTY != adid) {
            device_id = adid;
        } else if (StringUtils.EMPTY != dpid) {
            device_id = dpid;
        }
        targetUrl = StringUtils.replace(targetUrl, "%device_id%", device_id);
        return targetUrl;
    }

    public static final String getDeviceIdentifier(Impression impression, String systemName, Map<String, Long> didTypeIdsBySystemName) {
        // Device identifiers are stored on the Impression by the respective
        // DeviceIdentifierType id.  This is done for efficiency (i.e. it's
        // a more compact serialized to use a low, easily bit-optimizable
        // long value than storing a length-first string).  The flip side is
        // that we can't hard-code DeviceIdentifierType ids across the code.
        // So we use the supplied systemName-to-id map to look them up.
        if (impression != null) {
            Long id = didTypeIdsBySystemName.get(systemName);
            if (id != null) {
                return StringUtils.defaultString(impression.getDeviceIdentifiers().get(id));
            } else {
                return StringUtils.EMPTY;
            }
        } else {
            return StringUtils.EMPTY;
        }
    }

    /**
     * Properties names are from Device Atlas (DDR)
     */
    private static final String getDeviceType(Map<String, String> deviceProperties) {
        if (deviceProperties != null) {
            String deviceType = StringUtils.EMPTY;
            if (ONE_STRING.equals(deviceProperties.get("isMobilePhone"))) {
                deviceType = "PHN";
            } else if (ONE_STRING.equals(deviceProperties.get("isTablet"))) {
                deviceType = "TAB";
            } else {
                deviceType = deviceProperties.get("model");
                if (deviceType == null) {
                    deviceType = StringUtils.EMPTY;
                }
            }
            return deviceType;
        } else {
            return StringUtils.EMPTY;
        }
    }

    private static final String getDeviceOs(Map<String, String> deviceProperties) {
        if (deviceProperties != null) {
            String deviceOs = deviceProperties.get("osName");
            if (deviceOs != null) {
                return deviceOs.toUpperCase();
            } else {
                return StringUtils.EMPTY;
            }
        } else {
            return StringUtils.EMPTY;
        }
    }

}
