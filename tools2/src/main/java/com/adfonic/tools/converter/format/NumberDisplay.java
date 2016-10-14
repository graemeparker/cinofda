package com.adfonic.tools.converter.format;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

public class NumberDisplay implements Serializable {

    private static final long serialVersionUID = 1L;

    private final long HUNDRED = 100L;
    private final long THOUSAND = 1000L;
    private final long MILLION = 1000000L;
    private final long BILLION = 1000000000L;

    private String unit;
    private Long longValue;
    private Double doublevalue;
    private Integer decimals;

    public NumberDisplay() {
        this.unit = "";
        this.longValue = 0L;
        this.doublevalue = null;
        this.decimals = null;
    }

    public NumberDisplay(String unit, Long longValue) {
        this.unit = unit;
        this.longValue = longValue;
        this.doublevalue = null;
        this.decimals = null;
    }

    public NumberDisplay(Long longValue) {
        this.longValue = longValue;
        this.unit = "";
        this.doublevalue = null;
        this.decimals = null;
    }

    public NumberDisplay(Double doubleValue) {
        this.longValue = null;
        this.unit = "";
        this.doublevalue = doubleValue;
        this.decimals = null;
    }

    public NumberDisplay(String unit, Double doubleValue) {
        this.unit = unit;
        this.longValue = null;
        this.doublevalue = doubleValue;
        this.decimals = null;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public Long getLongValue() {
        return longValue;
    }

    public void setLongValue(Long longValue) {
        this.longValue = longValue;
    }

    public Double getDoublevalue() {
        return doublevalue;
    }

    public void setDoublevalue(Double doublevalue) {
        this.doublevalue = doublevalue;
    }

    public Integer getDecimals() {
        return decimals;
    }

    public void setDecimals(Integer decimals) {
        this.decimals = decimals;
    }

    public String getAbbreviatedNumber() {
        if (longValue != null) {
            return getLongAbbreviated();
        } else {
            return getDoubleAbbreviated();
        }
    }

    public String getNormalNumber(Locale locale) {
        NumberFormat nf = NumberFormat.getInstance(locale);
        if (longValue != null) {
            return nf.format(longValue);
        } else {
            return getDoubleAbbreviated();
        }
    }

    // ****** Private methods ******//
    private String getLongAbbreviated() {
        // Normal number
        if (0 <= longValue && longValue < THOUSAND) {
            return Long.toString(longValue);
        }
        // Thousands (k)
        else if (THOUSAND <= longValue && longValue < MILLION) {
            return getValueDouble(longValue) + "k";
        }
        // Millions (m)
        if (MILLION <= longValue && longValue < BILLION) {
            return getValueDouble(longValue / THOUSAND) + "m";
        }
        // Billions (bn)
        else if (BILLION <= longValue) {
            return getValueDouble(longValue / MILLION) + "bn";
        }

        return "0";

    }

    private String getValueDouble(Long num) {
        if (num >= 100 * THOUSAND || num % THOUSAND == 0) {
            return Long.toString(num / THOUSAND);
        } else if (num >= 10 * THOUSAND) {
            return (num / THOUSAND) + "." + (num % THOUSAND) / 100;
        } else {
            return (num / THOUSAND) + "." + (num % THOUSAND) / 100 + (num % HUNDRED) / 10;
        }
    }

    private String getDoubleAbbreviated() {
        NumberFormat nf = NumberFormat.getInstance();
        DecimalFormat form = (DecimalFormat) nf;

        if (0 <= doublevalue && doublevalue < 100) {
            if (decimals != null && decimals > 2 && isFractionalPrice(doublevalue)) {
                form.applyPattern("#.000");
            } else {
                form.applyPattern("#.00");
            }
        } else {
            form.applyPattern("###.0");
        }
        if (doublevalue < 1 && doublevalue >= 0) {
            return "0" + form.format(doublevalue);
        }
        return form.format(doublevalue);
    }

    private boolean isFractionalPrice(Double d) {
        BigDecimal amount = new BigDecimal(d);
        amount = amount.setScale(3, RoundingMode.HALF_UP);
        return ((amount.multiply(new BigDecimal(1000)).intValue()) % 10) != 0;
    }
}
