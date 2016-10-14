package com.adfonic.adserver.impl;

import java.util.Map;
import java.util.Set;

import com.adfonic.adserver.CreativeEliminatedReason;
import com.adfonic.adserver.TargetingContext;
import com.adfonic.adserver.TargetingEventListener;
import com.adfonic.domain.ConnectionType;
import com.adfonic.domain.DestinationType;
import com.adfonic.domain.Feature;
import com.adfonic.domain.cache.dto.adserver.BrowserDto;
import com.adfonic.domain.cache.dto.adserver.IntegrationTypeDto;
import com.adfonic.domain.cache.dto.adserver.ModelDto;
import com.adfonic.domain.cache.dto.adserver.OperatorDto;
import com.adfonic.domain.cache.dto.adserver.PlatformDto;
import com.adfonic.domain.cache.dto.adserver.adspace.AdSpaceDto;
import com.adfonic.domain.cache.dto.adserver.creative.CreativeDto;
import com.adfonic.domain.cache.dto.adserver.creative.SegmentDto;
import com.adfonic.util.Subnet;

/**
 * Device features related targeting checks
 * 
 * Dissected from massive BasicTargetingEngineImpl
 * 
 * @author mvanek
 *
 */
public class DeviceFeaturesTargetingChecks {

    //    private final Logger LOG = Logger.getLogger(getClass().getName());

    //    /**
    //     * Pattern used to pick the Android OS version out of a User-Agent
    //     */
    //    private static final Pattern ANDROID_UA_OS_VERSION_PATTERN = Pattern.compile("\\bAndroid (\\d+\\.\\d+)");
    //
    //    /**
    //     * Pattern used to pick out only the major/minor version for parsing
    //     */
    //    private static final Pattern MAJOR_MINOR_VERSION_PATTERN = Pattern.compile("^(\\d+\\.\\d+)");

    /**
     * TargetingContext attribute used by isAndroid
     */
    static final String IS_ANDROID = "BasicTargetingEngineImpl.isAndroid";

    /**
     * TargetingContext attribute used by getAndroidOsVersion
     */
    static final String ANDROID_OS_VERSION = "BasicTargetingEngineImpl.androidOsVersion";

    /**
     * TargetingContext attribute used by isSmsOk
     */
    static final String SMS_OK = "BasicTargetingEngineImpl.smsOk";

    static CreativeEliminatedReason checkVendorsAndDevice(AdSpaceDto adSpace, TargetingContext context, CreativeDto creative, SegmentDto segment, ModelDto model,
            TargetingEventListener listener) {
        // Segment.vendors and Segment.models
        boolean filterVendors = !segment.getVendorIds().isEmpty();
        boolean filterModels = !segment.getModelIds().isEmpty();
        if (filterVendors && filterModels) {
            // When the segment has both vendors and models listed, we need
            // to treat them as an "OR" scenario.
            boolean vendorPasses = model != null && model.getVendor() != null && segment.getVendorIds().contains(model.getVendor().getId());
            boolean modelPasses = model != null && segment.getModelIds().contains(model.getId());
            if (!vendorPasses && !modelPasses) {
                if (listener != null) {
                    listener.creativeEliminated(adSpace, context, creative, CreativeEliminatedReason.DeviceVendorModelMismatch, "vendor/model");
                }
                return CreativeEliminatedReason.DeviceVendorModelMismatch;
            }
        } else if (filterVendors) {
            if (model == null || model.getVendor() == null || !segment.getVendorIds().contains(model.getVendor().getId())) {
                if (listener != null) {
                    listener.creativeEliminated(adSpace, context, creative, CreativeEliminatedReason.DeviceVendorMismatch, "vendor");
                }
                return CreativeEliminatedReason.DeviceVendorMismatch;
            }
        } else if (filterModels) {
            if (model == null || !segment.getModelIds().contains(model.getId())) {
                if (listener != null) {
                    listener.creativeEliminated(adSpace, context, creative, CreativeEliminatedReason.DeviceModelMismatch, "model");
                }
                return CreativeEliminatedReason.DeviceModelMismatch;
            }
        }
        return null;
    }

