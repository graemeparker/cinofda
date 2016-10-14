package com.adfonic.domain.cache.dto.adserver;

import java.util.ArrayList;
import java.util.List;

import com.adfonic.domain.cache.dto.BusinessKeyDto;

public class FormatDto extends BusinessKeyDto {
    private static final long serialVersionUID = 2L;

    private String name;
    private String systemName;
    private List<ComponentDto> components = new ArrayList<ComponentDto>();
    private List<DisplayTypeDto> displayTypes = new ArrayList<DisplayTypeDto>();

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

    public List<ComponentDto> getComponents() {
        return components;
    }

    public List<DisplayTypeDto> getDisplayTypes() {
        return displayTypes;
    }

    @Override
    public String toString() {
        return "FormatDto {" + getId() + "," + systemName + ", components=" + components + ", displayTypes=" + displayTypes + "}";
    }
}
