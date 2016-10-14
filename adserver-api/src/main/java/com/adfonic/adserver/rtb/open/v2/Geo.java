package com.adfonic.adserver.rtb.open.v2;

import java.math.BigDecimal;

/**
 * OpenRTB-API-Specification
 * 3.2.12 Object: Geo
 *
 */
public class Geo {
    /**
     * Latitude from -90.0 to +90.0, where negative is south
     */
    private BigDecimal lat;

    /**
     * Longitude from -180.0 to +180.0, where negative is west.
     */
    private BigDecimal lon;

    /**
     * Source of location data; recommended when passing lat/lon. Refer to List 5.16.
     */
    private LocationType type;

    /**
     * Country code using ISO-3166-1-alpha-3.
     */
    private String country;

    /**
     * Region code using ISO-3166-2; 2-letter state code if USA.
     */
    private String region;

    /**
     * City using United Nations Code for Trade & Transport
     * Locations. See Appendix A for a link to the codes.
     */
    private String city;

    /**
     * Zip or postal code.
     */
    private String zip;

    // Unmapped: regionfips104, metro, utcoffset

    public BigDecimal getLat() {
        return lat;
    }

    public void setLat(BigDecimal lat) {
        this.lat = lat;
    }

    public BigDecimal getLon() {
        return lon;
    }

    public void setLon(BigDecimal lon) {
        this.lon = lon;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getZip() {
        return zip;
    }

    public void setZip(String zip) {
        this.zip = zip;
    }

    public LocationType getType() {
        return type;
    }

    public void setType(LocationType type) {
        this.type = type;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }
}