    static CreativeEliminatedReason checkDeviceGroup(AdSpaceDto adSpace, TargetingContext context, CreativeDto creative, SegmentDto segment, ModelDto model,
            TargetingEventListener listener) {
        if (creative.getSegment().getDeviceGroupIds().isEmpty()) {
            //No Device group targeting so its eligible
            return null;
        }
        if (model == null) {
            //can not derive model, that means cant determine its device group, so do not server on the 
            //campaign where device group targeting exists
            if (listener != null) {
                listener.creativeEliminated(adSpace, context, creative, CreativeEliminatedReason.noDeviceInRequestForDeviceGroupTargetedCampaign,
                        "Can not determine device group/device, so cant serve the campaign which are targeting device groups");
            }
            return CreativeEliminatedReason.noDeviceInRequestForDeviceGroupTargetedCampaign;
        }
        if (!creative.getSegment().getDeviceGroupIds().contains(model.getDeviceGroupId())) {
            //targeted device group list do not have current request model
            if (listener != null) {
                listener.creativeEliminated(adSpace, context, creative, CreativeEliminatedReason.deviceGroupDidNotMatch, "DeviceGroup " + model.getDeviceGroupId() + " not in "
                        + creative.getSegment().getDeviceGroupIds());
            }
            return CreativeEliminatedReason.deviceGroupDidNotMatch;
        }
        return null;
    }

    static CreativeEliminatedReason checkPlatformModel(AdSpaceDto adSpace, TargetingContext context, CreativeDto creative, SegmentDto segment, PlatformDto platform,
            ModelDto model, TargetingEventListener listener) {
        if (!segment.getPlatformIds().isEmpty() && !segment.getPlatformIds().contains(platform.getId())) {
            if (listener != null) {
                listener.creativeEliminated(adSpace, context, creative, CreativeEliminatedReason.DevicePlatformMismatch,
                        "Bid Platform " + platform.getId() + " vs " + segment.getPlatformIds());
            }
            return CreativeEliminatedReason.DevicePlatformMismatch;
        } else if (model != null && !segment.getExcludedModelIds().isEmpty() && segment.getExcludedModelIds().contains(model.getId())) {
            if (listener != null) {
                listener.creativeEliminated(adSpace, context, creative, CreativeEliminatedReason.DeviceModelExcluded,
                        "Device model " + model.getId() + " blacklisted: " + segment.getExcludedModelIds());
            }
            return CreativeEliminatedReason.DeviceModelExcluded;
        } else {
            return null;
        }
    }

    static CreativeEliminatedReason checkBrowsers(AdSpaceDto adSpace, TargetingContext context, CreativeDto creative, SegmentDto segment, TargetingEventListener listener,
            Map<Long, Boolean> segmentIdBrowserEligibilityMap) {
        // Segment.browsers...if non-empty, all of them must match
        if (!segment.getBrowserIds().isEmpty()) {
            boolean allMatched = true;
            Boolean cachedBrowserResult;
            for (Long browserId : segment.getBrowserIds()) {
                cachedBrowserResult = segmentIdBrowserEligibilityMap.get(browserId);
                if (cachedBrowserResult == null) {
                    BrowserDto browser = context.getDomainCache().getBrowserById(browserId);
                    if (!browser.isMatch(context)) {
                        allMatched = false;
                        segmentIdBrowserEligibilityMap.put(browserId, false);
                        break;
                    }
                    segmentIdBrowserEligibilityMap.put(browserId, true);
                } else {
                    if (!cachedBrowserResult) {
                        allMatched = false;
                        break;
                    }
                }
            }
            if (!allMatched) {
                if (listener != null) {
                    listener.creativeEliminated(adSpace, context, creative, CreativeEliminatedReason.BrowsersMismatch, "Not target browser: " + segment.getBrowserIds()
                            + " & Segment marked: " + segment.getId());
                }
                return CreativeEliminatedReason.BrowsersMismatch;
            }
        }
        return null;
    }

