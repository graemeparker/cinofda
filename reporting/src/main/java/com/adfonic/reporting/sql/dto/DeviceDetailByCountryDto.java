package com.adfonic.reporting.sql.dto;

public class DeviceDetailByCountryDto extends DeviceDetailDto {

    private static final long serialVersionUID = 1L;
    
    protected String country;

    public void setCountry(String co) {
        country = co;
    }

    public String getCountry() {
        return country;
    }

}
