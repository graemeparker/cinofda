package com.adfonic.presentation.validator;

/**
 * Store the validation related data
 * 
 * @author Attila
 *
 */
public class ValidationResult {
    
    private ValidationEnum validationEnum;
    
    public ValidationResult(ValidationEnum validationEnum) {
        this.validationEnum = validationEnum;
    }
    
    public String getMessageKey() {
        return validationEnum.getKey();
    }
    
    public boolean isFailed() {
        return (validationEnum != null) ? true : false;
    }

}
