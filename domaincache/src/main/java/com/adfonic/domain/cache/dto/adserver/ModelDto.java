package com.adfonic.domain.cache.dto.adserver;

import java.util.HashSet;
import java.util.Set;

import com.adfonic.domain.cache.dto.BusinessKeyDto;

public class ModelDto extends BusinessKeyDto {
    private static final long serialVersionUID = 3L;

    private String externalID;
    private String name;
    private VendorDto vendor;
    private boolean hidden;
    private final Set<PlatformDto> platforms = new HashSet<PlatformDto>();
    private Long deviceGroupId;

    public String getExternalID() {
        return externalID;
    }

    public void setExternalID(String externalID) {
        this.externalID = externalID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public VendorDto getVendor() {
        return vendor;
    }

    public void setVendor(VendorDto vendor) {
        this.vendor = vendor;
    }

    public boolean isHidden() {
        return hidden;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    public Set<PlatformDto> getPlatforms() {
        return platforms;
    }

    public Long getDeviceGroupId() {
        return deviceGroupId;
    }

    public void setDeviceGroupId(Long deviceGroupId) {
        this.deviceGroupId = deviceGroupId;
    }

    @Override
    public String toString() {
        return "ModelDto {" + getId() + ", externalID=" + externalID + ", name=" + name + ", vendor=" + vendor + ", hidden=" + hidden + ", platforms=" + platforms
                + ", deviceGroupId=" + deviceGroupId + "}";
    }

}
