package com.adfonic.domain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import org.junit.Test;

import com.adfonic.util.TimeZoneUtils;

public class TestTaxUtils {
    private static void check(String isoCode, String value, boolean expectedResult) throws Exception {
        boolean result = TaxUtils.isValidVatNumber(isoCode, value);
        if (result != expectedResult) {
            throw new Exception("Failed: " + isoCode + ", " + value + ", expected " + expectedResult + ", got " + result);
        }
    }
    
    @Test
    public void testIsValidVatNumber() throws Exception {
        check("FU", "123456789", false);
        check(null, "123456789", false);
        check("ES", null, false);

        // 9 total, first OR last OR first and last alpha
        check("ES", "123456789", false);
        check("ES", "12345678", false);
        check("ES", "12345678a", true);
        check("ES", "a12345678", true);
        check("ES", "a1234567b", true);

        // 11 total, first and second may be alpha or digit but never O or I
        check("FR", "1234567890", false);
        check("FR", "A2345678901", true);
        check("FR", "AB345678901", true);
        check("FR", "IO345678901", false);

        // 8 total, last OR second and last may be alpha
        check("IE", "123456789", false);
        check("IE", "1234567", false);
        check("IE", "12345678", false);
        check("IE", "AA34567A", false);
        check("IE", "1234567A", true);
        check("IE", "1A34567A", true);

        // 9 or 12 digits
        check("LT", "12345678", false);
        check("LT", "123456789", true);
        check("LT", "1234567890", false);
        check("LT", "12345678901", false);
        check("LT", "123456789012", true);
        check("LT", "1234567890123", false);

        // 12 total, position 10 always B
        check("NL", "123456789B12", true);
        check("NL", "123456789B1", false);
        check("NL", "123456789012", false);

        // 2 to 10 digits
        check("RO", "1", false);
        check("RO", "12", true);
        check("RO", "123", true);
        check("RO", "1234567890", true);
        check("RO", "12345678901", false);
    }

    @Test
    public void testGetTaxRate() {
        // VAT history:
        // 15% prior to January 1, 2010
        // 17.5% between January 1, 2010 (inclusive) and January 4, 2011 (exclusive)
        // 20% from January 4, 2011 onward

        // Should be 20% today (since we know "today" is after January 4, 2011)
        assertEquals(0.20, TaxUtils.getTaxRate().doubleValue(), 0.00000001);
        Calendar cal = Calendar.getInstance(TimeZoneUtils.getTimeZoneNonBlocking("GMT"));
        assertEquals(0.20, TaxUtils.getTaxRate(cal.getTime()).doubleValue(), 0.00000001);

        // Should be 20% on January 4, 2011 at 00:00:00.000 GMT
        cal.set(Calendar.YEAR, 2011);
        cal.set(Calendar.MONTH, Calendar.JANUARY);
        cal.set(Calendar.DAY_OF_MONTH, 4);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        assertEquals(0.20, TaxUtils.getTaxRate(cal.getTime()).doubleValue(), 0.00000001);

        // Should be 17.5% on January 3, 2011 at 23:59:59.999 GMT
        cal.add(Calendar.MILLISECOND, -1);
        assertEquals(0.175, TaxUtils.getTaxRate(cal.getTime()).doubleValue(), 0.00000001);

        // Should be 17.5% on January 1, 2010 at 00:00:00.000 GMT
        cal.set(Calendar.YEAR, 2010);
        cal.set(Calendar.MONTH, Calendar.JANUARY);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        assertEquals(0.175, TaxUtils.getTaxRate(cal.getTime()).doubleValue(), 0.00000001);

        // Should be 15% on December 31, 2009 at 23:59:59.999 GMT
        cal.add(Calendar.MILLISECOND, -1);
        assertEquals(0.15, TaxUtils.getTaxRate(cal.getTime()).doubleValue(), 0.00000001);
    }

    @Test
    public void testIsAdvertiserTaxable() {
        Region westernEurope = new Region("Western Europe");
        Country gb = new Country("United Kingdom", "GB", "GBR", "44", westernEurope, false, Country.TaxRegime.UK);
        Country es = new Country("Spain", "ES", "ESP", "34", westernEurope, false, Country.TaxRegime.EU);
        
        Region northAmerica = new Region("North America");
        Country us = new Country("United States", "US", "USA", "1", northAmerica, false, Country.TaxRegime.ROW);

        // In the UK (GB) every advertiser is taxable
        assertTrue(TaxUtils.isAdvertiserTaxable(gb, null));
        assertTrue(TaxUtils.isAdvertiserTaxable(gb, "12345"));

        // Outside of the UK and not in the EU tax regime, no advertiser is taxable
        assertFalse(TaxUtils.isAdvertiserTaxable(us, null));
        assertFalse(TaxUtils.isAdvertiserTaxable(us, "12345"));

        // In the EU, an advertiser is taxable only if they DO NOT have taxCode set
        assertTrue(TaxUtils.isAdvertiserTaxable(es, null));
        assertFalse(TaxUtils.isAdvertiserTaxable(es, "12345"));
    }

    @Test
    public void testCalculateAdvertiserVat() {
        BigDecimal cost = new BigDecimal("0.05");
        Date eventTime = new Date();
        
        assertEquals(cost.multiply(TaxUtils.getTaxRate(eventTime)), TaxUtils.calculateAdvertiserVat(cost, eventTime, true));
        assertNull(TaxUtils.calculateAdvertiserVat(cost, eventTime, false));
    }

    @Test
    public void testIsPublisherTaxable() {
        Region westernEurope = new Region("Western Europe");
        Country gb = new Country("United Kingdom", "GB", "GBR", "44", westernEurope, false, Country.TaxRegime.UK);
        
        Region northAmerica = new Region("North America");
        Country us = new Country("United States", "US", "USA", "1", northAmerica, false, Country.TaxRegime.ROW);
            
        // Outside of the UK (GB), no publisher is taxable
        assertFalse(TaxUtils.isPublisherTaxable(us, null));
        assertFalse(TaxUtils.isPublisherTaxable(us, "12345"));

        // Inside the UK (GB), a publisher is only taxable if they have their taxCode set
        assertFalse(TaxUtils.isPublisherTaxable(gb, null));
        assertTrue(TaxUtils.isPublisherTaxable(gb, "12345"));
    }
    
    @Test
    public void testCalculatePublisherVat() {
        BigDecimal payout = new BigDecimal("0.03");
        Date eventTime = new Date();

        assertEquals(payout.multiply(TaxUtils.getTaxRate(eventTime)), TaxUtils.calculatePublisherVat(payout, eventTime, true));
        assertNull(TaxUtils.calculatePublisherVat(payout, eventTime, false));
    }
}
