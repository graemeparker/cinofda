package com.adfonic.domain.cache.dto.adserver;

import com.adfonic.domain.cache.dto.BusinessKeyDto;

public class LocationTargetDto extends BusinessKeyDto {
    private static final long serialVersionUID = 2L;

    private String name;
    private double latitude;
    private double longitude;
    private Double radius;
    // enable fast validation using a box of the east-north and the west south points.
    transient double fvENlatidute, fvWSlatidute, fvENlongitude, fvWSlongitude;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public Double getRadius() {
        return radius;
    }

    public void setRadius(Double radius) {
        this.radius = radius;
    }

    public double getFvENlatidute() {
        return fvENlatidute;
    }

    public void setFvENlatidute(double fvENlatidute) {
        this.fvENlatidute = fvENlatidute;
    }

    public double getFvWSlatidute() {
        return fvWSlatidute;
    }

    public void setFvWSlatidute(double fvWSlatidute) {
        this.fvWSlatidute = fvWSlatidute;
    }

    public double getFvENlongitude() {
        return fvENlongitude;
    }

    public void setFvENlongitude(double fvENlongitude) {
        this.fvENlongitude = fvENlongitude;
    }

    public double getFvWSlongitude() {
        return fvWSlongitude;
    }

    public void setFvWSlongitude(double fvWSlongitude) {
        this.fvWSlongitude = fvWSlongitude;
    }

    public boolean isPossiblyInReach(double latitude, double longitude) {
        return latitude >= fvENlatidute && latitude <= fvWSlatidute && longitude >= fvENlongitude && longitude <= fvWSlongitude;
    }

    @Override
    public String toString() {
        return "LocationTargetDto {" + getId() + ", name=" + name + ", latitude=" + latitude + ", longitude=" + longitude + ", radius=" + radius + "}";
    }

}
