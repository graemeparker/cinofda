package com.adfonic.tasks.combined;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

import javax.sql.DataSource;

import org.apache.commons.dbutils.DbUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.adfonic.tasks.combined.fxrate.ExchangeRatesService;
import com.adfonic.tasks.combined.fxrate.Rates;
import com.adfonic.util.ConfUtils;

/**
 * FxRateTasks to fetch and update currency rates for CURRENCY_CONVERSION table.
 */
@Component
public class FxRateTasks {//implements Runnable {

    private final transient Logger LOG = LoggerFactory.getLogger(getClass().getName());

    private static final int HOURS_TO_PREPOPULATE = 24;
    private SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHH");

    @Autowired
    @Qualifier(ConfUtils.OPT_DS)
    private DataSource dataSource;

    @Autowired
    ExchangeRatesService exchangeRateService;

    //currencies we are interested in, to get a conversion rate for from BASE RATE i.e USD
    // we can add more currencies later and/or move this set to DB.
    private final String baseRate = "USD";
    Set<String> conversionCurrencies = new HashSet<>(Arrays.asList(new String[] { "EUR" }));

    /**
     * AD-294
     * Fetch currency rates from OpenExchangeRates every hour
     * and populate the CURRENCY_CONVERSION table with next 24 hours of conversion rate.
     */
    //@Scheduled(fixedRate=3600500)
    public void doFetchRate() {
        LOG.debug("Fetching conversion rates");

        boolean success = false;
        try {
            Rates rates = exchangeRateService.getRates();
            for (String currency : conversionCurrencies) {
                if (rates.getRates().containsKey(currency)) {
                    success = populateCurrencyRates(currency, rates.getRates().get(currency));
                } else {
                    LOG.debug("Currency rates for {} currency were not returned by ExchangeRates service.", currency);
                }
            }
        } catch (Throwable t) {
            LOG.error("Something went wrong {}", t);
        }

        if (success) {
            LOG.info("doFetchRate was successful.");
        } else {
            LOG.warn("doFetchRate was not successful.");
            for (String currency : conversionCurrencies) {
                LOG.info("Rolling over the last known {} rate for the next 24 hours.", currency);
                Double lastKnownCurrencyRate = getLastKnownCurrencyRate(currency);
                if (lastKnownCurrencyRate != null) {
                    populateCurrencyRates(currency, lastKnownCurrencyRate);
                } else {
                    LOG.error("Problem fetching currency rate from db in order to rollover since api call failed.");
                }
            }
        }
    }

    /**
     * This method will either insert if no data present for a CURRENCY pair 
     * OR update 23 hours and insert a new hour.
     * @param currency
     * @param rate
     */
    private boolean populateCurrencyRates(String currency, Double rate) {
        boolean success = false;
        Connection conn = null;
        PreparedStatement pst = null;
        ResultSet rs = null;

        LOG.debug("Populating currency rates for {} currency.", currency);

        try {
            //Get the records that need updating between NOW and next 24 hour window
            // - Can be a new currency pair hence 0 hours to update | insert 24 new rows
            // - Can be between X hours (<24) to be updated | update (X) and insert (24-X) missing rows
            LOG.debug("Getting the records that need updating between NOW and next 24 hour window");
            conn = dataSource.getConnection();
            pst = conn.prepareStatement(getRecordsToUpdateSql(currency));
            rs = pst.executeQuery();

            int i = 0;
            while (rs.next()) {
                //update currency pair value for records between NOW and next 24 hours
                int id = rs.getInt(1);
                LOG.debug("Updating currency with id={} pair value for records between NOW and next 24 hours ({})", id, currency);
                executeUpdate(getUpdateSql(id, currency, rate));
                i++;
            }
            LOG.info("{} values updated for {} currency", i, currency);
            //Insert new records for the missing hours
            if (i < HOURS_TO_PREPOPULATE) {
                //will insert (HOURS_TO_PREPOPULATE - count) for records in the CURRENCY_CONVERSION table for 
                //the combination of from_currency and to_currency 
                LOG.info("Values on table CURRENCY_CONVERSION are less than the number of hours to prepopulate ({}). Creating {} new records.", HOURS_TO_PREPOPULATE,
                        HOURS_TO_PREPOPULATE - i);
                success = executeUpdate(getInsertSql(generateValues(currency, rate, HOURS_TO_PREPOPULATE - i)));
                LOG.debug("{} new records have been prepopulated for currency {}", HOURS_TO_PREPOPULATE - i, currency);
            } else {
                success = true;
            }
        } catch (Throwable t) {
            LOG.error("Failure detected {}", t);
        } finally {
            DbUtils.closeQuietly(conn, pst, null);
        }
        return success;
    }

