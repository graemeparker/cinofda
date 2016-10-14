package com.adfonic.weve.dto;

public class DeviceIdentifierTypeDto {
    
    private int typeId;
    private String systemName;
    private String regexPattern;
    private boolean secure;

    public DeviceIdentifierTypeDto(int typeId, String deviceIdName, String regexPattern, boolean secure) {
        this.typeId = typeId;
        this.systemName = deviceIdName;
        this.regexPattern = regexPattern;
        this.secure = secure;
    }

    public int getTypeId() {
        return typeId;
    }

    public String getSystemName() {
        return this.systemName;
    }

    public String getRegexPattern() {
        return this.regexPattern;
    }

    public boolean isSecure() {
        return secure;
    }
}
