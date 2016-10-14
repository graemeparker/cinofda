package com.adfonic.sso.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import com.adfonic.domain.Country;
import com.adfonic.domain.CurrencyExchangeRate;
import com.byyd.middleware.common.service.CommonManager;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.SortOrder;
import com.byyd.middleware.iface.dao.Sorting;

public class SystemServiceImpl implements SystemService {

    @Autowired
    private CommonManager commonManager;
    
    @Override
    @Transactional(readOnly=true)
    public List<Country> getCountriesList() {
        return commonManager.getAllCountries(new Sorting(SortOrder.asc("name")), (FetchStrategy[]) null);
    }
    
    @Override
    @Transactional(readOnly=true)
    public Country getCountry(String isoCode){
        return commonManager.getCountryByIsoCode(isoCode, (FetchStrategy[]) null);
    }
    
    @Override
    @Transactional(readOnly=true)
    public List<CurrencyExchangeRate> getDefaultCurrenciesList(){
        return commonManager.getAllCurrencyExchangeRate();
    }
    
    @Override
    @Transactional(readOnly=true)
    public CurrencyExchangeRate getDefaultCurrency(String id){
        return commonManager.getCurrencyExchangeRateById(id);
    }
}
