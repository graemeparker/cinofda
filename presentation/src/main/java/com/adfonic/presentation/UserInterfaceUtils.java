package com.adfonic.presentation;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Date;
import java.util.TimeZone;

import org.apache.commons.lang.StringUtils;

import com.adfonic.domain.Advertiser;
import com.adfonic.domain.Campaign;
import com.adfonic.util.DateUtils;
import com.adfonic.util.TimeZoneUtils;

public class UserInterfaceUtils {

    private static final String UTF_8_ENCODING = "utf-8";
    private static final int RADIX_16 = 16;
    
    private UserInterfaceUtils(){        
    }

    public static String colorGradient(String colorStart, String colorEnd, double percent) {
        int s, e, o;
        StringBuilder output = new StringBuilder();
        for (int rgbByte = 0; rgbByte < 3; rgbByte++) {
            s = Integer.parseInt(colorStart.substring(rgbByte * 2,rgbByte * 2 + 2), RADIX_16);
            e = Integer.parseInt(colorEnd.substring(rgbByte * 2,rgbByte * 2 + 2), RADIX_16);
            o = (int) ((e - s) * percent + s);
            String str = Integer.toHexString(o);
            if (str.length() == 1) { 
                output.append("0"); 
            }
            output.append(str);
        }
        return output.toString();
    }

    public static String getUICampaignStatus(Campaign c) {
        if (c == null) {
            return null;
        }
        
        if ((c.getStatus() == Campaign.Status.ACTIVE) && (!c.isCurrentlyActive())) {
            return "SCHEDULED";
        }
        
        return c.getStatus().toString();
    }

    public static String getUIAdvertiserStatus(Advertiser a){
        if(a==null) {
            return null;
        }
        
        if(a.getStatus() == Advertiser.Status.ACTIVE) {
            return "ACTIVE";
        }else{
            return "INACTIVE";
        }
    }

    public static Date exampleDate() {
        return DateUtils.getEndOfMonth(new Date(), TimeZoneUtils.getDefaultTimeZone());
    }

    public static Date exampleDate(TimeZone tz) {
        return DateUtils.getEndOfMonth(new Date(), tz);
    }

    public static String urlEncode(String input, String encoding) {
        String localEncoding = encoding;
        if (!StringUtils.isBlank(input)) {
            if (StringUtils.isBlank(localEncoding)) {
                localEncoding = UTF_8_ENCODING;
            }
            
            try {
                return URLEncoder.encode(input, localEncoding);
            } catch (UnsupportedEncodingException use) {
                //do nothing
            }
        }
        return "";
    }


}
