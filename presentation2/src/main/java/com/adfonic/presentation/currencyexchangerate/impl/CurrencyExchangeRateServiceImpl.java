package com.adfonic.presentation.currencyexchangerate.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.adfonic.domain.CurrencyExchangeRate;
import com.adfonic.dto.campaign.bidding.CurrencyExchangeRateDto;
import com.adfonic.presentation.currencyexchangerate.CurrencyExchangeRateService;
import com.adfonic.presentation.util.GenericServiceImpl;
import com.byyd.middleware.common.filter.CurrencyExchangeRatesFilter;
import com.byyd.middleware.common.service.CommonManager;

@Service("currencyExchangeRateService")
public class CurrencyExchangeRateServiceImpl extends GenericServiceImpl implements CurrencyExchangeRateService {
    
    @Autowired
    private CommonManager commonManager;
    
    public CurrencyExchangeRateDto getCurrencyExchangeRate(final Long id){
        CurrencyExchangeRate currencyExchangeRate =  commonManager.getCurrencyExchangeRateById(id);
        return getObjectDto(CurrencyExchangeRateDto.class, currencyExchangeRate);
    }
    
    public CurrencyExchangeRateDto getDefaultCurrencyExchangeRate(){
        CurrencyExchangeRatesFilter filter = new CurrencyExchangeRatesFilter().setDefaultConversion(true);
        List<CurrencyExchangeRate> currencyExchangeRates = commonManager.getCurrencyExchangeRates(filter);
        
        CurrencyExchangeRateDto dto = null;
        if (currencyExchangeRates!=null){
            dto = getObjectDto(CurrencyExchangeRateDto.class, currencyExchangeRates.get(0));
        }
        return dto;
    }
    
    public List<CurrencyExchangeRateDto> getCurrencyExchangeRate(final String fromCurrencyCode, final String toCurrencyCode){
        CurrencyExchangeRatesFilter filter = new CurrencyExchangeRatesFilter().setFromCurrencyCode(fromCurrencyCode).setToCurrencyCode(toCurrencyCode);
        List<CurrencyExchangeRate> currencyExchangeRates = commonManager.getCurrencyExchangeRates(filter);
        return getList(CurrencyExchangeRateDto.class, currencyExchangeRates);
    }
    
    public List<CurrencyExchangeRateDto> getAllCurrencyExchangeRate(){
        List<CurrencyExchangeRate> currencyExchangeRates = commonManager.getAllCurrencyExchangeRate();
        return getList(CurrencyExchangeRateDto.class, currencyExchangeRates);
    }
}
