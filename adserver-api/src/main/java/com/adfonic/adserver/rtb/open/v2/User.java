package com.adfonic.adserver.rtb.open.v2;

/**
 * OpenRTB-API-Specification
 * 3.2.13 Object: User
 *
 */
public class User {

    /**
     * Exchange-specific ID for the user. At least one of id or
     * buyerid is recommended.
     */
    private String id;

    /**
     * Year of birth as a 4-digit integer.
     */
    private Integer yob;

    /**
     * Gender, where “M” = male, “F” = female, “O” = known to be
     * other (i.e., omitted is unknown).
     */
    private String gender;

    /**
     * Location of the user’s home base defined by a Geo object
     * (Section 3.2.12). This is not necessarily their current location.
     */
    private Geo geo;

    // Unmapped: buyerid, keywords, customdata, data

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Integer getYob() {
        return yob;
    }

    public void setYob(Integer yob) {
        this.yob = yob;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public Geo getGeo() {
        return geo;
    }

    public void setGeo(Geo geo) {
        this.geo = geo;
    }

}
