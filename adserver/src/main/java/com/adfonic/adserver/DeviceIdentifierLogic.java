package com.adfonic.adserver;

import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;

import com.adfonic.adserver.logging.LoggingUtils;
import com.adfonic.domain.DeviceIdentifierType;
import com.adfonic.domain.cache.DomainCache;
import com.adfonic.domain.cache.dto.adserver.DeviceIdentifierTypeDto;
import com.adfonic.domain.cache.dto.adserver.PublicationTypeDto;
import com.adfonic.domain.cache.dto.adserver.adspace.AdSpaceDto;
import com.adfonic.util.DeviceIdentifierUtils;

/**
 * Logic related to working with device identifiers
 */
public class DeviceIdentifierLogic {

    private static final transient Logger LOG = Logger.getLogger(DeviceIdentifierLogic.class.getName());

    /**
     * Auto-promote device identifiers to other secure forms as applicable.
     * For example, d.android and d.udid may be "promoted" to d.dpid when
     * d.dpid isn't explicitly specified.  Likewise, d.android may be
     * promoted to d.odin-1.  And d.ifa gets promoted to d.hifa.
     * @param secureDeviceIdentifiers the map of device identifiers by type,
     * supplied by the publisher, made secure, to which we'll add values if
     * any promotions are applicable
     * @param deviceIdentifierTypeIdsBySystemName a map providing a lookup
     * from system name to DeviceIdentifierType id
     */
    public static void promoteDeviceIdentifiers(Map<Long, String> secureDeviceIdentifiers, Map<String, Long> deviceIdentifierTypeIdsBySystemName) {
        // First see if d.dpid was supplied
        Long dpidId = deviceIdentifierTypeIdsBySystemName.get(DeviceIdentifierType.SYSTEM_NAME_DPID);
        if (dpidId != null) { // would only be null if the db lacks required data
            // Check to see if dpid was already derived
            if (!secureDeviceIdentifiers.containsKey(dpidId)) {
                if (LOG.isLoggable(Level.FINE)) {
                    //LOG.fine("dpid not supplied, checking for possible promotions");
                    LoggingUtils.log(LOG, Level.FINE, null, null, DeviceIdentifierLogic.class, "promoteDeviceIdentifiers", "dpid not supplied, checking for possible promotions");
                }

                // Promote Android ID if supplied
                String secureAndroid = getSecureDeviceIdentifier(secureDeviceIdentifiers, DeviceIdentifierType.SYSTEM_NAME_ANDROID, deviceIdentifierTypeIdsBySystemName);
                if (StringUtils.isNotEmpty(secureAndroid)) {
                    // Since it's already secure (SHA1'd), we can set dpid to it directly
                    if (LOG.isLoggable(Level.FINE)) {
                        //LOG.fine("Promoting android to dpid");
                        LoggingUtils.log(LOG, Level.FINE, null, null, DeviceIdentifierLogic.class, "promoteDeviceIdentifiers", "Promoting android to dpid");
                    }
                    secureDeviceIdentifiers.put(dpidId, secureAndroid);
                } else {
                    // No Android ID, let's see if UDID was supplied
                    String secureUdid = getSecureDeviceIdentifier(secureDeviceIdentifiers, DeviceIdentifierType.SYSTEM_NAME_UDID, deviceIdentifierTypeIdsBySystemName);
                    if (StringUtils.isNotEmpty(secureUdid)) {
                        // Same deal here, since it's already SHA1'd, we can set dpid to it directly
                        if (LOG.isLoggable(Level.FINE)) {
                            //LOG.fine("Promoting udid to dpid");
                            LoggingUtils.log(LOG, Level.FINE, null, null, DeviceIdentifierLogic.class, "promoteDeviceIdentifiers", "Promoting udid to dpid");
                        }
                        secureDeviceIdentifiers.put(dpidId, secureUdid);
                    }
                }
            }
        }

        // See if odin-1 was supplied
        Long odin1Id = deviceIdentifierTypeIdsBySystemName.get(DeviceIdentifierType.SYSTEM_NAME_ODIN_1);
        if (odin1Id != null) { // would only be null if the db lacks required data
            // Check to see if odin-1 was already derived
            if (!secureDeviceIdentifiers.containsKey(odin1Id)) {
                if (LOG.isLoggable(Level.FINE)) {
                    //LOG.fine("odin-1 not supplied, checking for possible promotions");
                    LoggingUtils.log(LOG, Level.FINE, null, null, DeviceIdentifierLogic.class, "promoteDeviceIdentifiers", "odin-1 not supplied, checking for possible promotions");
                }

                // Android ID in its secure form is considered equivalent to ODIN-1
                String secureAndroid = getSecureDeviceIdentifier(secureDeviceIdentifiers, DeviceIdentifierType.SYSTEM_NAME_ANDROID, deviceIdentifierTypeIdsBySystemName);
                if (StringUtils.isNotEmpty(secureAndroid)) {
                    // Since it's already secure (SHA1'd), we can set odin-1 to it directly
                    if (LOG.isLoggable(Level.FINE)) {
                        //LOG.fine("Promoting android to odin-1");
                        LoggingUtils.log(LOG, Level.FINE, null, null, DeviceIdentifierLogic.class, "promoteDeviceIdentifiers", "Promoting android to odin-1");
                    }
                    secureDeviceIdentifiers.put(odin1Id, secureAndroid);
                }
            }
        }

        promoteIFA(secureDeviceIdentifiers, deviceIdentifierTypeIdsBySystemName);
        promoteADID(secureDeviceIdentifiers, deviceIdentifierTypeIdsBySystemName);
    }

