package com.adfonic.dto.geotarget;

import java.math.BigDecimal;

import org.jdto.annotation.Source;

import com.adfonic.dto.NameIdBusinessDto;

public class LocationTargetDto extends NameIdBusinessDto {

    private static final long serialVersionUID = 1L;


    @Source(value = "latitude")
    private BigDecimal latitude;
    @Source(value = "longitude")
    private BigDecimal longitude;
    @Source(value = "radiusMiles")
    private BigDecimal radiusMiles;

    private boolean selected;

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

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

}
