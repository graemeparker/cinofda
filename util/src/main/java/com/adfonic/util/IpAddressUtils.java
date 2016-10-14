package com.adfonic.util;

import java.net.InetAddress;

public class IpAddressUtils {
    
    private IpAddressUtils(){
    }
    
    /** Convert an IP address string (i.e. 123.45.67.89) to numeric form */
    public static long ipAddressToLong(String ipAddress) throws java.net.UnknownHostException {
        return bytesToLong(InetAddress.getByName(ipAddress).getAddress());
    }

    /** Convert an IP address byte array to numeric form */
    public static long bytesToLong(byte[] address) {
        long ipnum = 0;
        for (int i = 0; i < 4; ++i) {
            long y = address[i];
            if (y < 0) {
                y += 256; // convert signed to unsigned
            }
            ipnum += y << ((3 - i) * 8);
        }
        return ipnum;
    }

    /** Convert a numeric IP address value to string form */
    public static String longToIpAddress(long value) {
        return ((value >> 24) & 0xFF) + "." + ((value >> 16) & 0xFF) + "." + ((value >> 8) & 0xFF) + "." + (value & 0xFF);
    }
}
