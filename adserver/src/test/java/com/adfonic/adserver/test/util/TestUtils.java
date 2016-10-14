package com.adfonic.adserver.test.util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;
import java.util.Map.Entry;

import net.anthavio.aspect.ApiPolicyOverride;

import com.adfonic.adserver.Parameters;

public class TestUtils {

    public static String createQueryString(Map<String, Object> queryMap) {
        StringBuilder sb = new StringBuilder("");
        if (queryMap != null) {
            for (Entry<String, Object> oneEntry : queryMap.entrySet()) {
                sb.append(oneEntry.getKey());
                sb.append("=");
                try {
                    sb.append(URLEncoder.encode((String) oneEntry.getValue(), "UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                sb.append("&");
            }
        }
        return sb.toString();
    }

    public static String createDiagnosticUrlParams(String adSpaceExternalId, Map<String, Object> queryMap) {

        StringBuilder sb = new StringBuilder("");
        if (queryMap != null) {
            sb.append("adSpaceId=");
            sb.append(adSpaceExternalId);
            sb.append("&");

            appendParams(sb, queryMap, Parameters.IP, "ipAddressCustom");
            appendParams(sb, queryMap, Parameters.TEST_MODE, "testMode");
            appendParams(sb, queryMap, Parameters.GENDER, "gender");
            appendParams(sb, queryMap, Parameters.MEDIUM, "medium");
            appendParams(sb, queryMap, Parameters.COUNTRY_CODE, "country");
            appendParams(sb, queryMap, Parameters.POSTAL_CODE, "postalCode");
            appendParams(sb, queryMap, Parameters.STATE, "state");
            appendParams(sb, queryMap, Parameters.DMA, "dma");
            appendParams(sb, queryMap, Parameters.TIME_ZONE, "timeZone");
            appendParams(sb, queryMap, Parameters.USER_LATITUDE, "latitude");
            appendParams(sb, queryMap, Parameters.USER_LONGITUDE, "longitude");
            appendParams(sb, queryMap, Parameters.TRACKING_ID, "trackingIdentifier");
            //TODO add age related params too
            appendParams(sb, queryMap, Parameters.LANGUAGE, "language");
            appendParams(sb, queryMap, "h.user-agent", "userAgentCustom");

        }
        return sb.toString();
    }

    @ApiPolicyOverride
    private static void appendParams(StringBuilder sb, Map<String, Object> queryMap, String sourceParam, String targetParam) {
        try {
            if (queryMap.get(sourceParam) != null) {
                sb.append(targetParam);
                sb.append("=");
                if (queryMap.get(sourceParam) != null) {
                    sb.append(URLEncoder.encode((String) queryMap.get(sourceParam), "UTF-8"));
                }
                sb.append("&");
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

}
