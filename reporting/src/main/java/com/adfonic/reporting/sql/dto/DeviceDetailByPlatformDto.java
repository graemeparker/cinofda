package com.adfonic.reporting.sql.dto;

public class DeviceDetailByPlatformDto extends BaseReportDto {

    private static final long serialVersionUID = 1L;

    protected String platform;

    public void setPlatform(String plat) {
        this.platform = plat;
    }

    public String getPlatform() {
        return platform;
    }

}
