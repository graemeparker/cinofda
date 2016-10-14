package com.adfonic.domain.cache.dto.adserver;

import com.adfonic.domain.Medium;
import com.adfonic.domain.TrackingIdentifierType;
import com.adfonic.domain.cache.dto.BusinessKeyDto;

public class PublicationTypeDto extends BusinessKeyDto {
    private static final long serialVersionUID = 4L;

    private String systemName;
    private Medium medium;
    private TrackingIdentifierType defaultTrackingIdentifierType;
    private Long defaultIntegrationTypeId;

    public String getSystemName() {
        return systemName;
    }

    public void setSystemName(String systemName) {
        this.systemName = systemName;
    }

    public Medium getMedium() {
        return medium;
    }

    public void setMedium(Medium medium) {
        this.medium = medium;
    }

    public TrackingIdentifierType getDefaultTrackingIdentifierType() {
        return defaultTrackingIdentifierType;
    }

    public void setDefaultTrackingIdentifierType(TrackingIdentifierType defaultTrackingIdentifierType) {
        this.defaultTrackingIdentifierType = defaultTrackingIdentifierType;
    }

    public Long getDefaultIntegrationTypeId() {
        return defaultIntegrationTypeId;
    }

    public void setDefaultIntegrationTypeId(Long defaultIntegrationTypeId) {
        this.defaultIntegrationTypeId = defaultIntegrationTypeId;
    }

    @Override
    public String toString() {
        return "PublicationTypeDto {" + getId() + " " + systemName + ", medium=" + medium + ", defaultTrackingIdentifierType=" + defaultTrackingIdentifierType
                + ", defaultIntegrationTypeId=" + defaultIntegrationTypeId + "}";
    }

}
