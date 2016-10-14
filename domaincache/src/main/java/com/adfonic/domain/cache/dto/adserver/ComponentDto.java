package com.adfonic.domain.cache.dto.adserver;

import java.util.HashMap;
import java.util.Map;

import com.adfonic.domain.cache.dto.BusinessKeyDto;

public class ComponentDto extends BusinessKeyDto {
    private static final long serialVersionUID = 2L;

    private String systemName;
    private Map<DisplayTypeDto, ContentSpecDto> contentSpecMap = new HashMap<DisplayTypeDto, ContentSpecDto>();

    public String getSystemName() {
        return systemName;
    }

    public void setSystemName(String systemName) {
        this.systemName = systemName;
    }

    public Map<DisplayTypeDto, ContentSpecDto> getContentSpecMap() {
        return contentSpecMap;
    }

    public ContentSpecDto getContentSpec(DisplayTypeDto displayType) {
        return contentSpecMap.get(displayType);
    }

    @Override
    public String toString() {
        return "ComponentDto {" + getId() + "," + systemName + ",contentSpecMap=" + contentSpecMap + "}";
    }

}
