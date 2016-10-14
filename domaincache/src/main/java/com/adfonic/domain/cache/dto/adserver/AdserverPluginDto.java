package com.adfonic.domain.cache.dto.adserver;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.adfonic.domain.cache.dto.BusinessKeyDto;

public class AdserverPluginDto extends BusinessKeyDto {
    private static final long serialVersionUID = 3L;

    private String systemName;
    private boolean enabled;
    private Long expectedResponseTimeMillis;
    private final Map<String,String> properties = new HashMap<String,String>();

    public String getSystemName() {
        return systemName;
    }
    public void setSystemName(String systemName) {
        this.systemName = systemName;
    }

    public boolean isEnabled() {
        return enabled;
    }
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Long getExpectedResponseTimeMillis() {
        return expectedResponseTimeMillis;
    }
    public void setExpectedResponseTimeMillis(Long expectedResponseTimeMillis) {
        this.expectedResponseTimeMillis = expectedResponseTimeMillis;
    }

    public Map<String,String> getProperties() {
        return properties;
    }
    
    public String getProperty(String name) {
        return properties.get(name);
    }
    
    public void setProperty(String name, String value) {
        properties.put(name, value);
    }

    public String getProperty(String name, String defaultValue) {
        return StringUtils.defaultString(properties.get(name), defaultValue);
    }
    
    public String getPropertyRequired(String name) {
        String value = properties.get(name);
        if (value != null) {
            return value;
        } else {
            throw new PropertyNotDefinedException(name);
        }
    }

    public static class PropertyNotDefinedException extends RuntimeException {
        private PropertyNotDefinedException(String name) {
            super(name);
        }
    }
}
