package com.adfonic.domain;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name="LOCATION_TARGET")
public class LocationTarget extends BusinessKey implements Named {

    private static final long serialVersionUID = 1L;
    
    @Id @GeneratedValue @Column(name="ID")
    private long id;
    
    @Column(name="NAME",length=255,nullable=false)
    private String name;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="ADVERTISER_ID",nullable=false)
    private Advertiser advertiser;

    @Column(name="LATITUDE",nullable=false)
    private BigDecimal latitude;
    @Column(name="LONGITUDE",nullable=false)
    private BigDecimal longitude;
    @Column(name="RADIUS_MILES",nullable=false)
    private BigDecimal radiusMiles;
    
    LocationTarget() {
    }
    
    public LocationTarget(Advertiser advertiser, 
                          String name, 
                          BigDecimal latitude,
                          BigDecimal longitude,
                          BigDecimal radiusMiles) {
        this.advertiser = advertiser;
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.radiusMiles = radiusMiles;
    }
    
    @Override
    public long getId() {
        return id;
    }
    
    @Override
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public Advertiser getAdvertiser() {
        return advertiser;
    }
    public void setAdvertiser(Advertiser advertiser) {
        this.advertiser = advertiser;
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
