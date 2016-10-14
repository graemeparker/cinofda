package com.adfonic.tasks.combined;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.adfonic.domain.CurrencyExchangeRate;
import com.adfonic.tasks.combined.fxrate.ExchangeRatesService;
import com.adfonic.tasks.combined.fxrate.Rates;
import com.byyd.middleware.auditlog.listener.AuditLogJpaListener;
import com.byyd.middleware.common.service.CommonManager;

/**
 * ExchangeRatesAutofeed tasks retrieve all data 
 */
@Component
public class ExchangeRatesAutoFeedTask {

    private final transient Logger LOG = LoggerFactory.getLogger(getClass().getName());
    
    public static final MathContext CURRENCY_MATH_CONTEXT = new MathContext(6, RoundingMode.HALF_EVEN);
    private final TimeZone GMT_TIMEZONE = TimeZone.getTimeZone("GMT");
    
    @Autowired
    CommonManager commonManager;
    
    @Autowired
    ExchangeRatesService exchangeRateService;
    
    @Autowired
    private AuditLogJpaListener auditLogJpaListener;
    
    com.byyd.middleware.auditlog.listener.System system = new com.byyd.middleware.auditlog.listener.System(getClass().getName());
    
    public void doTask() {
        LOG.info("Starting {} job", getClass().getName());
        
        LOG.debug("Setting auditLog credentials");
        auditLogJpaListener.setContextInfo(system);
        
        updateExchangeRates();
        
        LOG.debug("Removing auditLog credentials");
        auditLogJpaListener.cleanContextInfo();
        
        LOG.info("Job {} has finished.", getClass().getName());
    }

    @Transactional
    private void updateExchangeRates() {
        // Get all currency conversions from database
        LOG.debug("Getting current currency exchanges rates from database");
        List<CurrencyExchangeRate> currencyExchangeRates = commonManager.getAllCurrencyExchangeRate();
        LOG.info("{} currency exchange rates found in database", currencyExchangeRates.size());
        
        // Create a map for storing exchange rates retrieved  
        Map<String, Rates> ratesMaps = new HashMap<String, Rates>();
        int cnt = 0;
        
        // Iterate for each currency, and set its new exchange value
        try{
            for (CurrencyExchangeRate currencyExchangeRate : currencyExchangeRates){
                String fromCurrencyCode = currencyExchangeRate.getFromCurrencyCode();
                String toCurrencyCode = currencyExchangeRate.getToCurrencyCode();
                LOG.debug("Getting exchange rate from cache for currency conversion: {} -> {}" , fromCurrencyCode, toCurrencyCode);
                Double exchangeRate = getExchangeRate(fromCurrencyCode, toCurrencyCode, ratesMaps);
                if (exchangeRate==null){
                    LOG.error("Cannot update exchange rate for {} -> {} conversion. Not rates available on server. Check currency codes on database!!" , fromCurrencyCode, toCurrencyCode);
                }else if(new BigDecimal(exchangeRate, CURRENCY_MATH_CONTEXT).compareTo(currencyExchangeRate.getCurrentExchangeRate())==0){
                    LOG.info("Exchange rate for {} -> {} conversion has not changed and it won't be updated" , fromCurrencyCode, toCurrencyCode);
                }else{
                    LOG.info("Updating exchange rate for {} -> {} conversion to {}" , fromCurrencyCode, toCurrencyCode, exchangeRate);
                    currencyExchangeRate.setCurrentExchangeRate(new BigDecimal(exchangeRate, CURRENCY_MATH_CONTEXT));
                    currencyExchangeRate.setLastUpdated(Calendar.getInstance(GMT_TIMEZONE).getTime());
                    commonManager.update(currencyExchangeRate);
                    cnt++;
                }
            }
            LOG.info("{} exchange rates were updated.", cnt);
        }catch(RuntimeException e){
            LOG.error("Can not update all currency exchange rates. Something went wrong {}", e);
        }
    }

    private Double getExchangeRate(String fromCurrencyCode, String toCurrencyCode, Map<String, Rates> ratesMaps) {
        Rates rates = ratesMaps.get(fromCurrencyCode);
        if (rates==null){
            rates = exchangeRateService.getRates(fromCurrencyCode);
            ratesMaps.put(fromCurrencyCode, rates);
        }
        return rates.getRates().get(toCurrencyCode);
    }
}
