package com.adfonic.data.cache.loaders;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.Objects;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sql.DataSource;

import com.adfonic.data.cache.AdserverDataCacheImpl;
import com.adfonic.data.cache.ecpm.loader.DBLoader;
import com.adfonic.data.cache.ecpm.loader.EcpmDataCacheLoader;
import com.adfonic.data.cache.ecpm.loader.ReadFromRecordSet;
import com.adfonic.domain.cache.ext.util.AdfonicStopWatch;
import com.adfonic.domain.cache.service.CurrencyService;
import com.adfonic.domain.cache.service.CurrencyServiceImpl;
import com.adfonic.util.DateUtils;

public class DataCacheCurrencyLoader extends DBLoader {

    private static final transient Logger LOG = Logger.getLogger(EcpmDataCacheLoader.class.getName());

    private final DataSource dataSource;

    public DataCacheCurrencyLoader(DataSource dataSource) {
        Objects.requireNonNull(dataSource);
        this.dataSource = dataSource;
    }

    public void loadCurrencyData(final AdserverDataCacheImpl adserverDataCache, AdfonicStopWatch adfonicStopWatch) throws SQLException {
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Loading Currencies");
        }
        final CurrencyService currencyService = new CurrencyServiceImpl();
        currencyService.clearAllConversionRate();
        int timeId = DateUtils.getTimeID(new Date(), TimeZone.getDefault());
        final String CURRENCY_SQL = "SELECT GMT_TIME_ID,FROM_CUR,TO_CUR,RATE FROM CURRENCY_CONVERSION WHERE GMT_TIME_ID >= " + timeId;

        ReadFromRecordSet readBlock = new ReadFromRecordSet() {
            @Override
            public boolean read(ResultSet rs) throws SQLException {
                String gmtTimeId = rs.getString("GMT_TIME_ID");
                String fromCurrency = rs.getString("FROM_CUR");
                String toCurrency = rs.getString("TO_CUR");
                BigDecimal amount = rs.getBigDecimal("RATE");
                currencyService.addCurrencyConversionRate(fromCurrency, toCurrency, gmtTimeId, amount);
                return true;
            }
        };

        loadEntitiesFromDb(dataSource, adfonicStopWatch, "Currency Conversion Rate", CURRENCY_SQL, readBlock);
        adserverDataCache.setCurrencyService(currencyService);
    }
}
