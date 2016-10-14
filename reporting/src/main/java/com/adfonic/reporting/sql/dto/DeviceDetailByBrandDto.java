package com.adfonic.reporting.sql.dto;

public class DeviceDetailByBrandDto extends BaseReportDto {

    private static final long serialVersionUID = 1L;

    protected String brand;

    public void setBrand(String br) {
        brand = br;
    }

    public String getBrand() {
        return brand;
    }

}
