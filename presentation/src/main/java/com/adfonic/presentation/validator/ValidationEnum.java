package com.adfonic.presentation.validator;

 enum ValidationEnum {

    // Generic validation messages
    NULL                    ("page.error.validation.null"),               
    EMPTY                   ("page.error.validation.empty"),

    // URL validation messages
    URL_UNSUPPORTED_SCHEME  ("page.error.validation.url.scheme"),
    URL_WRONG_FORMAT        ("page.error.validation.url.format"),
    URL_MARKET_LENGTH       ("page.error.validation.url.market.scheme.length"),
    URL_WHITESPACE          ("page.error.validation.url.whitespace"),
    URL_MAX_LENGTH          ("page.error.validation.url.length");
    
    private String key;

    ValidationEnum(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }
}
