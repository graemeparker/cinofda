package com.adfonic.domain.cache.dto.adserver;

import com.adfonic.domain.cache.dto.BusinessKeyDto;

public class LanguageDto extends BusinessKeyDto {
    private static final long serialVersionUID = 2L;

    private String name;
    private String isoCode;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getISOCode() {
        return isoCode;
    }

    public void setISOCode(String isoCode) {
        this.isoCode = isoCode;
    }

    public String getIsoCode() {
        return isoCode;
    }

    public void setIsoCode(String isoCode) {
        this.isoCode = isoCode;
    }

    @Override
    public String toString() {
        return "LanguageDto {" + getId() + ", name=" + name + ", isoCode=" + isoCode + "}";
    }

}
