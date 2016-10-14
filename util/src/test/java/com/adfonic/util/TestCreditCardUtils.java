package com.adfonic.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Calendar;

import org.apache.commons.lang.time.DateUtils;
import org.junit.Test;

public class TestCreditCardUtils {
    @Test
    public void testMakeExpirationDate() {
        Calendar now = Calendar.getInstance();
        for (int k = 0; k < 1000; ++k) {
            if (k > 0) {
                now.add(Calendar.DATE, 1);
            }
            Calendar firstOfThisMonth = DateUtils.truncate(now, Calendar.MONTH);
            int mm1to12 = now.get(Calendar.MONTH) + 1;
            int year = now.get(Calendar.YEAR);
            assertEquals(firstOfThisMonth.getTime(), CreditCardUtils.makeExpirationDate(mm1to12, year));
        }
    }

    @Test
    public void testOnlyDigits() {
        assertEquals("1234567890123456", CreditCardUtils.onlyDigits("1234-5678-9012-3456"));
        assertEquals("1234567890123456", CreditCardUtils.onlyDigits("1234.5678.9012.3456"));
        assertEquals("1234567890123456", CreditCardUtils.onlyDigits("1234 5678 9012 3456"));
    }

    @Test
    public void testIsValidCreditCardNumber() {
        assertFalse(CreditCardUtils.isValidCreditCardNumber("1234567890123456"));
        assertFalse(CreditCardUtils.isValidCreditCardNumber("1"));
        assertFalse(CreditCardUtils.isValidCreditCardNumber("123456"));
        // http://www.paypalobjects.com/en_US/vhelp/paypalmanager_help/credit_card_numbers.htm
        assertTrue(CreditCardUtils.isValidCreditCardNumber("4111111111111111"));
        assertTrue(CreditCardUtils.isValidCreditCardNumber("4012888888881881"));
        assertTrue(CreditCardUtils.isValidCreditCardNumber("5555555555554444"));
        assertTrue(CreditCardUtils.isValidCreditCardNumber("5105105105105100"));
        assertTrue(CreditCardUtils.isValidCreditCardNumber("6011111111111117"));
        assertTrue(CreditCardUtils.isValidCreditCardNumber("6011000990139424"));
        assertTrue(CreditCardUtils.isValidCreditCardNumber("378282246310005"));
        assertTrue(CreditCardUtils.isValidCreditCardNumber("371449635398431"));
    }
}
