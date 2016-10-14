package com.byyd.middleware.common.dao;

import java.util.List;

import com.adfonic.domain.CurrencyExchangeRate;
import com.byyd.middleware.common.filter.CurrencyExchangeRatesFilter;
import com.byyd.middleware.iface.dao.BusinessKeyDao;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.Pagination;
import com.byyd.middleware.iface.dao.Sorting;

public interface CurrencyExchangeRateDao extends BusinessKeyDao<CurrencyExchangeRate> {
    
    Long countCurrencyExchangeRates(CurrencyExchangeRatesFilter filter);
    List<CurrencyExchangeRate> getCurrencyExchangeRates(CurrencyExchangeRatesFilter filter, FetchStrategy ... fetchStrategy);
    List<CurrencyExchangeRate> getCurrencyExchangeRates(CurrencyExchangeRatesFilter filter, Sorting sort, FetchStrategy ... fetchStrategy);
    List<CurrencyExchangeRate> getCurrencyExchangeRates(CurrencyExchangeRatesFilter filter, Pagination page, FetchStrategy ... fetchStrategy);
}
