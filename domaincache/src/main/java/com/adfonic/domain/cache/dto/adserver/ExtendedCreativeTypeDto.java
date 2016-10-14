package com.adfonic.domain.cache.dto.adserver;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.adfonic.domain.ContentForm;
import com.adfonic.domain.Feature;
import com.adfonic.domain.MediaType;
import com.adfonic.domain.cache.dto.BusinessKeyDto;

public class ExtendedCreativeTypeDto extends BusinessKeyDto {
    private static final long serialVersionUID = 4L;

    private String name;
    private MediaType mediaType;
    private Set<Feature> features = new HashSet<Feature>();
    private Map<ContentForm, String> templateMap = new HashMap<ContentForm, String>();
    private boolean clickRedirectRequired;
    private boolean useDynamicTemplates;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public MediaType getMediaType() {
        return mediaType;
    }

    public void setMediaType(MediaType mediaType) {
        this.mediaType = mediaType;
    }

    public Set<Feature> getFeatures() {
        return features;
    }

    public Map<ContentForm, String> getTemplateMap() {
        return templateMap;
    }

    public String getTemplate(ContentForm contentForm) {
        return templateMap.get(contentForm);
    }

    public boolean isClickRedirectRequired() {
        return clickRedirectRequired;
    }

    public void setClickRedirectRequired(boolean clickRedirectRequired) {
        this.clickRedirectRequired = clickRedirectRequired;
    }

    public boolean getUseDynamicTemplates() {
        return useDynamicTemplates;
    }

    public void setUseDynamicTemplates(boolean useDynamicTemplates) {
        this.useDynamicTemplates = useDynamicTemplates;
    }

    @Override
    public String toString() {
        return "ExtendedCreativeTypeDto {" + getId() + ", name=" + name + ", mediaType=" + mediaType + ", useDynamicTemplates=" + useDynamicTemplates + ", features=" + features
                + ", templateMap=" + templateMap + ", clickRedirectRequired=" + clickRedirectRequired + "}";
    }

}
