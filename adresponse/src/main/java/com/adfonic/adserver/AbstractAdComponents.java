package com.adfonic.adserver;

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.lang.ClassUtils;

import com.adfonic.domain.DestinationType;

public abstract class AbstractAdComponents implements java.io.Serializable, AdComponents {
    private static final long serialVersionUID = 1L;
    private String format;
    private DestinationType destinationType;
    private String destinationUrl;
    private Map<String, Map<String, String>> components = new LinkedHashMap<String, Map<String, String>>();

    public AbstractAdComponents() {
    }

    public AbstractAdComponents(AdComponents other) {
        if (other != null) {
            this.format = other.getFormat();
            this.destinationType = other.getDestinationType();
            this.destinationUrl = other.getDestinationUrl();
            for (Map.Entry<String, Map<String, String>> entry : other.getComponents().entrySet()) {
                Map<String, String> component = new LinkedHashMap<String, String>();
                component.putAll(entry.getValue());
                this.components.put(entry.getKey(), component);
            }
        }
    }

    /** @return the Format.systemName that is being used for this ad */
    @Override
    public String getFormat() {
        return format;
    }

    @Override
    public void setFormat(String format) {
        this.format = format;
    }

    @Override
    public DestinationType getDestinationType() {
        return destinationType;
    }

    @Override
    public void setDestinationType(DestinationType destinationType) {
        this.destinationType = destinationType;
    }

    @Override
    public String getDestinationUrl() {
        return destinationUrl;
    }

    @Override
    public void setDestinationUrl(String destinationUrl) {
        this.destinationUrl = destinationUrl;
    }

    @Override
    public Map<String, Map<String, String>> getComponents() {
        return components;
    }

    @Override
    public String toString() {
        return ClassUtils.getShortClassName(getClass()) + "[format=" + getFormat() + ",destinationType=" + getDestinationType() + ",destinationUrl=" + getDestinationUrl()
                + ",components=" + getComponents() + "]";
    }
}
