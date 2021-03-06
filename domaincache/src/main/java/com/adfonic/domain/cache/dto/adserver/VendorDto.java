package com.adfonic.domain.cache.dto.adserver;

import com.adfonic.domain.cache.dto.BusinessKeyDto;

public class VendorDto extends BusinessKeyDto {
    private static final long serialVersionUID = 2L;

    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "VendorDto {" + getId() + ", name=" + name + "}";
    }

}
