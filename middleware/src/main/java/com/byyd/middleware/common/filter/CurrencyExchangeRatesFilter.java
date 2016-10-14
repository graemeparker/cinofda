package com.byyd.middleware.common.filter;


public class CurrencyExchangeRatesFilter {
    private String fromCurrencyCode;
    private String toCurrencyCode;
    private Boolean defaultConversion;
    
    public String getFromCurrencyCode() {
        return fromCurrencyCode;
    }
    public CurrencyExchangeRatesFilter setFromCurrencyCode(String fromCurrencyCode) {
        this.fromCurrencyCode = fromCurrencyCode;
        return this;
    }
    public String getToCurrencyCode() {
        return toCurrencyCode;
    }
    public CurrencyExchangeRatesFilter setToCurrencyCode(String toCurrencyCode) {
        this.toCurrencyCode = toCurrencyCode;
        return this;
    }
    public Boolean isDefaultConversion() {
        return defaultConversion;
    }
    public CurrencyExchangeRatesFilter setDefaultConversion(Boolean defaultConversion) {
        this.defaultConversion = defaultConversion;
        return this;
    }
}
