package com.adfonic.sso.beans;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.adfonic.domain.Country;
import com.adfonic.domain.CurrencyExchangeRate;
import com.adfonic.sso.services.SystemService;
import com.adfonic.util.AdfonicTimeZone;

public class RootBean {
    
    @Autowired
    private SystemService systemService;
    
    private List<HearAboutPlace> hearAboutPlaces;
    
    private List<Country> countriesList;
    
    private List<CurrencyExchangeRate> defaultCurrenciesMap;
    
    public List<Country> getCountriesList(){
        if (this.countriesList==null){
            this.countriesList = systemService.getCountriesList();
        }
        return this.countriesList;
    }
    
    public List<AdfonicTimeZone> getTimezonesList() {
        return Arrays.asList(AdfonicTimeZone.values());
    }
    
    public List<HearAboutPlace> getHearAboutPlaces(){
        if (this.hearAboutPlaces==null){
            this.hearAboutPlaces = HearAboutPlacesBuilder.build();
        }
        return this.hearAboutPlaces;
    }
    
    public List<CurrencyExchangeRate> getDefaultCurrencyCodes(){
        if (this.defaultCurrenciesMap==null){
            defaultCurrenciesMap = systemService.getDefaultCurrenciesList();
        }
        return defaultCurrenciesMap;
    }
}
