package com.adfonic.sso.services;

import java.util.List;

import com.adfonic.domain.Country;
import com.adfonic.domain.CurrencyExchangeRate;

public interface SystemService {
    
    public List<Country> getCountriesList();
    public Country getCountry(String isoCode);
    public List<CurrencyExchangeRate> getDefaultCurrenciesList();
    public CurrencyExchangeRate getDefaultCurrency(String id);
}
