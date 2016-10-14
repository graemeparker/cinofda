package com.adfonic.presentation.validator;

/**
 * Util class for all common validations
 * 
 * @author Attila
 */
public class ValidationUtils {
    
    private ValidationUtils(){
    }
    
    // URL validation related methods
    
    public static ValidationResult validateUrl(String url) {
        return validateUrl(url, true);
    }
    
    public static ValidationResult validateUrl(String url, boolean required) {
        return new ValidationResult(URLValidator.getInstance().validate(url, required));
    }
    
    public static int getUrlMaxLength() {
        return URLValidator.URL_MAX_LENGTH;
    }
    
}
