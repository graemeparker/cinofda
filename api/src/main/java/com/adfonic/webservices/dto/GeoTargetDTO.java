package com.adfonic.webservices.dto;

import com.adfonic.domain.GeotargetType;

public class GeoTargetDTO {//TODO - GeoTarget*->Geotarget* in line with domain class

    private String name;

    private String country;// country.isoCode

    public enum Type { POSTAL_CODE, STATE, DMA }
    
    private Type type;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }
    
    public void setTypeByGeotargetType(GeotargetType geotargetType) {
        this.type = Type.valueOf(geotargetType.getType());
        // let it throw illegal arg for unsupported campaign types
    }
}
