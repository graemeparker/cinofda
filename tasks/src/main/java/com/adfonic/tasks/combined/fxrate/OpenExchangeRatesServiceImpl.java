package com.adfonic.tasks.combined.fxrate;

import java.io.IOException;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultServiceUnavailableRetryStrategy;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class OpenExchangeRatesServiceImpl implements ExchangeRatesService{
    
    private final transient Logger LOG = LoggerFactory.getLogger(getClass().getName());
    
    // OpenExchangeRates URL
    @Value("${FxRate.api.url}")
    private String fxRateApiUrl;
    @Value("${FxRate.api.key}")
    private String fxRateApiKey;
    
    private ObjectMapper objectMapper = new ObjectMapper();
    
    // HTTP auto-retry settings
    private static final int MAX_RETRIES = 10;
    private static final int RETRY_INTERVAL_MS = 30000;
    
    private static final String DEFAULT_BASE_CURRENCY = "USD";
    
    @Override
    public Rates getRates() {
        return getRates(DEFAULT_BASE_CURRENCY);
    }

    @Override
    public Rates getRates(String baseCurrency) {
        LOG.debug("Fetching conversion rates from OpenExchangeRates");
        Rates rates = null;
        String internalBaseCurrency = (StringUtils.isBlank(baseCurrency)?DEFAULT_BASE_CURRENCY:baseCurrency);

        HttpClient httpClient = HttpClientBuilder.create().setServiceUnavailableRetryStrategy(new DefaultServiceUnavailableRetryStrategy(MAX_RETRIES, RETRY_INTERVAL_MS)).build();
        HttpGet httpGet = new HttpGet(String.format(fxRateApiUrl, fxRateApiKey, internalBaseCurrency));
        LOG.debug("{} {}", httpGet.getMethod(), httpGet.getURI());
        HttpResponse httpResponse;
        try {
            httpResponse = httpClient.execute(httpGet);
            rates = objectMapper.readValue(httpResponse.getEntity().getContent(), Rates.class);
            if (rates != null) {
                if (rates.isError()) {
                    LOG.error(rates.getDescription());
                    throw new RuntimeException(rates.getDescription());
                }
            } else {
                //We didn't receive any rates from the Fx API. Logging the response from the API
                String cErrorMsg = "Response from OpenExchangeRates: " + httpResponse.getEntity().getContent();
                LOG.error(cErrorMsg);
                throw new RuntimeException(cErrorMsg);
            }
        } catch (IOException e) {
            String cErrorMsg = "Something went wrong retrieving fixed rates from OpenExchangeRates:";
            LOG.error(cErrorMsg, e);
            throw new RuntimeException(cErrorMsg, e);
        }
        
        return rates;
    }
}
