package com.adfonic.reporting.sql.dto;

public class DeviceDetailByCountryByBrandDayDto extends DeviceDetailByCountryByBrandDto {

    private static final long serialVersionUID = 1L;

    protected String day;

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

}
