package com.adfonic.util;

import java.util.Calendar;
import java.util.Date;

public class CreditCardUtils {
    
    private CreditCardUtils(){
    }
    
    public static Date makeExpirationDate(int mm1to12, int yyyy) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        cal.set(Calendar.MONTH, mm1to12 - 1);
        cal.set(Calendar.YEAR, yyyy);
        return cal.getTime();
    }

    public static String onlyDigits(String str) {
        // Make sure the ccNumber is all digits
        StringBuilder bld = new StringBuilder();
        for (int k = 0; k < str.length(); ++k) {
            if (Character.isDigit(str.charAt(k))) {
                bld.append(str.charAt(k));
            }
        }
        return bld.toString();
    }

    public static boolean isValidCreditCardNumber(String ccNumber) {
        int sum = 0;
        boolean alternate = false;
        for (int k = ccNumber.length() - 1; k >= 0; --k) {
            int val = ccNumber.charAt(k) - '0';
            if (alternate) {
                val *= 2;
                if (val > 9) {
                    val = 1 + (val % 10);
                }
            }
            sum += val;
            alternate = !alternate;
        }
        return (sum % 10) == 0;
    }
}
