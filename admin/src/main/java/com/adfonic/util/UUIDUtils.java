package com.adfonic.util;

import java.util.regex.Pattern;

public class UUIDUtils {
	protected static final Pattern UUID_PATTERN = Pattern.compile("^[0-9A-Fa-f]{8}-[0-9A-Fa-f]{4}-[0-9A-Fa-f]{4}-[0-9A-Fa-f]{4}-[0-9A-Fa-f]{12}$");
	
    public static boolean isUUID(String value) {
        return UUID_PATTERN.matcher(value).matches();
    }
}
