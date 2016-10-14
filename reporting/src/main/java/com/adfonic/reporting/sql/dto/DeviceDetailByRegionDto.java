package com.adfonic.reporting.sql.dto;

public class DeviceDetailByRegionDto extends DeviceDetailDto {

    private static final long serialVersionUID = 1L;

    protected String region;

    public void setRegion(String reg) {
        region = reg;
    }

    public String getRegion() {
        return region;
    }

}
