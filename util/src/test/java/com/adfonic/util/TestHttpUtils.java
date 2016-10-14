package com.adfonic.util;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

public class TestHttpUtils {
    @Test
    public void test() {
        Map<String, String> map = new HashMap<String, String>();
        map.put("123", "howdy");
        map.put("hey", new java.math.BigDecimal("123.456789").toString());
        map.put("what's", "up");
        map.put("3.0", "4.5");
        map.put("true", "not");
        String encoded = HttpUtils.encodeParams(map);
        Map<String, String> decoded = HttpUtils.decodeParams(encoded);
        assertEquals(map.size(), decoded.size());
        for (Map.Entry<String, String> entry : map.entrySet()) {
            assertEquals(entry.getValue(), decoded.get(entry.getKey()));
        }
    }
}
