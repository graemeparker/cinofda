package com.adfonic.reporting;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

import com.adfonic.util.CurrencyUtils;

/**
 * Some of these only apply to publishers or advertisers, not both.
 * The names correspond to measures defined in the OLAP cube.
 *
 */
public enum Metric {
    TOTAL_VIEWS(Type.INTEGER),
    COMPLETED_VIEWS(Type.INTEGER),
    AVERAGE_DURATION(Type.SECONDS),
    ENGAGEMENT_SCORE(Type.PERCENT),
    COST_PER_VIEW(Type.CURRENCY),
    Q1_PERCENT(Type.PERCENT),
    Q2_PERCENT(Type.PERCENT),
    Q3_PERCENT(Type.PERCENT),
    Q4_PERCENT(Type.PERCENT),
    REQUESTS(Type.INTEGER),
    IMPRESSIONS(Type.INTEGER),
    BEACONS(Type.INTEGER),
    FILL_RATE(Type.PERCENT),
    CLICKS(Type.INTEGER),
    CTR(Type.PERCENT),
    COST(Type.CURRENCY),
    PAYOUT(Type.CURRENCY),
    ECPM_PUB(Type.CURRENCY),
    ECPC_PUB(Type.CURRENCY),
    ECPM_AD(Type.CURRENCY),
    ECPC_AD(Type.CURRENCY),
    UNIQUE_USERS(Type.INTEGER),
    DEVICE_PERCENT_REQUESTS(Type.PERCENT),
    DEVICE_PERCENT_IMPRESSIONS(Type.PERCENT),
    PLATFORM_PERCENT_IMPRESSIONS(Type.PERCENT),
    LOCATION_PERCENT_REQUESTS(Type.PERCENT),
    LOCATION_PERCENT_IMPRESSIONS(Type.PERCENT),
    CONVERSIONS(Type.INTEGER),
    CONVERSION_PERCENT(Type.PERCENT),
    COST_PER_CONVERSION(Type.CURRENCY);

    public enum Type { INTEGER, PERCENT, CURRENCY, SECONDS };
    private Type type;

    Metric(Type type) { this.type = type; }

    public NumberFormat getFormat(Locale locale) {
        switch (type) {
        case INTEGER:
            return NumberFormat.getIntegerInstance(locale);
        case PERCENT:
            NumberFormat pi = NumberFormat.getPercentInstance(locale);
            pi.setMinimumFractionDigits(2);
            pi.setMaximumFractionDigits(2);
            return pi;
        case CURRENCY:
            return CurrencyUtils.CURRENCY_FORMAT_USD;
        case SECONDS:
            NumberFormat formatter = new DecimalFormat("## ms");
            formatter.setMaximumFractionDigits(0);
            formatter.setMinimumFractionDigits(0);
            return formatter;
        }
        return null; // never gets here
    }

    public Object getEmptyValue() {
        switch (type) {
        case INTEGER:
            return 0;
        case PERCENT:
        case CURRENCY:
            return .0d;
        }
        return null; // never gets here
    }

    public Type getType() {
        return type;
    }

}
