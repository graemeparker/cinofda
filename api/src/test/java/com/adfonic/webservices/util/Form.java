package com.adfonic.webservices.util;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class Form {

    private Map<String, String> map = new LinkedHashMap<String, String>();

    public void set(String key, Object value) {
        map.put(key, value.toString());
    }


    public Set<Entry<String, String>> getEntries() {
        return map.entrySet();
    }
}
