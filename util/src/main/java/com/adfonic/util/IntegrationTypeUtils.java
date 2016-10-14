package com.adfonic.util;

import java.util.regex.Pattern;

public class IntegrationTypeUtils {
    // This pattern will match the <prefix>/<version> pattern (i.e.
    // "Mobclix/iphone/4.3.1")
    // prefix := everything prior to the last slash (i.e. "Mobclix/iphone")
    // version := everything after the last slash (i.e. "4.3.1")
    public static final Pattern INTEGRATION_TYPE_PATTERN = Pattern.compile("^(.+)/([^/]+)$");

    private static final int MAX_DIGITS_PER_TOKEN = 3;
    private static final int MAX_TOKENS = 3;

    // Let's say we have 3 digits max per token and 3 tokens max (i.e.
    // 123.456.789).
    // We start out with a multiplier of 1,000,000 (10^6), and each time we
    // encounter
    // another token we divide by 1,000 (10^3).
    private static final int FIRST_TOKEN_MULTIPLIER = (int) Math.pow(10, MAX_DIGITS_PER_TOKEN * (MAX_TOKENS - 1));
    private static final int NEXT_TOKEN_DIVISOR = (int) Math.pow(10, MAX_DIGITS_PER_TOKEN);

    // When we have 3 digits max per token, we only support up to (including)
    // 999
    // in each token value. This will be used to validate that.
    private static final int MAX_TOKEN_VALUE = NEXT_TOKEN_DIVISOR - 1;
    
    private IntegrationTypeUtils(){
    }

    /**
     * Parse an IntegrationType version to its numeric value. This method takes
     * the "version" component of a "prefix/version" string representing an
     * IntegrationType (i.e. "Mobclix/iphone/3.1.2") and parses out a numeric
     * value that can be used in range comparisons.
     *
     * Three numeric tokens are expected with a dot separator in between. Fewer
     * tokens are supported, and any non-digit characters at the end, or
     * additional tokens will be ignored.
     *
     * For example:
     *
     * "4.1.8" == 4001008 "4.1.8.2" == 4001008 "4.1.8-alpha1" == 4001008
     *
     * "4.1" == 4001000 "4.1rc2" == 4001000
     *
     * Null will be returned if: a) The version string starts with anything
     * other than a digit. b) The version string contains two dots in
     * succession.
     *
     * @param version
     *            the version string to parse
     * @return a numeric value between 0 and 999999999 representing the version
     */
    public static Integer parseVersionValue(String version) {
        int multiplier = FIRST_TOKEN_MULTIPLIER;
        StringBuilder token = new StringBuilder(version.length());
        char c;
        int value = 0;
        int position = 0;
        for (; position < version.length(); ++position) {
            c = version.charAt(position);
            if (c >= '0' && c <= '9') {
                // Append numeric chars to the current token
                token.append(c);
            } else if (c == '.') {
                // It's the end of a token
                if (token.length() == 0) {
                    return null; // two dots in a row, or started with a dot
                }

                int tokenValue = Integer.parseInt(token.toString());
                if (tokenValue > MAX_TOKEN_VALUE) {
                    return null; // illegal value
                }
                value += tokenValue * multiplier;
                token.setLength(0);

                if (multiplier == 1) {
                    // Already in the last expected token, so stop here
                    break;
                } else {
                    // Advance to the next token
                    multiplier /= NEXT_TOKEN_DIVISOR;
                }
            } else {
                // Non-dot and non-digit, so stop here
                break;
            }
        }

        if (position == 0) {
            return null; // no numeric characters found at all
        }

        // Add the value of the last token
        if (token.length() > 0) {
            int tokenValue = Integer.parseInt(token.toString());
            if (tokenValue > MAX_TOKEN_VALUE) {
                return null;
            }
            value += tokenValue * multiplier;
        }

        return value;
    }
}