    public static void promoteADID(Map<Long, String> deviceIdentifiers, Map<String, Long> deviceIdentifierTypeIdsBySystemName) {
        Long adidType = deviceIdentifierTypeIdsBySystemName.get(DeviceIdentifierType.SYSTEM_NAME_ADID);
        if (adidType == null) {
            return;
        }

        final String adid = deviceIdentifiers.get(adidType);
        if (StringUtils.isBlank(adid)) {
            return;
        }

        Long adidMd5Type = deviceIdentifierTypeIdsBySystemName.get(DeviceIdentifierType.SYSTEM_NAME_ADID_MD5);
        // Check to see if already promoted
        String adidMd5 = deviceIdentifiers.get(adidMd5Type);
        if (StringUtils.isBlank(adidMd5)) {
            adidMd5 = DigestUtils.md5Hex(adid);
            deviceIdentifiers.put(adidMd5Type, adidMd5);
        }

        Long sha1Type = deviceIdentifierTypeIdsBySystemName.get(DeviceIdentifierType.SYSTEM_NAME_ADID_SHA1);
        String sha1adid = deviceIdentifiers.get(sha1Type);
        if (StringUtils.isBlank(sha1adid)) {
            sha1adid = DigestUtils.sha1Hex(adid);
            deviceIdentifiers.put(sha1Type, sha1adid);
        }
    }

    public static void promoteIFA(Map<Long, String> deviceIdentifiers, Map<String, Long> deviceIdentifierTypeIdsBySystemName) {

        Long ifaType = deviceIdentifierTypeIdsBySystemName.get(DeviceIdentifierType.SYSTEM_NAME_IFA);
        if (ifaType == null) {
            return;
        }

        String ifa = deviceIdentifiers.get(ifaType);
        if (StringUtils.isBlank(ifa)) {
            return;
        }

        Long sha1Type = deviceIdentifierTypeIdsBySystemName.get(DeviceIdentifierType.SYSTEM_NAME_HIFA);
        Long idfaMd5Type = deviceIdentifierTypeIdsBySystemName.get(DeviceIdentifierType.SYSTEM_NAME_IDFA_MD5);

        promoteIFA_HIFA(deviceIdentifiers, ifa, sha1Type);
        promoteIFA_MD5(deviceIdentifiers, ifa, idfaMd5Type);
    }

    private static void promoteIFA_HIFA(Map<Long, String> deviceIdentifiers, String rawId, Long promotedType) {
        if (promotedType == null) {
            return;
        }

        // Check to see if already promoted
        String promotedId = deviceIdentifiers.get(promotedType);
        if (!StringUtils.isBlank(promotedId)) {
            return;
        }

        promotedId = DigestUtils.sha1Hex(rawId);
        deviceIdentifiers.put(promotedType, promotedId);
    }

    private static void promoteIFA_MD5(Map<Long, String> deviceIdentifiers, String rawId, Long promotedType) {
        if (promotedType == null) {
            return;
        }

        // Check to see if already promoted
        String promotedId = deviceIdentifiers.get(promotedType);
        if (!StringUtils.isBlank(promotedId)) {
            return;
        }

        //. The identiﬁers must use all capital letters before they are hashed. 
        // DCM wont recognize MD5-bashed values of lowercase IDs
        promotedId = DigestUtils.md5Hex(rawId.toUpperCase());
        deviceIdentifiers.put(promotedType, promotedId);
    }

    /**
     * Utility method to simplify getting at an already-derived secure
     * device identifier
     * @return an already-derived secure device identifier, if specified,
     * otherwise null
     */
    public static String getSecureDeviceIdentifier(Map<Long, String> secureDeviceIdentifiers, String systemName, Map<String, Long> deviceIdentifierTypeIdsBySystemName) {
        Long id = deviceIdentifierTypeIdsBySystemName.get(systemName);
        return id == null ? null : secureDeviceIdentifiers.get(id);
    }

