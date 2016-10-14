package com.adfonic.presentation.location.model;

import java.math.BigDecimal;

public class GeoLocationModel {

    private String name;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private BigDecimal radiusMiles;

    public GeoLocationModel(String name, BigDecimal latitude, BigDecimal longitude, BigDecimal radiusMiles) {
        super();
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.radiusMiles = radiusMiles;
    }

    public String getName() {
        return name;
    }
    
    public BigDecimal getLatitude() {
        return latitude;
    }

    public void setLatitude(BigDecimal latitude) {
        this.latitude = latitude;
    }

    public BigDecimal getLongitude() {
        return longitude;
    }

    public void setLongitude(BigDecimal longitude) {
        this.longitude = longitude;
    }

    public BigDecimal getRadiusMiles() {
        return radiusMiles;
    }

    public void setRadiusMiles(BigDecimal radiusMiles) {
        this.radiusMiles = radiusMiles;
    }

}
