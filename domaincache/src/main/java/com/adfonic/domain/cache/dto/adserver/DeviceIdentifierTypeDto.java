package com.adfonic.domain.cache.dto.adserver;

import java.util.regex.Pattern;

import com.adfonic.domain.cache.dto.BusinessKeyDto;

public class DeviceIdentifierTypeDto extends BusinessKeyDto implements Comparable<DeviceIdentifierTypeDto> {
    private static final long serialVersionUID = 3L;

    private String systemName;
    private int precedenceOrder;
    private Pattern validationPattern;
    private boolean secure;

    public String getSystemName() {
        return systemName;
    }

    public void setSystemName(String systemName) {
        this.systemName = systemName;
    }

    public int getPrecedenceOrder() {
        return precedenceOrder;
    }

    public void setPrecedenceOrder(int precedenceOrder) {
        this.precedenceOrder = precedenceOrder;
    }

    public Pattern getValidationPattern() {
        return validationPattern;
    }

    public void setValidationPattern(Pattern validationPattern) {
        this.validationPattern = validationPattern;
    }

    public boolean isSecure() {
        return secure;
    }

    public void setSecure(boolean secure) {
        this.secure = secure;
    }

    @Override
    public int compareTo(DeviceIdentifierTypeDto other) {
        return Integer.valueOf(precedenceOrder).compareTo(Integer.valueOf(other.precedenceOrder));
    }

    @Override
    public String toString() {
        return "DeviceIdentifierTypeDto {" + getId() + ", systemName=" + systemName + ", precedenceOrder=" + precedenceOrder + ", validationPattern=" + validationPattern
                + ", secure=" + secure + "]";
    }

}
