package com.adfonic.domain.cache.service;

import java.math.BigDecimal;

import com.adfonic.domain.cache.dto.adserver.adspace.AdSpaceDto;

public interface CurrencyService {

    BigDecimal convertToBidCurrencyFromUsd(AdSpaceDto adSpace, String gmtTimeId, BigDecimal amount);

    BigDecimal convertFromBidCurrencyToUsd(AdSpaceDto adSpace, String gmtTimeId, BigDecimal amount);

    void addCurrencyConversionRate(String fromCurrency, String toCurrency, String gmtTimeId, BigDecimal amount);

    BigDecimal getCurrencyConversionRate(String fromCurrency, String toCurrency, String gmtTimeId);

    void clearAllConversionRate();
}
