package com.adfonic.presentation.currencyexchangerate;

import java.util.List;

import com.adfonic.dto.campaign.bidding.CurrencyExchangeRateDto;

public interface CurrencyExchangeRateService {
    
    public CurrencyExchangeRateDto getCurrencyExchangeRate(final Long id);
    public CurrencyExchangeRateDto getDefaultCurrencyExchangeRate();
    public List<CurrencyExchangeRateDto> getCurrencyExchangeRate(final String fromCurrencyCode, final String toCurrencyCode);
    public List<CurrencyExchangeRateDto> getAllCurrencyExchangeRate();
    
}

