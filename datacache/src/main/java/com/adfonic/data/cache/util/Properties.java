package com.adfonic.data.cache.util;

import java.util.HashMap;
import java.util.Map;

public class Properties {

    private Map<String, String> global;
    private Map<String, String> location;
    private Map<String, String> shard;
    private Map<String, String> server;

    public Properties(Map<String, String> global, Map<String, String> location, Map<String, String> shard, Map<String, String> server) {

        this.global = global;
        this.location = location;
        this.shard = shard;
        this.server = server;
    }

    public String getProperty(String propertyName) {
        String propertyValue = null;

        propertyValue = server.get(propertyName);
        if (propertyValue != null)
            return propertyValue;

        propertyValue = shard.get(propertyName);
        if (propertyValue != null)
            return propertyValue;

        propertyValue = location.get(propertyName);
        if (propertyValue != null)
            return propertyValue;

        propertyValue = global.get(propertyName);
        if (propertyValue != null)
            return propertyValue;

        return null;
    }

    public Map<String, String> getAll() {
        Map<String, String> all = new HashMap<String, String>();
        all.putAll(global);
        all.putAll(location);
        all.putAll(shard);
        all.putAll(server);
        return all;
    }
}
