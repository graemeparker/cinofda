package com.adfonic.presentation.validator;

import org.apache.commons.lang.StringUtils;

/**
 * Base class for all validators.
 * 
 * @author Attila
 *
 */
abstract class AbstractValidator {

    /**
     * Validate the input based on specific validation rules in the child class.
     * 
     * @param text the parameter to be validated
     * @param required if the extra null/empty check is needed
     * 
     * @return true if the input is valid false otherwise
     */
    public ValidationEnum validate(String text, boolean required) {

        String trimmed = (text == null) ? null : text.trim();

        if (required) {
            if (trimmed == null) {
                return ValidationEnum.NULL;
            } else if (trimmed.isEmpty()) {
                return ValidationEnum.EMPTY;
            } else {
                return validateChild(trimmed);
            }
        } else if (StringUtils.isNotBlank(trimmed)) {
            return validateChild(trimmed);
        } else {
            return null;
        }
    }

    /**
     * Further specific validation
     * 
     * @param text to be validated
     * 
     * @return whether the input is valid or not
     */
    protected abstract ValidationEnum validateChild(String text);

}
