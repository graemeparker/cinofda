package com.adfonic.weve;

import java.util.List;
import java.util.Set;

public class WeveUtil {
    
    private WeveUtil(){        
    }

    public static String normalizeDeviceIdList(List<String> deviceIds) {
        if (deviceIds.isEmpty()) {
            return "";
        } else {
            StringBuilder deviceIdBuffer = new StringBuilder(deviceIds.size());
            for (String entry : deviceIds) {
                deviceIdBuffer.append(entry)
                .append("|");
            }
            deviceIdBuffer.deleteCharAt(deviceIdBuffer.length() - 1);
            return deviceIdBuffer.toString();
        }
    }
    
    
    /**
     * Building the weve id pipe separated list for weve db stored proc.
     */
    public static String normalizeWeveIdList(Set<Long> weveIds) {
        if (weveIds.isEmpty()) {
            return "";
        } else {
            StringBuilder weveIdBuffer = new StringBuilder(weveIds.size());
            for (Long entry : weveIds) {
                weveIdBuffer.append(String.valueOf(entry))
                .append("|");
            }
            weveIdBuffer.deleteCharAt(weveIdBuffer.length() - 1);
            return weveIdBuffer.toString();
        }
    }
    
    public static String printableDeviceIds(List<String> deviceIds) {
        if (deviceIds.isEmpty()) {
            return "null";
        } else {
            StringBuilder sbuf = new StringBuilder();
            for (String deviceIdTildaType : deviceIds) {
                String[] splitString = deviceIdTildaType.split("~");
                sbuf.append(splitString[0])
                .append(", ");
            }
            sbuf.deleteCharAt(sbuf.length() - 1);
            sbuf.deleteCharAt(sbuf.length() - 1);
            return sbuf.toString();
        }
    }
    
    /**
     * Printable Weve Ids - for logging only.
     */
    public static String printableWeveIds(Set<Long> weveIds) {
        if (weveIds.isEmpty()) {
            return "null";
        } else {
            StringBuilder sbuf = new StringBuilder();
            for (Long weveId : weveIds) {
                sbuf.append(weveId)
                .append(", ");
            }
            sbuf.deleteCharAt(sbuf.length() - 1);
            return sbuf.toString();
        }
   }
}
