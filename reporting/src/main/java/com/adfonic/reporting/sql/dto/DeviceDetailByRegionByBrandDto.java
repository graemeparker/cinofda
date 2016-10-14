package com.adfonic.reporting.sql.dto;

public class DeviceDetailByRegionByBrandDto extends BaseReportDto {

    private static final long serialVersionUID = 1L;

    protected String brand;
    protected String region;

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getBrand() {
        return brand;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }
}
