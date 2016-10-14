package com.adfonic.util;

import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

/**
 * Device identifier utility methods
 */
public final class DeviceIdentifierUtils {

    /** Length of an iOS UDID */
    public static final int IOS_UDID_LENGTH = 40;

    /** Length of an Android ANDROID_ID */
    public static final int ANDROID_ID_LENGTH = 16;

    /** Pattern of a hyphenated IFA (UUID 8-4-4-4-12 hyphenation) */
    static final Pattern ALREADY_HYPHENATED_IFA_PATTERN = Pattern.compile("^[A-Fa-f0-9]{8}-[A-Fa-f0-9]{4}-[A-Fa-f0-9]{4}-[A-Fa-f0-9]{4}-[A-Fa-f0-9]{12}$");

    /** Length of a non-hyphenated IFA */
    static final int NON_HYPHENATED_IFA_LENGTH = 32;

    /** Adfonic salt for Device Insight Configuration **/
    static final String ADFONIC_ADTRUTH_SALT = "adf0n1cS41t4A6truth";

    private static final Pattern ANY_LOWER_PATTERN = Pattern.compile("\\p{javaLowerCase}");

    private DeviceIdentifierUtils() {
    }

    /**
     * Given an IFA, "normalize" it to its hyphenated form (SC-215)
     */
    static String normalizeIfa(String ifa) {
        if (ALREADY_HYPHENATED_IFA_PATTERN.matcher(ifa).matches()) {
            return ifa;
        } else {
            String bare = StringUtils.remove(ifa, '-');
            if (bare.length() != NON_HYPHENATED_IFA_LENGTH) {
                throw new IllegalArgumentException("Not a valid IFA: " + ifa + " (why wasn't the validation pattern enforced?)");
            }
            return bare.substring(0, 8) + "-" + bare.substring(8, 12) + "-" + bare.substring(12, 16) + "-" + bare.substring(16, 20) + "-" + bare.substring(20);
        }
    }

    private static String ensureUpperCase(String deviceId) {
        if (ANY_LOWER_PATTERN.matcher(deviceId).find()) {
            //LOG.warning("Forcing device-id(" + deviceId + ") to uppers");
            return deviceId.toUpperCase();
        }

        return deviceId;
    }

    /**
     * Normalize deviceId based on type.
     * 
     * @param deviceId
     *            non null device id
     * @param deviceIdTypeSystemName
     *            DeviceIdentifierType as system name. non null
     *
     * @return normalized base don type
     */
    public static String normalizeDeviceIdentifier(String deviceId, String deviceIdTypeSystemName) {
        return "ifa".equals(deviceIdTypeSystemName) ? ensureUpperCase(normalizeIfa(deviceId)) : deviceId.toLowerCase();
    }

}
