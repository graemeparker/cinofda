package com.adfonic.tasks.combined.fxrate;

public interface ExchangeRatesService {
    
    /**
     * Retrieve all fixed rates for the default base currency (USD).
     */
    public Rates getRates();

    /**
     * Retrieve all fixed rates for the base currency passed as argument.
     * If base currency is null, USD will be used.
     */
    public Rates getRates(String baseCurrency);
    

}
