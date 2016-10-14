package com.adfonic.domain;

import java.util.HashSet;
import java.util.Set;
import javax.persistence.*;

@Entity
@Table(name="COUNTRY")
public class Country extends BusinessKey implements Named {
    private static final long serialVersionUID = 3L;

    public enum TaxRegime {
	UK, EU, ROW;
    }

    @Id @GeneratedValue @Column(name="ID")
    private long id;
    @Column(name="NAME",length=255,nullable=false)
    private String name;
    @Column(name="ISO_CODE",length=2,nullable=true)
    private String isoCode;
    @Column(name="ISO_ALPHA3",length=3,nullable=true)
    private String isoAlpha3;
    @Column(name="DIAL_PREFIX",length=10,nullable=true)
    private String dialPrefix;
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="REGION_ID",nullable=true)
    private Region region;
    @OneToMany(mappedBy="country",fetch=FetchType.LAZY)
    private Set<Operator> operators;
    @Column(name="TAX_REGIME",length=32,nullable=false)
    @Enumerated(EnumType.STRING)
    private TaxRegime taxRegime;

    /** Hidden countries cannot be used for targeting / reporting directly. */
    private boolean hidden;

    @Column(name="DISPLAY_LATITUDE",nullable=false)
    private double displayLatitude;
    
    @Column(name="DISPLAY_LONGITUDE",nullable=false)
    private double displayLongitude;
        
    @Column(name="DISPLAY_MAP_ZOOM",nullable=false)
    private int displayMapZoom;
    
    {
	this.operators = new HashSet<Operator>();
    }
    
    Country() {}

    public Country(String name, String isoCode, String isoAlpha3, String dialPrefix, Region region) {
	this(name, isoCode, isoAlpha3, dialPrefix, region, false, TaxRegime.ROW);
    }

    public Country(String name, String isoCode, String isoAlpha3, String dialPrefix, Region region, boolean hidden) {
	this(name, isoCode, isoAlpha3, dialPrefix, region, hidden, TaxRegime.ROW);
    }

    public Country(String name, String isoCode, String isoAlpha3, String dialPrefix, Region region, boolean hidden, TaxRegime taxRegime) {
        this(name, isoCode, isoAlpha3, dialPrefix, region, hidden, taxRegime, 0, 0, 0);
    }

    public Country(
                String name, 
                String isoCode, 
                String isoAlpha3, 
                String dialPrefix, 
                Region region, 
                boolean hidden, 
                TaxRegime taxRegime,
                double displayLatitude,
                double displayLongitude,
                int displayMapZoom) {
        this.name = name;
        this.isoCode = isoCode;
        this.isoAlpha3 = isoAlpha3;
        this.dialPrefix = dialPrefix;
        this.region = region;
        this.taxRegime = taxRegime;
        this.hidden = hidden;
        this.displayLatitude = displayLatitude;
        this.displayLongitude = displayLongitude;
        this.displayMapZoom = displayMapZoom;
    }
    
    public long getId() { return id; };
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getIsoCode() { return isoCode; }
    public void setIsoCode(String isoCode) { this.isoCode = isoCode; }

    public String getIsoAlpha3() { return isoAlpha3; }
    public void setIsoAlpha3(String isoAlpha3) { this.isoAlpha3 = isoAlpha3; }

    public String getDialPrefix() { return dialPrefix; }
    public void setDialPrefix(String dialPrefix) {
	this.dialPrefix = dialPrefix;
    }

    public Region getRegion() { return region; }
    public void setRegion(Region region) { this.region = region; }

    public Set<Operator> getOperators() { return operators; }

    public TaxRegime getTaxRegime() { return taxRegime; }
    public void setTaxRegime(TaxRegime taxRegime) {
	this.taxRegime = taxRegime;
    }

    public boolean isHidden() { return hidden; }
    public void setHidden(boolean hidden) { this.hidden = hidden; }
    
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

    public int getDisplayMapZoom() {
        return displayMapZoom;
    }

    public void setDisplayZoom(int displayMapZoom) {
        this.displayMapZoom = displayMapZoom;
    }    
}