    static CreativeEliminatedReason checkConnectionType(AdSpaceDto adSpace, TargetingContext context, CreativeDto creative, SegmentDto segment, OperatorDto operator,
            TargetingEventListener listener) {
        CreativeEliminatedReason checkResult = null;

        // Segment.connectionType
        ConnectionType ct = segment.getConnectionType();
        boolean allowOperator = ct.isSet(ConnectionType.OPERATOR.bitValue());
        boolean allowWifi = ct.isSet(ConnectionType.WIFI.bitValue());

        // If operator is not empty we will check operator lists
        if (operator != null) {

            boolean isMobileOperator = operator.isMobileOperator();

            // if ((operator is NOT a mobile operator AND wifi operators are not allowed) OR (operator is a mobile operator AND mobile operators are not allowed))
            if ((!isMobileOperator && !allowWifi) || (isMobileOperator && !allowOperator)) {
                if (listener != null) {
                    listener.creativeEliminated(adSpace, context, creative, CreativeEliminatedReason.ConnectionTypeMismatch, "Device connection: "
                            + (isMobileOperator ? "operator" : "wifi") + " & Segment marked: " + segment.getId());
                }
                checkResult = CreativeEliminatedReason.ConnectionTypeMismatch;
            } else if (operator != null) { // If (operator is not empty) we try to find it in the segmen operator list
                if (isMobileOperator) { // Logic for mobile operators
                    if (allowOperator && !segment.getMobileOperatorIds().isEmpty()) {
                        boolean containsOperator = segment.getMobileOperatorIds().contains(operator.getId());
                        boolean isWhitelist = segment.getMobileOperatorListIsWhitelist();
                        checkResult = checkOperatorList(adSpace, context, creative, segment, operator, listener, containsOperator, isWhitelist);
                    }
                } else { // Logic for ISP operators
                    if (allowWifi && !segment.getIspOperatorIds().isEmpty()) {
                        boolean containsOperator = segment.getIspOperatorIds().contains(operator.getId());
                        boolean isWhitelist = segment.getIspOperatorListIsWhitelist();
                        checkResult = checkOperatorList(adSpace, context, creative, segment, operator, listener, containsOperator, isWhitelist);
                    }
                }
            }
        } else if (!allowWifi) {
            if (listener != null) {
                listener.creativeEliminated(adSpace, context, creative, CreativeEliminatedReason.ConnectionTypeMismatch,
                        "Device connection: wifi & Segment marked: " + segment.getId());
            }
            checkResult = CreativeEliminatedReason.ConnectionTypeMismatch;
        }

        return checkResult;
    }

    private static CreativeEliminatedReason checkOperatorList(AdSpaceDto adSpace, TargetingContext context, CreativeDto creative, SegmentDto segment, OperatorDto operator,
            TargetingEventListener listener, boolean containsOperator, boolean isWhitelist) {
        CreativeEliminatedReason checkResult = null;

        if (isWhitelist) {
            // Whitelist: If operators list is non-empty, operator must be in it
            if (!containsOperator) {
                // OperatorDto isn't supported
                if (listener != null) {
                    listener.creativeEliminated(adSpace, context, creative, CreativeEliminatedReason.OperatorNotWhitelisted, "Operator not whitelisted: " + operator.getName()
                            + " & Segment marked: " + segment.getId());
                }
                checkResult = CreativeEliminatedReason.OperatorNotWhitelisted;
            }
        } else {
            // Blacklist: If operators list is non-empty, operator must not be in it
            if (containsOperator) {
                if (listener != null) {
                    listener.creativeEliminated(adSpace, context, creative, CreativeEliminatedReason.OperatorBlacklisted, "Operator blacklisted: " + operator.getName()
                            + " & Segment marked: " + segment.getId());
                }
                checkResult = CreativeEliminatedReason.OperatorBlacklisted;
            }
        }

        return checkResult;
    }