    /**
     * MAD-616 When we don't get data from the currency webservice we should 
     * preserve the last known rate for the next day until the next job will run. 
     * The current rate should be updated for this 'rolled over' day with
     * the next doFetchRate scheduled call.
     */
    private Double getLastKnownCurrencyRate(String currency) {
        Connection conn = null;
        PreparedStatement pst = null;
        ResultSet rs = null;
        try {
            conn = dataSource.getConnection();
            pst = conn.prepareStatement(getLastUpdatedRecordSql(currency));
            rs = pst.executeQuery();
            if (rs != null) {
                rs.next();
                Double lastRecordedRate = rs.getDouble("RATE");
                LOG.debug("fetching last recorded rate: {} {}", currency, lastRecordedRate);
                return lastRecordedRate;
            }
        } catch (SQLException e) {
            LOG.error("Failure detected {}", e);
        } finally {
            DbUtils.closeQuietly(conn, pst, null);
        }
        return null;
    }

    /**
     * Generates SQL string to get the records that need updating 
     * between NOW and next 24 hours for a given currency pair 
     * @param currency
     * @return
     */
    private String getRecordsToUpdateSql(String currency) {
        String selectSql = "SELECT ID, GMT_TIME_ID FROM CURRENCY_CONVERSION WHERE FROM_CUR = \"" + baseRate + "\" AND TO_CUR=\"" + currency
                + "\" AND GMT_TIME_ID >= DATE_FORMAT(NOW(),\"%Y%m%d%H\") AND GMT_TIME_ID < DATE_FORMAT(DATE_ADD(NOW(),INTERVAL 24 HOUR),\"%Y%m%d%H\") ORDER BY GMT_TIME_ID DESC ";
        LOG.trace("getRecordsToUpdateSql: {}", selectSql);
        return selectSql;
    }

    /**
     * Insert new records for given currency pair
     * @param values
     * @return
     */
    private String getInsertSql(String values) {
        String insertSql = "INSERT INTO CURRENCY_CONVERSION (GMT_TIME_ID, FROM_CUR, TO_CUR, RATE, LAST_UPDATED) VALUES " + values;
        LOG.trace("insertSql: {}", insertSql);
        return insertSql;
    }

    /**
     * Update records for given currency pair
     * @param id
     * @param currency
     * @param rate
     * @return
     */
    private String getUpdateSql(int id, String currency, Double rate) {
        // MAX-197: Change currency conversion update
        String updateSql = "UPDATE CURRENCY_CONVERSION SET RATE = %f, LAST_UPDATED = now() WHERE ID";
        String formattedString = String.format(updateSql, rate, id, baseRate, currency);
        LOG.trace("updateSql: {}", formattedString);
        return formattedString;
    }

    /**
     * Generates SQL string to get the latest record updated 
     * for a given currency pair to use as a fail-over 
     * @param currency
     * @return
     */
    private String getLastUpdatedRecordSql(String currency) {
        return "SELECT RATE FROM CURRENCY_CONVERSION WHERE FROM_CUR = \"" + baseRate + "\" AND TO_CUR=\"" + currency + "\" ORDER BY GMT_TIME_ID DESC LIMIT 1";
    }

    /**
     * Execute INSERT and UPDATE queries
     * @param sql
     */
    private boolean executeUpdate(String sql) {
        boolean success = false;
        LOG.trace("Updating currency conversion table");

        Connection conn = null;
        PreparedStatement pst = null;
        try {
            conn = dataSource.getConnection();
            pst = conn.prepareStatement(sql);
            pst.executeUpdate();
            success = true;
        } catch (SQLIntegrityConstraintViolationException e) {
            LOG.error("Job ran more than once for the hour. Please check why?! {}", e);
            success = true;
        } catch (java.sql.SQLException e) {
            LOG.error("Failure detected {}", e);
        } finally {
            DbUtils.closeQuietly(conn, pst, null);
        }

        LOG.trace("Finished updating currency conversion table");
        return success;
    }

    /**
     * Generate values to be inserted
     * @param currency
     * @param rate
     * @param hours
     * @return
     */
    private String generateValues(String currency, Double rate, int hours) {
        String values = "";

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.HOUR_OF_DAY, HOURS_TO_PREPOPULATE - hours);

        for (int i = 0; i < hours; i++) {
            values += String.format("(%d,\"%s\",\"%s\",%f,now()),", Integer.valueOf(format.format(cal.getTime())), baseRate, currency, rate);
            cal.add(Calendar.HOUR_OF_DAY, 1);
        }
        // Remove trailing comma
        return values.length() > 0 ? values.substring(0, values.length() - 1) : "";
    }

    //For testing ONLY! 
    //    public void run() {
    //    	doFetchRate();
    //    }
    //    
    //    public static void main(String[] args) {
    //		int exitCode = 0;
    //		try {
    //			SpringTaskBase.runBean(FxRateTasks.class, null, "adfonic-toolsdb-context.xml", "adfonic-tasks-context.xml", "adfonic-optdb-context.xml");
    //		} catch (Exception e) {
    //			LOG.log(Level.SEVERE, "Exception caught", e);
    //			exitCode = 1;
    //		} finally {
    //			Runtime.getRuntime().exit(exitCode);
    //		}
    //	}
}
