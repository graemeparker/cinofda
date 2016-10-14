package com.adfonic.dto.model;

import java.util.Set;

import org.jdto.annotation.DTOCascade;
import org.jdto.annotation.Source;

import com.adfonic.dto.devicegroup.DeviceGroupDto;
import com.adfonic.dto.publication.platform.PlatformDto;

public class ModelDto extends ModelPartialDto {
    
    private static final long serialVersionUID = 1L;

    @DTOCascade
    @Source(value = "platforms")
    private Set<PlatformDto> platforms;

    @Source(value = "deviceGroup")
    private DeviceGroupDto deviceGroup;

    public Set<PlatformDto> getPlatforms() {
        return platforms;
    }

    public void setPlatforms(Set<PlatformDto> platforms) {
        this.platforms = platforms;
    }

    public DeviceGroupDto getDeviceGroup() {
        return deviceGroup;
    }

    public void setDeviceGroup(DeviceGroupDto deviceGroup) {
        this.deviceGroup = deviceGroup;
    }
}
