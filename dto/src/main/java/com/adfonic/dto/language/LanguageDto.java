package com.adfonic.dto.language;

import com.adfonic.dto.BusinessKeyDTO;

public class LanguageDto extends BusinessKeyDTO {
    
    private static final long serialVersionUID = 1L;

    private String isoCode;
    private String name;

    public String getIsoCode() {
        return isoCode;
    }

    public void setIsoCode(String isoCode) {
        this.isoCode = isoCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
