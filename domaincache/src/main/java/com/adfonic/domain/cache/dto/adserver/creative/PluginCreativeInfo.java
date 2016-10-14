package com.adfonic.domain.cache.dto.adserver.creative;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

import com.adfonic.util.HttpUtils;

public class PluginCreativeInfo implements java.io.Serializable {
    private static final long serialVersionUID = 1L;

    private static final Pattern PLUGIN_URL_PATTERN = Pattern.compile("^plugin://([^/?&]+)/?\\??(.*)$");

    private final String pluginName;
    private final Map<String, String> parameters = new LinkedHashMap<String, String>();

    public PluginCreativeInfo(CreativeDto creative) {
        if (!creative.isPluginBased()) {
            throw new IllegalArgumentException("Non-plugin-based Creative");
        }

        // Parse the destination data as a plugin URL
        String data = creative.getDestination().getData();
        Matcher matcher = PLUGIN_URL_PATTERN.matcher(data);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("The destination data URL is not a plugin URL: " + data);
        }

        pluginName = matcher.group(1);

        String queryString = matcher.group(2);
        if (queryString != null) {
            parameters.putAll(HttpUtils.decodeParams(queryString));
        }
    }

    public String getPluginName() {
        return pluginName;
    }

    public Map<String, String> getParameters() {
        return parameters;
    }

    public String getParameterRequired(String name) throws Exception {
        String value = parameters.get(name);
        if (value == null) {
            throw new Exception("PluginCreativeInfo missing required parameter: " + name);
        }
        return value;
    }

    public String getParameterOptional(String name) {
        return parameters.get(name);
    }

    public String getParameterOptional(String name, String defaultValue) {
        return StringUtils.defaultString(parameters.get(name), defaultValue);
    }

    @Override
    public String toString() {
        return "PluginCreativeInfo{pluginName=" + pluginName + ",parameters=" + parameters + "}";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((parameters == null) ? 0 : parameters.hashCode());
        result = prime * result + ((pluginName == null) ? 0 : pluginName.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        PluginCreativeInfo other = (PluginCreativeInfo) obj;
        if (parameters == null) {
            if (other.parameters != null)
                return false;
        } else if (!parameters.equals(other.parameters))
            return false;
        if (pluginName == null) {
            if (other.pluginName != null)
                return false;
        } else if (!pluginName.equals(other.pluginName))
            return false;
        return true;
    }

}
