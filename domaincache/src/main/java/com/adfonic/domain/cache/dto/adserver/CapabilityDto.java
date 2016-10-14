package com.adfonic.domain.cache.dto.adserver;

import com.adfonic.domain.cache.dto.BusinessKeyDto;
import com.adfonic.util.Constrained;

public class CapabilityDto extends BusinessKeyDto implements Constrained {
    private static final long serialVersionUID = 2L;

    private String name;
    private String constraints;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
        return "CapabilityDto {" + getId() + ", name=" + name + ", constraints=" + constraints + "}";
    }

}
