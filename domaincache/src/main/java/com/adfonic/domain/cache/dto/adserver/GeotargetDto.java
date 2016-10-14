package com.adfonic.domain.cache.dto.adserver;

import com.adfonic.domain.cache.dto.BusinessKeyDto;

public class GeotargetDto extends BusinessKeyDto {
    private static final long serialVersionUID = 3L;

    public enum Type {
        POLYGON, RADIUS, POSTAL_CODE, STATE, DMA
    }

    private String name;
    private String countryIsoCode;
    private Type type;
    private double displayLatitude;
    private double displayLongitude;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCountryIsoCode() {
        return countryIsoCode;
    }

    public void setCountryIsoCode(String countryIsoCode) {
        this.countryIsoCode = countryIsoCode;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public double getDisplayLatitude() {
        return displayLatitude;
    }

    public void setDisplayLatitude(double displayLatitude) {
        this.displayLatitude = displayLatitude;
    }

    public double getDisplayLongitude() {
        return displayLongitude;
    }

    public void setDisplayLongitude(double displayLongitude) {
        this.displayLongitude = displayLongitude;
    }

    @Override
    public String toString() {
        return "GeotargetDto {" + getId() + ", name=" + name + ", countryIsoCode=" + countryIsoCode + ", type=" + type + ", displayLatitude=" + displayLatitude
                + ", displayLongitude=" + displayLongitude + "}";
    }

}
