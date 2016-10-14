package com.adfonic.dto;

import org.jdto.annotation.Source;

public abstract class NameIdBusinessDto extends BusinessKeyDTO {

    private static final long serialVersionUID = 1L;

    @Source(value = "name")
    protected String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
