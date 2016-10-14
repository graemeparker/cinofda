package com.adfonic.tools.beans.data;

import com.adfonic.dto.NameIdBusinessDto;

public class NoMatchDataBean {

    private String name;

    private NameIdBusinessDto dto = null;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public NameIdBusinessDto getDto() {
        return dto;
    }

    public void setDto(NameIdBusinessDto dto) {
        this.dto = dto;
    }
}
