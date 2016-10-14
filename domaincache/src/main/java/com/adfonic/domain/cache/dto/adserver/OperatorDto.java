package com.adfonic.domain.cache.dto.adserver;

import com.adfonic.domain.cache.dto.BusinessKeyDto;

public class OperatorDto extends BusinessKeyDto {
    private static final long serialVersionUID = 2L;

    private String name;
    private String countryIsoCode;
    private boolean weveEnabled = false;
    private boolean mobileOperator;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCountryIsoCode() {
        return countryIsoCode;
    }

    public void setCountryIsoCode(String countryIsoCode) {
        this.countryIsoCode = countryIsoCode;
    }

    public boolean isWeveEnabled() {
        return weveEnabled;
    }

    public void setWeveEnabled(boolean weveEnabled) {
        this.weveEnabled = weveEnabled;
    }

    public boolean isMobileOperator() {
        return mobileOperator;
    }

    public void setMobileOperator(boolean mobileOperator) {
        this.mobileOperator = mobileOperator;
    }

    @Override
    public String toString() {
        return "OperatorDto {" + getId() + ", name=" + name + ", countryIsoCode=" + countryIsoCode + ", weveEnabled=" + weveEnabled +  ", mobileOperator=" + mobileOperator + "}";
    }

}
