package com.adfonic.domain;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@Table(name="CURRENCY_EXCHANGE_RATE")
public class CurrencyExchangeRate extends BusinessKey {
    private static final long serialVersionUID = 1L;
    
    @Id 
    @GeneratedValue 
    @Column(name="ID")
    private long id;
    
    @Column(name="FROM_CURRENCY_CODE",length=3,nullable=false)
    private String fromCurrencyCode;
    
    @Column(name="TO_CURRENCY_CODE",length=3,nullable=false)
    private String toCurrencyCode;
    
    @Column(name="CURRENT_EXCHANGE_RATE",nullable=false)
    private BigDecimal currentExchangeRate;
    
    @Column(name="MIN_THRESHOLD",nullable=false)
    private BigDecimal minThreshold;
    
    @Column(name="MAX_THRESHOLD",nullable=false)
    private BigDecimal maxThreshold;
    
    @Column(name="LAST_UPDATED",nullable=true)
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastUpdated;
    
    @Column(name="IS_DEFAULT",nullable=false)
    private boolean defaultConversion;
    
    // CONSTRUCTOR
    CurrencyExchangeRate() {
        // empty constructor
    }
    
    // GETTERS & SETTERS
    public long getId() { return id; }

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
