package com.adfonic.dmp.cache;

public class KeyManager {
    
    private KeyManager(){
        // do nothing
    }

    public static String getKey(String deviceId, long deviceTypeId) {
        return deviceTypeId + "." + deviceId;
    }

    public static String getAudienceIdKey(long audienceId) {
        return "aud." + audienceId;
    }

    public static String getDeviceIdKeyForAudience(String deviceId, long deviceTypeId) {
        return "a." + deviceTypeId + "." + deviceId;
    }

    public static String getDeviceIdKeyForAudience(String deviceIdWithDeviceIdType) {
        return "a." + deviceIdWithDeviceIdType;
    }

    public static String getDeviceIdKeyForOptOut(String deviceIdWithDeviceIdType) {
        return "d.oo." + deviceIdWithDeviceIdType;
    }

    public static String getPropertyKey(String key) {
        return "prop." + key;
    }

}
