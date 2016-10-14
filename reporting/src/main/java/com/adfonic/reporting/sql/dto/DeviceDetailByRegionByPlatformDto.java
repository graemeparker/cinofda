package com.adfonic.reporting.sql.dto;

public class DeviceDetailByRegionByPlatformDto extends BaseReportDto {

    private static final long serialVersionUID = 1L;

    protected String platform;
    protected String region;

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getPlatform() {
        return platform;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

}
