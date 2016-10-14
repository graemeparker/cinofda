package com.adfonic.dto.campaign.bidding;

import java.math.BigDecimal;
import java.util.Date;

import org.jdto.annotation.Source;

import com.adfonic.dto.BusinessKeyDTO;

public class CurrencyExchangeRateDto extends BusinessKeyDTO {

    private static final long serialVersionUID = 1L;
    
    @Source(value = "fromCurrencyCode")
    private String fromCurrencyCode;
    
    @Source(value = "toCurrencyCode")
    private String toCurrencyCode;
    
    @Source(value = "currentExchangeRate")
    private BigDecimal currentExchangeRate;
    
    @Source(value = "minThreshold")
    private BigDecimal minThreshold;
    
    @Source(value = "maxThreshold")
    private BigDecimal maxThreshold;
    
    @Source(value = "lastUpdated")
    private Date lastUpdated;
    
    @Source(value = "defaultConversion")
    private boolean defaultConversion;

    public String getFromCurrencyCode() {
        return fromCurrencyCode;
    }

    public void setFromCurrencyCode(String fromCurrencyCode) {
        this.fromCurrencyCode = fromCurrencyCode;
    }

    public String getToCurrencyCode() {
        return toCurrencyCode;
    }

    public void setToCurrencyCode(String toCurrencyCode) {
        this.toCurrencyCode = toCurrencyCode;
    }

    public BigDecimal getCurrentExchangeRate() {
        return currentExchangeRate;
    }

    public void setCurrentExchangeRate(BigDecimal currentExchangeRate) {
        this.currentExchangeRate = currentExchangeRate;
    }

    public BigDecimal getMinThreshold() {
        return minThreshold;
    }

    public void setMinThreshold(BigDecimal minThreshold) {
        this.minThreshold = minThreshold;
    }

    public BigDecimal getMaxThreshold() {
        return maxThreshold;
    }

    public void setMaxThreshold(BigDecimal maxThreshold) {
        this.maxThreshold = maxThreshold;
    }

    public Date getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Date lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public boolean isDefaultConversion() {
        return defaultConversion;
    }

    public void setDefaultConversion(boolean defaultConversion) {
        this.defaultConversion = defaultConversion;
    }
}
