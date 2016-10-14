package com.adfonic.util;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

public class HttpUtils {

    private static final String CHARSET_UTF_8 = "utf-8";

    private HttpUtils() {
    }

    public static Cookie getCookieByName(HttpServletRequest request, String name) {
        if (name == null) {
            return null;
        }
        for (Cookie c : request.getCookies()) {
            if (name.equals(c.getName())) {
                return c;
            }
        }
        return null;
    }

    public static String urlEncode(String string) {
        try {
            return URLEncoder.encode(string, CHARSET_UTF_8);
        } catch (UnsupportedEncodingException uex) {
            throw new IllegalStateException(uex);
        }
    }

    public static String urlDecode(String string) {
        try {
            return URLDecoder.decode(string, CHARSET_UTF_8);
        } catch (UnsupportedEncodingException uex) {
            throw new IllegalStateException(uex);
        }
    }

    public static String encodeParams(Map<String, String> params) {
        if (params == null || params.isEmpty()) {
            return null;
        }
        StringBuilder buf = new StringBuilder();
        boolean first = true;
        for (Iterator<Map.Entry<String, String>> iter = params.entrySet().iterator(); iter.hasNext();) {
            final Map.Entry<String, String> entry = iter.next();
            if (first) {
                first = false;
            } else {
                buf.append('&');
            }
            try {
                buf.append(URLEncoder.encode(String.valueOf(entry.getKey()), CHARSET_UTF_8));
                buf.append('=');
                buf.append(URLEncoder.encode(String.valueOf(entry.getValue()), CHARSET_UTF_8));
            } catch (java.io.UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
        }
        return buf.toString();
    }

    public static Map<String, String> decodeParams(String encoded) {
        Map<String, String> params = new LinkedHashMap<String, String>();
        StringTokenizer tokenizer = new StringTokenizer(encoded, "&");
        while (tokenizer.hasMoreTokens()) {
            String nvp = tokenizer.nextToken();
            String[] toks = StringUtils.split(nvp, '=');
            if (toks.length == 2 && toks[1].length() > 0) {
                try {
                    params.put(URLDecoder.decode(toks[0], CHARSET_UTF_8), URLDecoder.decode(toks[1], CHARSET_UTF_8));
                } catch (java.io.UnsupportedEncodingException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return params;
    }

    public static List<NameValuePair> toNameValuePairList(Map<String, String> map) {
        List<NameValuePair> nvPairs = new ArrayList<NameValuePair>();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            nvPairs.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
        }
        return nvPairs;
    }

    public static Map<String, String> fromNameValuePairList(List<NameValuePair> nvPairs) {
        Map<String, String> map = new LinkedHashMap<String, String>();
        for (NameValuePair nvPair : nvPairs) {
            map.put(nvPair.getName(), nvPair.getValue());
        }
        return map;
    }
}
