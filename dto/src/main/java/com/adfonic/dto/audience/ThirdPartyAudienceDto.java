package com.adfonic.dto.audience;

/**
 * This DTO is meant to hold the collated results of DMPVendor Name/DMPAttribute
 * Name + DMPSelectorName, DMPSelector price and DMPSelector populations
 *
 * @author pierre
 *
 */
public class ThirdPartyAudienceDto {

    private String externalId;
    private String vendorName;
    private String attributeName;
    private String selectorName;
    private String thirdPartyAudienceName;
    private double dataRetail;
    private long population;

    public String getVendorName() {
        return vendorName;
    }

    public void setVendorName(String vendorName) {
        this.vendorName = vendorName;
    }

    public String getAttributeName() {
        return attributeName;
    }

    public void setAttributeName(String attributeName) {
        this.attributeName = attributeName;
    }

    public String getSelectorName() {
        return selectorName;
    }

    public void setSelectorName(String selectorName) {
        this.selectorName = selectorName;
    }

    public String getThirdPartyAudienceName() {
        return thirdPartyAudienceName;
    }

    public void setThirdPartyAudienceName(String thirdPartyAudienceName) {
        this.thirdPartyAudienceName = thirdPartyAudienceName;
    }

    public double getDataRetail() {
        return dataRetail;
    }

    public void setDataRetail(double dataRetail) {
        this.dataRetail = dataRetail;
    }

    public long getPopulation() {
        return population;
    }

    public void setPopulation(long population) {
        this.population = population;
    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

}
