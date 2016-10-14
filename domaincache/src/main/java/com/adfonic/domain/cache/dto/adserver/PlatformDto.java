package com.adfonic.domain.cache.dto.adserver;

import com.adfonic.domain.cache.dto.BusinessKeyDto;
import com.adfonic.util.Constrained;

public class PlatformDto extends BusinessKeyDto implements Constrained {
    private static final long serialVersionUID = 2L;

    private String name;
    private String systemName;
    private String constraints;

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

    @Override
    public String getConstraints() {
        return constraints;
    }

    public void setConstraints(String constraints) {
        this.constraints = constraints;
    }

    @Override
    public String toString() {
        return "PlatformDto {" + getId() + ", systemName=" + systemName + ", constraints=" + constraints + "}";
    }

}
