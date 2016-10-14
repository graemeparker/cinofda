package com.adfonic.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/** A geotarget represents a geographical area, defined by an ordered list of
    of lat/lon coordinate points, that can be used for segment targeting.
*/
@Entity
@Table(name="GEOTARGET")
public class Geotarget extends BusinessKey implements Named {
    private static final long serialVersionUID = 1L;

    //public enum Type { POLYGON, RADIUS, POSTAL_CODE, STATE, DMA }

    @Id @GeneratedValue @Column(name="ID")
    private long id;
    @Column(name="NAME",length=255,nullable=false)
    private String name;
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="COUNTRY_ID",nullable=false)
    private Country country;
    //@Column(name="TYPE",nullable=false)
    //@Enumerated(EnumType.STRING)
    //private Type type;
    @Column(name="DISPLAY_LATITUDE",nullable=false)
    private double displayLatitude;
    @Column(name="DISPLAY_LONGITUDE",nullable=false)
    private double displayLongitude;
//    @Column(name="RADIUS",nullable=true)
//    private Double radius;
    //@OneToMany(mappedBy="geotarget",fetch=FetchType.LAZY)
    //@OrderColumn(name="IDX",nullable=false,insertable=true,updatable=true)
    //@OrderBy("id")
    //private List<GeotargetPoint> geotargetPoints;
    
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="GEOTARGET_TYPE_ID",nullable=false)
    private GeotargetType geotargetType;


    {
       // geotargetPoints = new ArrayList<GeotargetPoint>();
    }

    Geotarget() {}

    public Geotarget(String name, Country country/*, Type type*/, GeotargetType geotargetType, double displayLatitude, double displayLongitude) {
        this.name = name;
        this.country = country;
        //this.type = type;
        this.geotargetType = geotargetType;
        this.displayLatitude = displayLatitude;
        this.displayLongitude = displayLongitude;
    }

    public long getId() { return id; };

    public String getName() { return name; }
    public void setName(String name) {
        this.name = name;
    }

    public Country getCountry() { return country; }
    public void setCountry(Country country) {
        this.country = country;
    }

    /*
    public Type getType() { return type; }
    public void setType(Type type) {
        this.type = type;
    }
    */

    public double getDisplayLatitude() { return displayLatitude; }
    public void setDisplayLatitude(double displayLatitude) {
        this.displayLatitude = displayLatitude;
    }

    public double getDisplayLongitude() { return displayLongitude; }
    public void setDisplayLongitude(double displayLongitude) {
        this.displayLongitude = displayLongitude;
    }

	public GeotargetType getGeotargetType() {
		return geotargetType;
	}

	public void setGeotargetType(GeotargetType geotargetType) {
		this.geotargetType = geotargetType;
	}

//    public Double getRadius() { return radius; }
//    public void setRadius(Double radius) {
//        this.radius = radius;
//    }

    //public List<GeotargetPoint> getGeotargetPoints() { return geotargetPoints; }
    
    
}
