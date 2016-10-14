package com.adfonic.reporting.sql.dto;

public class DeviceDetailByCountryByPlatformDto extends BaseReportDto {
    
    private static final long serialVersionUID = 1L;
    
    protected String country;
    protected String platform;

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getPlatform() {
        return platform;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

}
