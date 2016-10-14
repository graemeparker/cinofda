package com.adfonic.reporting.sql.dto;

public class DeviceDetailByCountryByPlatformDayDto extends DeviceDetailByCountryDayDto {

    private static final long serialVersionUID = 1L;

    protected String country;
    protected String day;
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

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

}
