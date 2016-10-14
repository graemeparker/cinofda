package com.adfonic.util;

import java.net.InetAddress;

public class HostUtils {
    private static String hostName = null;
    private static String hostAddress = null;
    
    private HostUtils(){
    }
    
    static {
        try {
            InetAddress addr = InetAddress.getLocalHost();
            hostName = addr.getCanonicalHostName();
            hostAddress = addr.getHostAddress();
        } catch (java.net.UnknownHostException e) {
            throw new IllegalStateException("Failed to initialize HostUtils", e);
        }
    }

    public static String getHostName() {
        return hostName;
    }

    public static String getHostAddress() {
        return hostAddress;
    }

    public static String getHostString() {
        return hostName + "(" + hostAddress + ")";
    }
}
