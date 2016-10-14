package com.adfonic.reporting.sql.dto;

public class DeviceDetailByBrandDayDto extends DeviceDetailByBrandDto {

    private static final long serialVersionUID = 1L;
    
    public String day;

    public void setDay(String day) {
        this.day = day;
    }

    public String getDay() {
        return day;
    }
}
