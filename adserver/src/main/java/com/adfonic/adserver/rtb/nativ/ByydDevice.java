package com.adfonic.adserver.rtb.nativ;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.adfonic.geo.Coordinates;

public class ByydDevice {

    public static final ByydDevice EMPTY = new ByydDevice();

    private String ip;

    private String userAgent;

    private Coordinates coordinates;

    // This is akin to Parameters.MCC_MNC (no dash, just MCC and MNC concatenated)
    private String mccMnc;

    // This is akin to Parameters.NETWORK_TYPE (i.e. "wifi" or 
    private String networkType;

    // This holds device identifiers by DeviceIdentifierType.systemName (i.e. "dpid", "ifa", "odin-1")
    private final Map<String, String> deviceIdentifiers = new HashMap<>();

    // NOT used in core rtb logic. Currently (temporarily?) used for dynamic integration type derivation
    private String os;

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public Coordinates getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(Coordinates coordinates) {
        this.coordinates = coordinates;
    }

    public String getNetworkType() {
        return networkType;
    }

    public void setNetworkType(String networkType) {
        this.networkType = networkType;
    }

    public String getMccMnc() {
        return mccMnc;
    }

    public void setMccMnc(String mccMnc) {
        this.mccMnc = mccMnc;
    }

    public Map<String, String> getDeviceIdentifiers() {
        return deviceIdentifiers;
    }

    public void setDeviceIdentifier(String deviceIdentifierTypeSystemName, String deviceIdentifier) {
        if (StringUtils.isNotEmpty(deviceIdentifier)) {
            deviceIdentifiers.put(deviceIdentifierTypeSystemName, deviceIdentifier);
        }
    }

    public String getOs() {
        return os;
    }

    public void setOs(String os) {
        this.os = os;
    }

}