    static CreativeEliminatedReason checkIpAddress(AdSpaceDto adSpace, TargetingContext context, CreativeDto creative, SegmentDto segment, TargetingEventListener listener) {
        // Segment.ipAddresses ...which we've pre-parsed into subnets
        Set<Subnet> subnets = context.getAdserverDomainCache().getSubnetsBySegmentId(segment.getId());
        Long ipAddressValue = null;
        if (subnets != null && !subnets.isEmpty()) {
            ipAddressValue = context.getAttribute(TargetingContext.IP_ADDRESS_VALUE);
            boolean found = false;
            // Make sure one of the allowed subnets contains the IP address
            for (Subnet subnet : subnets) {
                if (subnet.contains(ipAddressValue)) {
                    found = true;
                    break;
                }
            }

            // This logic can be expressed using a !XOR operation using boolean values for IP is found and segment's white or black listing value
            //  Found !XOR  isWhitelist = isEligible
            //  true        true        = true
            //  true        false       = false
            //  false       true        = false
            //  false       false       = true
            if (!(found ^ segment.isIpAddressesListWhitelist()) == false) {
                // It wasn't in any of the allowed subnets
                CreativeEliminatedReason elimination = segment.isIpAddressesListWhitelist() ? CreativeEliminatedReason.IpNotWhitelisted : CreativeEliminatedReason.IpIsBlacklisted;
                if (listener != null) {
                    listener.creativeEliminated(adSpace, context, creative, elimination, "IP not allowed " + ipAddressValue);
                }
                return elimination;
            }
        }
        return null;
    }

    static boolean checkSmsSupport(AdSpaceDto adSpace, TargetingContext context, CreativeDto creative, TargetingEventListener listener) {
        if (DestinationType.SMS.equals(creative.getDestination().getDestinationType()) && !isSmsOk(context)) {
            if (listener != null) {
                listener.creativeEliminated(adSpace, context, creative, CreativeEliminatedReason.smsNotPresent, "!sms");
            }
            return false;
        }
        return true;
    }

    /**
     * Determine whether or not the integration type supports SMS
     * This method "caches" the result so we don't calculate it more than
     * once per ad request.
     */
    static boolean isSmsOk(TargetingContext context) {
        Boolean smsOk = context.getAttribute(SMS_OK, Boolean.class);
        if (smsOk == null) {
            // Lazily derive whether or not SMS is supported for the given IntegrationType
            IntegrationTypeDto integrationType = context.getAttribute(TargetingContext.INTEGRATION_TYPE);
            smsOk = integrationType != null && integrationType.getSupportedFeatures().contains(Feature.SMS);
            context.setAttribute(SMS_OK, smsOk);
        }
        return smsOk;
    }

    static CreativeEliminatedReason checkCapabilities(AdSpaceDto adSpace, TargetingContext context, CreativeDto creative, SegmentDto segment, Set<Long> deviceCapabilityIds,
            TargetingEventListener listener) {
        // Segment.capabilityMap
        boolean capabilitiesMatched = true;
        long segmentCapabilityId = -1;
        for (Map.Entry<Long, Boolean> entry : segment.getCapabilityIdMap().entrySet()) {
            segmentCapabilityId = entry.getKey();
            Boolean required = entry.getValue();
            if (required == null) {
                if (listener != null) {
                    listener.creativeEliminated(adSpace, context, creative, CreativeEliminatedReason.CapabilityNotRequired, "Device Capability required is null: "
                            + segmentCapabilityId);
                }
                return CreativeEliminatedReason.CapabilityNotRequired;
            }
            boolean hasCapability = deviceCapabilityIds.contains(segmentCapabilityId);
            if ((required && !hasCapability) || (!required && hasCapability)) {
                capabilitiesMatched = false;
                break;
            }
        }
        if (!capabilitiesMatched) {
            if (listener != null) {
                listener.creativeEliminated(adSpace, context, creative, CreativeEliminatedReason.CapabilityMismatch, "Device capabilities " + deviceCapabilityIds + " are missing "
                        + segmentCapabilityId);
            }
            return CreativeEliminatedReason.CapabilityMismatch;
        }
        return null;
    }

