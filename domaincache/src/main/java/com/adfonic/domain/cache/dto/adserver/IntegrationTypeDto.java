package com.adfonic.domain.cache.dto.adserver;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.adfonic.domain.BeaconMode;
import com.adfonic.domain.ContentForm;
import com.adfonic.domain.Feature;
import com.adfonic.domain.MediaType;
import com.adfonic.domain.cache.dto.BusinessKeyDto;

public class IntegrationTypeDto extends BusinessKeyDto {
    private static final long serialVersionUID = 3L;

    private String name;
    private String systemName;
    private Set<Feature> supportedFeatures = new HashSet<Feature>();
    private Set<BeaconMode> supportedBeaconModes = new HashSet<BeaconMode>();
    private Map<MediaType, Set<ContentForm>> contentFormsByMediaType = new HashMap<MediaType, Set<ContentForm>>();
    // i.e. blocked for dynamic types (special case)
    private Set<String> blockedExtendedCreativeTypes = new HashSet<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSystemName() {
        return systemName;
    }

    public void setSystemName(String systemName) {
        this.systemName = systemName;
    }

    public Set<Feature> getSupportedFeatures() {
        return supportedFeatures;
    }

    public Set<BeaconMode> getSupportedBeaconModes() {
        return supportedBeaconModes;
    }

    public Map<MediaType, Set<ContentForm>> getContentFormsByMediaType() {
        return contentFormsByMediaType;
    }

    public boolean isMediaTypeSupported(MediaType mediaType) {
        return contentFormsByMediaType.containsKey(mediaType);
    }

    public Set<ContentForm> getSupportedContentForms(MediaType mediaType) {
        Set<ContentForm> contentForms = contentFormsByMediaType.get(mediaType);
        return contentForms == null ? Collections.EMPTY_SET : contentForms;
    }

    public void addSupportedContentForm(MediaType mediaType, ContentForm contentForm) {
        Set<ContentForm> contentForms = contentFormsByMediaType.get(mediaType);
        if (contentForms == null) {
            contentForms = new HashSet<ContentForm>();
            contentFormsByMediaType.put(mediaType, contentForms);
        }
        contentForms.add(contentForm);
    }

    public Set<String> getBlockedExtendedCreativeTypes() {
        return blockedExtendedCreativeTypes;
    }

    @Override
    public String toString() {
        return "IntegrationTypeDto {" + getId() + " " + systemName + ", contentFormsByMediaType=" + contentFormsByMediaType + ", supportedFeatures=" + supportedFeatures
                + ", supportedBeaconModes=" + supportedBeaconModes + ", blockedExtendedCreativeTypes=" + blockedExtendedCreativeTypes + "}";
    }

}
