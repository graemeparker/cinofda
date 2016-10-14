package com.adfonic.util;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.HashSet;
import java.util.Set;

public class StringUtils {

    private StringUtils() {
    }

    public static String concat(String lhs, String rhs) {
        return lhs + rhs;
    }

    public static String capitalize(String input) {
        if (input == null || "".equals(input)) {
            return "";
        }
        return input.substring(0, 1).toUpperCase() + input.substring(1);
    }

    /*
     * The spec: given an integer, round up, and display as "n K" or "n M" for
     * thousands and millions, respectively. Given that:
     * 
     * 0-499 == "" 500-999499 == "n "K" + == "n M"
     */
    public static String toThousandsString(Integer value) {
        String result = "";
        if (value > 499) {
            Double upTo;
            String suffix;

            if (value < 999500) {
                upTo = value / 1000.0;
                suffix = " K";
            } else {
                upTo = value / 1000000.0;
                suffix = " M";
            }
            DecimalFormat df = new DecimalFormat("###,###,##0");
            df.setRoundingMode(RoundingMode.HALF_UP);
            result = df.format(upTo) + suffix;
        }
        return result;
    }

    public static Set<Long> toSetOfLongs(String value, String separator) {
        String[] split = value.split(separator);
        HashSet<Long> set = new HashSet<Long>();
        for (String item : split) {
            set.add(Long.parseLong(item.trim()));
        }
        return set;
    }

    public static Set<String> toSetOfStrings(String value, String separator) {
        String[] split = value.split(separator);
        HashSet<String> set = new HashSet<String>();
        for (String item : split) {
            set.add(item.trim());
        }
        return set;
    }

    public static Long tryLong(String creativeIdent) {
        try {
            return Long.parseLong(creativeIdent);
        } catch (NumberFormatException nfx) {
            return null;
        }

    }
}
