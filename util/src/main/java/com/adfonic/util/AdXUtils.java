package com.adfonic.util;

import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

public final class AdXUtils {
    private static final Pattern ADX_APPID_PATTERN = Pattern.compile("^[a-z.]+$", Pattern.CASE_INSENSITIVE);

    private AdXUtils() {
    }

    /**
     * Test an application ID for AdX validity
     *
     * @param value
     *            String to be validated
     * @return boolean true if valid, false otherwise
     */
    public static boolean isValidAdXApplicationID(String value) {
        return StringUtils.isNotBlank(value) && ADX_APPID_PATTERN.matcher(value).matches();
    }
}
