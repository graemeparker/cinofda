package com.adfonic.domain.cache.dto.datacollector.publication;

import java.util.TimeZone;

import com.adfonic.domain.cache.dto.BusinessKeyDto;
import com.adfonic.util.TimeZoneUtils;

public class CompanyDto extends BusinessKeyDto {
    private static final long serialVersionUID = 1L;

    private boolean taxablePublisher;
    private String defaultTimeZoneID;

    private volatile transient TimeZone defaultTimeZone;

    public boolean isTaxablePublisher() {
        return taxablePublisher;
    }

    public void setTaxablePublisher(boolean taxablePublisher) {
        this.taxablePublisher = taxablePublisher;
    }

    public String getDefaultTimeZoneID() {
        return defaultTimeZoneID;
    }

    public void setDefaultTimeZoneID(String defaultTimeZoneID) {
        this.defaultTimeZoneID = defaultTimeZoneID;
    }

    public TimeZone getDefaultTimeZone() {
        if (defaultTimeZone == null) {
            defaultTimeZone = TimeZoneUtils.getTimeZoneNonBlocking(defaultTimeZoneID);
        }
        return defaultTimeZone;
    }

    @Override
    public String toString() {
        return "CompanyDto [" + getId() + ", taxablePublisher=" + taxablePublisher + ", defaultTimeZoneID=" + defaultTimeZoneID + "}";
    }

}
