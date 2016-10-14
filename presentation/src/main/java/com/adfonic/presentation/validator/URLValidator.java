package com.adfonic.presentation.validator;

import java.util.regex.Pattern;

/**
 * Common URL validator for admin and tools2
 * 
 */
class URLValidator extends AbstractValidator {
    
    private static final Pattern WHITESPACE_ANYWHERE_PATTERN = Pattern.compile("\\s+");
    
    // Pattern to validate HTTP, HTTPS, PLUGIN, MARKET schemas
    private static final String ALL_SCHEMAS_ALLOWED_STRING_PATTERN = "\\A"                        + // Beginning  
                                                                     "((http|https|market)://)"   + // HTTP, HTTPS, MARKET schemas
                                                                     "|((plugin):)"               ; // PLUGIN schemas
    private static final Pattern ALL_SCHEMAS_ALLOWED_PATTERN = Pattern.compile(ALL_SCHEMAS_ALLOWED_STRING_PATTERN);
    
    // Pattern to validate starts with HTTP, HTTPS schemas
    private static final String START_WITH_HTTP_HTTPS_STRING_PATTERN = "\\A((http|https)://)";   // HTTP, HTTPS schemas                                                                              
    private static final Pattern START_WITH_HTTP_HTTPS_PATTERN = Pattern.compile(START_WITH_HTTP_HTTPS_STRING_PATTERN);
    
    // Pattern to validate starts with MARKET schema
    private static final String  START_WITH_MARKET_STRING_PATTERN = "\\Amarket://";   // MARKET schema                                                                              
    private static final Pattern START_WITH_MARKET_PATTERN = Pattern.compile(START_WITH_MARKET_STRING_PATTERN);
    
    // Pattern to validate HTTP and HTTPS URLs
    private static final String HTTP_HTTPS_URL_STRING_PATTERN = "\\A"                                  + // Beginning  
                                                                "(https|http)://"                      + // Scheme or subdomain
                                                                "((.)+@)?"                             + // User
                                                                "[a-zA-Z0-9-_]+(\\.[a-zA-Z0-9-_]+)+"   + // Domain
                                                                "(:\\d+)?"                             + // Port
                                                                "(/[a-zA-Z0-9-_]+)*"                   + // Path
                                                                "([/?].*)?"                            + // Parameters
                                                                "\\Z";                                   // End
    private static final Pattern HTTP_HTTPS_URL_PATTERN = Pattern.compile(HTTP_HTTPS_URL_STRING_PATTERN);
    private static final int MIN_MARKET_URL_LENGHT = 14;
    protected static final int URL_MAX_LENGTH = 1024;

    // Singleton
    private static final URLValidator INSTANCE = new URLValidator();

    private URLValidator() {
    }

    public static URLValidator getInstance() {
        return INSTANCE;
    }

    @Override
    protected ValidationEnum validateChild(String url) {

        // URL length validation
        if (url.length() > URL_MAX_LENGTH) {
            return ValidationEnum.URL_MAX_LENGTH;
        }
        
        // URL scheme validation
        if (!ALL_SCHEMAS_ALLOWED_PATTERN.matcher(url).find()) {
            return ValidationEnum.URL_UNSUPPORTED_SCHEME;
        }
        
        // URL whitespace check inside
        if (WHITESPACE_ANYWHERE_PATTERN.matcher(url).find()) {
            return ValidationEnum.URL_WHITESPACE;
        }

        // URL format validation
        if (START_WITH_HTTP_HTTPS_PATTERN.matcher(url).find() && !HTTP_HTTPS_URL_PATTERN.matcher(url).find()) {
            return ValidationEnum.URL_WRONG_FORMAT;
        }

        // Android Market pseudo-URLs market://a?b=c
        if (START_WITH_MARKET_PATTERN.matcher(url).find() && url.length() < MIN_MARKET_URL_LENGHT) {
            return ValidationEnum.URL_MARKET_LENGTH;
        }

        // No validation error
        return null;
    }
}