    /**
     * Make sure that if the creative's destination is
     * click-to-call, that the viewer's device supports it
     */
    static boolean checkClickToCallSupport(AdSpaceDto adSpace, TargetingContext context, CreativeDto creative, Map<String, String> deviceProps, TargetingEventListener listener) {
        if (DestinationType.CALL.equals(creative.getDestination().getDestinationType()) && !"1".equals(deviceProps.get("uriSchemeTel"))) {
            if (listener != null) {
                listener.creativeEliminated(adSpace, context, creative, CreativeEliminatedReason.notClickToCallDevice, "Device can't do click-to-call");
            }
            return false;
        }
        return true;
    }

    /**
     * mvanek - It is year 2015 so ignore prehistoric Android version...  
     * 
     * Filter out any animated creatives when:
     * 1) The device is an Android one
     * 2) The user agent does not indicate an osVersion >= 2.2
    static boolean checkAnimated(AdSpaceDto adSpace, TargetingContext context, CreativeDto creative, ModelDto model, Map<String, String> deviceProps,
            TargetingEventListener listener) {
        if (creative.isAnimated() && isAndroid(model, context) && getAndroidOsVersion(deviceProps, context) < 2.2) {
            // Animated content not allowed pre-2.2 on Android
            if (listener != null) {
                listener.creativeEliminated(adSpace, context, creative, CreativeEliminatedReason.animatedCreativeOnOldAndroid, "android^animated");
            }
            return false;
        }
        return true;
    }
     */
    /**
     * Determine whether or not the model is on the Android platform.
     * This method "caches" the result so we don't calculate it more than
     * once per ad request.
    static boolean isAndroid(ModelDto model, TargetingContext context) {
        Boolean yesNo = context.getAttribute(IS_ANDROID, Boolean.class);
        if (yesNo == null) {
            // See if the model is an Android handset
            yesNo = model.getPlatforms().contains(context.getDomainCache().getPlatformBySystemName("android"));
            context.setAttribute(IS_ANDROID, yesNo);
        }
        return yesNo;
    }
    */

    /**
     * Determine the OS version for a device known to be Android.
     * This method "caches" the result so we don't calculate it more than
     * once per ad request.
    static double getAndroidOsVersion(Map<String, String> deviceProps, TargetingContext context) {
        Double osVersion = context.getAttribute(ANDROID_OS_VERSION);
        if (osVersion == null) {
            String osVersionString = deviceProps.get("osVersion");
            if (StringUtils.isEmpty(osVersionString)) {
                // Bugzilla 1510
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.fine("isAndroid but no osVersion in device properties...falling back on User-Agent check");
                }
                // Fall back on trying to pick it out of the User-Agent
                String userAgent = context.getEffectiveUserAgent();
                Matcher matcher = ANDROID_UA_OS_VERSION_PATTERN.matcher(userAgent);
                if (matcher.find()) {
                    osVersionString = matcher.group(1);
                    if (LOG.isLoggable(Level.FINE)) {
                        LOG.fine("Derived osVersion from User-Agent: " + osVersionString);
                    }
                }
            }

            if (StringUtils.isNotEmpty(osVersionString)) {
                try {
                    osVersion = Double.parseDouble(osVersionString);
                } catch (Exception e) {
                    // If that failed, it's likely that DeviceAtlas gave us a
                    // version value such as "4.0.1" or something like that.
                    // Extract the major/minor version only so we can parse it.
                    Matcher matcher = MAJOR_MINOR_VERSION_PATTERN.matcher(osVersionString);
                    if (matcher.find()) {
                        osVersion = Double.parseDouble(matcher.group(1));
                    }
                }
            }

            if (osVersion == null) {
                // It's either null or non-numeric or something, just default to uber-low
                osVersion = -1.0;
            }
            context.setAttribute(ANDROID_OS_VERSION, osVersion);
        }
        return osVersion;
    }
    */
}
