package com.adfonic.domain.cache.dto.adserver;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.adfonic.domain.cache.dto.BusinessKeyDto;

public class ContentSpecDto extends BusinessKeyDto {
    private static final long serialVersionUID = 2L;

    public static final String CONTENT_SPEC_MANIFEST_WIDTH = "width";
    public static final String CONTENT_SPEC_MANIFEST_HEIGHT = "height";

    private String name;
    private Map<String, String> manifestProperties = new HashMap<String, String>();
    private Set<ContentTypeDto> contentTypes = new HashSet<ContentTypeDto>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, String> getManifestProperties() {
        return manifestProperties;
    }

    public Set<ContentTypeDto> getContentTypes() {
        return contentTypes;
    }

    @Override
    public String toString() {
        return "ContentSpecDto {" + getId() + "," + name + ", manifestProperties=" + manifestProperties + "}";
    }
}
