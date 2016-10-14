package com.adfonic.domain.cache.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

import com.adfonic.domain.cache.dto.adserver.adspace.AdSpaceDto;

public class CurrencyServiceImpl implements CurrencyService {

    Map<String, BigDecimal> currencyConversionMap = new HashMap<String, BigDecimal>();
    BigDecimal defaultConversion = new BigDecimal(1);
    private static final transient Logger LOG = Logger.getLogger(CurrencyServiceImpl.class.getName());

    @Override
    public BigDecimal convertToBidCurrencyFromUsd(AdSpaceDto adSpace, String gmtTimeId, BigDecimal amount) {
        String mapKey = buildKey("USD", adSpace.getPublication().getPublisher().getRtbConfig().getBidCurrency(), gmtTimeId);
        BigDecimal conversionRate = currencyConversionMap.get(mapKey);

        if (conversionRate == null) {
            LOG.warning("Invalid currency " + adSpace.getPublication().getPublisher().getRtbConfig().getBidCurrency() + ", No conversion rate found");
            return amount;
        }
        return amount.multiply(conversionRate);
    }

    @Override
    public BigDecimal convertFromBidCurrencyToUsd(AdSpaceDto adSpace, String gmtTimeId, BigDecimal amount) {
        String mapKey = buildKey("USD", adSpace.getPublication().getPublisher().getRtbConfig().getBidCurrency(), gmtTimeId);
        BigDecimal conversionRate = currencyConversionMap.get(mapKey);
        if (conversionRate == null) {
            LOG.warning("Invalid currency " + adSpace.getPublication().getPublisher().getRtbConfig().getBidCurrency() + ", No conversion rate found so used 1");
            return amount;
        }
        return amount.divide(conversionRate, 4, RoundingMode.HALF_UP);
    }

    @Override
    public void addCurrencyConversionRate(String fromCurrency, String toCurrency, String gmtTimeId, BigDecimal amount) {
        currencyConversionMap.put(buildKey(fromCurrency, toCurrency, gmtTimeId), amount);

    }

    private String buildKey(String fromCurrency, String toCurrency, String gmtTimeId) {
        String mapKey = fromCurrency + "_" + toCurrency + "_" + gmtTimeId;
        mapKey = mapKey.toUpperCase();
        return mapKey;
    }

    @Override
    public void clearAllConversionRate() {
        currencyConversionMap.clear();
    }

    @Override
    public BigDecimal getCurrencyConversionRate(String fromCurrency, String toCurrency, String gmtTimeId) {
        String mapKey = buildKey(fromCurrency, toCurrency, gmtTimeId);
        BigDecimal conversionRate = currencyConversionMap.get(mapKey);
        if (conversionRate == null) {
            LOG.warning("Invalid currency " + toCurrency + ", No conversion rate found for " + mapKey + " so used 1");
            printCurrencyCache();
            return new BigDecimal(1);
        }
        return conversionRate;
    }

    private void printCurrencyCache() {
        for (Entry<String, BigDecimal> oneCurrencyCoversion : currencyConversionMap.entrySet()) {
            LOG.warning(oneCurrencyCoversion.getKey() + " = " + oneCurrencyCoversion.getValue());
        }
    }

}
