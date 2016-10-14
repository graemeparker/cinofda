package com.adfonic.util;

import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.validator.routines.UrlValidator;

public class ValidationUtils {
    private static UrlValidator urlValidator = new UrlValidator(new String[] { "http", "https" });
    public static final Pattern WHITESPACE_ANYWHERE_PATTERN = Pattern.compile("\\s+");
    public static final int URL_MAX_LENGTH = 1024;
    
    private ValidationUtils(){
    }

    /**
     * Validate email address
     * 
     * @param value
     * @return true if valid
     */
    public static boolean isValidEmailAddress(String value) {

        if (value == null || "".equals(value)) {
            return false;
        }
        if (value.indexOf("..") != -1) {
            return false;
        }
        int at = value.indexOf('@');
        int dot = value.lastIndexOf('.');
        if (at <= 0) {
            return false;
        } else if (dot <= 0 || dot < at) {
            return false;
        } else if (dot == (value.length() - 1)) {
            return false;
        }

        try {
            new javax.mail.internet.InternetAddress(value);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Uses isValidPhoneNumber but does not allow non-numeric values other than
     * + required in the first position
     *
     * see {@link Functions#isValidPhoneNumber(String)}
     *
     * @param String
     *            number to check
     * @return true if valid false if invalid
     */
    public static boolean isValidClickToCallNumber(String value) {
        if (value == null || value.length() < 2) {
            return false;
        }

        if (!StringUtils.isNumeric(value.substring(1))) {
            return false;
        }

        return isValidPhoneNumber(value);
    }

    /**
     * Uses isValidPhoneToCallNumber but does not allow non-numeric values other
     * than + required in the first position
     *
     * see {@link Functions#isValidPhoneNumber(String)}
     *
     * @param String
     *            number to check
     * @return true if valid false if invalid
     */
    public static boolean isValidPhoneToCallNumber(String value) {
        if (value == null || value.length() < 2) {
            return false;
        }
        
        String localValue = value.replace(" ", "");
        if (!StringUtils.isNumeric(localValue.substring(1))) {
            return false;
        }
        return isValidPhoneNumber(localValue);
    }

    /**
     * Checks if a phone number looks correct. Currently this means it starts
     * with a '+' sign and has a reasonable number of digits. The algorithm
     * ignores spaces, parentheses, hyphens and forward slashes.
     */
    public static boolean isValidPhoneNumber(String value) {
        return isValidPhoneNumber(value, true);
    }

    /**
     * Checks if a phone number looks correct. Currently this means it starts
     * with a '+' sign (if the mustStartWithPlus argument is true) and has a
     * reasonable number of digits. The algorithm ignores spaces, parentheses,
     * hyphens and forward slashes.
     */
    public static boolean isValidPhoneNumber(String value, boolean mustStartWithPlus) {
        if (value == null) {
            return false;
        }

        int startingIndex = 0;
        if (value.startsWith("+")) {
            ++startingIndex;
        } else if (mustStartWithPlus) {
            return false;
        }

        // Strip out spaces, dashes, and parentheses; stop at the first
        // character that is not one of those or a digit
        StringBuilder sb = new StringBuilder();
        char[] exploded = value.toCharArray();
        parser: for (int i = startingIndex; i < exploded.length; i++) {
            char ch = exploded[i];
            switch (ch) {
            case '(':
            case ')':
            case ' ':
            case '-':
            case '/':
                break;
            case '0':
            case '1':
            case '2':
            case '3':
            case '4':
            case '5':
            case '6':
            case '7':
            case '8':
            case '9':
                sb.append(ch);
                break;

            default:
                if (!Character.isDigit(ch)) {
                    return false;
                }
                break parser;
            }
        }
        String rawDigits = sb.toString();

        // Special tests for US and UK numbers
        if (rawDigits.startsWith("1")) {
            // North American Numbering Plan, +1 + 10 digits
            return rawDigits.length() == 11;
        }

        if (rawDigits.startsWith("44")) {
            // British numbers are usually 10,
            // and per 1028 may be as few as code + six digits
            return (rawDigits.length() >= 8) && (rawDigits.length() <= 13);
        }

        // For rest-of-world, country codes are at least 2 digits long,
        // and let's assume a minimum of 5 digits of phone number.
        // In most cases that's probably not enough, but whatever.
        return rawDigits.length() > 7;
    }

    /**
     * uses a static UrlValidator configured for specific schemes.
     *
     */
    public static boolean isValidURL(String url) {

        if (url == null || WHITESPACE_ANYWHERE_PATTERN.matcher(url).find()) {
            return false;
        }

        if (url.length() > URL_MAX_LENGTH) {
            return false;
        }

        if (url.startsWith("http://") || url.startsWith("https://")) {
            return urlValidator.isValid(url);
        }
        // Android Market pseudo-URLs market://a?b=c
        if (url.startsWith("market://")) {
            return url.length() >= 14;
        }
        // Plugin special
        if (url.startsWith("plugin:")) {
            return true;
        }
        return false;
    }

}
