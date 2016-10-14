package com.adfonic.dto.geotarget;

import org.jdto.annotation.Source;

import com.adfonic.dto.NameIdBusinessDto;

public class GeotargetPartialDto extends NameIdBusinessDto {

    private static final long serialVersionUID = 1L;

    @Source(value = "type")
    private String type;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

}