    /**
     * Given a raw tracking id (i.e. the value of "r.id"), determine whether
     * it represents a device identifier (as opposed to some other random value).
     * @param rawTrackingId the raw tracking id (i.e. value of ("r.id")
     * @param context the targeting context
     * @return the respective DeviceIdentifierType if determined, otherwise null
     */
    public static DeviceIdentifierTypeDto determineDeviceIdentifierTypeFromRawTrackingId(String rawTrackingId, AdSpaceDto adSpace, DomainCache domainCache) {
        if (adSpace == null) {
            if (LOG.isLoggable(Level.INFO)) {
                //LOG.info("No way to determine DeviceIdentifierType without a defined AdSpace");
                LoggingUtils.log(LOG, Level.INFO, null, null, DeviceIdentifierLogic.class, "determineDeviceIdentifierTypeFromRawTrackingId",
                        "No way to determine DeviceIdentifierType without a defined AdSpace");
            }
            return null; // Without knowing the publication type there's not much we can do
        }

        DeviceIdentifierTypeDto deviceIdentifierType = null;
        PublicationTypeDto publicationType = domainCache.getPublicationTypeById(adSpace.getPublication().getPublicationTypeId());
        if (SystemName.IPHONE_APP.equals(publicationType.getSystemName()) || SystemName.IPAD_APP.equals(publicationType.getSystemName())) {
            // Let's assume it's an iOS UDID
            deviceIdentifierType = domainCache.getDeviceIdentifierTypeBySystemName(DeviceIdentifierType.SYSTEM_NAME_IFA);
        } else if (SystemName.ANDROID_APP.equals(publicationType.getSystemName())) {
            // Let's assume it's an Android Device ID
            deviceIdentifierType = domainCache.getDeviceIdentifierTypeBySystemName(DeviceIdentifierType.SYSTEM_NAME_ADID);
        } else if (SystemName.OTHER_APP.equals(publicationType.getSystemName())) {
            // It could be a UDID or an Android Device ID, or something else.
            // We'll do a quick & dirty length check here, and full pattern
            // based validation will confirm in a later step below.
            if (rawTrackingId.length() == DeviceIdentifierUtils.IOS_UDID_LENGTH) {
                // Assume for now that it's an iOS UDID
                deviceIdentifierType = domainCache.getDeviceIdentifierTypeBySystemName(DeviceIdentifierType.SYSTEM_NAME_UDID);
            } else if (rawTrackingId.length() == DeviceIdentifierUtils.ANDROID_ID_LENGTH) {
                // Assume for now that it's an Android Device ID
                deviceIdentifierType = domainCache.getDeviceIdentifierTypeBySystemName(DeviceIdentifierType.SYSTEM_NAME_ANDROID);
            }
        }

        if (LOG.isLoggable(Level.FINE)) {
            //LOG.fine("PublicationType is " + publicationType.getSystemName() + ", determined DeviceIdentifierType: " + (deviceIdentifierType == null ? "null" : deviceIdentifierType.getSystemName()));
            LoggingUtils.log(
                    LOG,
                    Level.FINE,
                    null,
                    null,
                    DeviceIdentifierLogic.class,
                    "determineDeviceIdentifierTypeFromRawTrackingId",
                    "PublicationType is " + publicationType.getSystemName() + ", determined DeviceIdentifierType: "
                            + (deviceIdentifierType == null ? "null" : deviceIdentifierType.getSystemName()));
        }

        // If the DeviceIdentifierType has a validation regex, make sure it matches
        if (deviceIdentifierType != null && deviceIdentifierType.getValidationPattern() != null && !deviceIdentifierType.getValidationPattern().matcher(rawTrackingId).matches()) {
            if (LOG.isLoggable(Level.INFO)) {
                //LOG.info("Tracking id (" + rawTrackingId + ") doesn't match validation pattern for " + deviceIdentifierType.getSystemName() + ": " + deviceIdentifierType.getValidationPattern());
                LoggingUtils.log(LOG, Level.INFO, null, null, DeviceIdentifierLogic.class, "determineDeviceIdentifierTypeFromRawTrackingId", "Tracking id (" + rawTrackingId
                        + ") doesn't match validation pattern for " + deviceIdentifierType.getSystemName() + ": " + deviceIdentifierType.getValidationPattern());
            }
            return null;
        } else {
            return deviceIdentifierType;
        }
    }

    /**
     * Enforce the device identifier blacklist by removing any of the
     * supplied device identifiers that are blacklisted
     * @param derivedDeviceIdentifiers the device identifiers derived for
     * the given ad request, as supplied by the publisher and promoted
     * as applicable
     * @param domainCache the current DomainCache
     */
    public static void enforceBlacklist(Map<Long, String> derivedDeviceIdentifiers, DomainCache domainCache) {
        for (Iterator<Map.Entry<Long, String>> iter = derivedDeviceIdentifiers.entrySet().iterator(); iter.hasNext();) {
            Map.Entry<Long, String> entry = iter.next();
            if (domainCache.isDeviceIdentifierBlacklisted(entry.getKey(), entry.getValue())) {
                if (LOG.isLoggable(Level.INFO)) {
                    //LOG.info("Removing blacklisted device identifier: " + entry.getKey() + "/" + entry.getValue());
                    LoggingUtils.log(LOG, Level.INFO, null, null, DeviceIdentifierLogic.class, "enforceBlacklist", "Removing blacklisted device identifier: " + entry.getKey()
                            + "/" + entry.getValue());
                }
                iter.remove();
            }
        }
    }
}
