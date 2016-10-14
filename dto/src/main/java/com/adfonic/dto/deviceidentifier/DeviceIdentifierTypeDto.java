package com.adfonic.dto.deviceidentifier;

import org.jdto.annotation.Source;

import com.adfonic.dto.NameIdBusinessDto;

public class DeviceIdentifierTypeDto extends NameIdBusinessDto {

    private static final long serialVersionUID = 1L;

    @Source(value = "systemName")
    private String systemName;

    @Source(value = "precedenceOrder")
    private int precedenceOrder;

    @Source(value = "hidden")
    private boolean hidden;

    @Source(value = "validationRegex")
    private String validationRegex;

    @Source(value = "secure")
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

    public boolean isHidden() {
        return hidden;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    public String getValidationRegex() {
        return validationRegex;
    }

    public void setValidationRegex(String validationRegex) {
        this.validationRegex = validationRegex;
    }

    public boolean isSecure() {
        return secure;
    }

    public void setSecure(boolean secure) {
        this.secure = secure;
    }

}
