package com.adfonic.domain;

import javax.persistence.*;

/** A geotarget point is one element in a geotarget's list of coordinates. */
@Entity
@Table(name="GEOTARGET_POINT")
public class GeotargetPoint extends BusinessKey {
    private static final long serialVersionUID = 1L;

    @Id @GeneratedValue @Column(name="ID")
    private long id;
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="GEOTARGET_ID",nullable=false)
    private Geotarget geotarget;
    @Column(name="LATITUDE",nullable=false)
    private double latitude;
    @Column(name="LONGITUDE",nullable=false)
    private double longitude;

    GeotargetPoint() {}

    public GeotargetPoint(Geotarget geotarget,
                          double latitude,
                          double longitude)
    {
        this.geotarget = geotarget;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public long getId() { return id; };
    
    public Geotarget getGeotarget() { return geotarget; }
    
    public double getLatitude() { return latitude; }
    
    public double getLongitude() { return longitude; }
}
