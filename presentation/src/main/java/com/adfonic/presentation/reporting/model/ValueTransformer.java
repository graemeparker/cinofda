package com.adfonic.presentation.reporting.model;

/**
 * Responsibility: To provide an alternative way to modify values once the field 
 * iterator has got its value. 
 * Use in case the cells of one column could have more than 1 possible format.
 * 
 * @author David Martin
 *
 * @param <T> Row class
 */
public interface ValueTransformer {
    /**
     * Transform a field value
     * 
     * @param fieldName Field name
     * @param value Value to transform
     * 
     * @return Transformed value
     */
    public Object transform(String fieldName, Object value);
}
