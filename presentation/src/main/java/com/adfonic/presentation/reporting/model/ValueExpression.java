package com.adfonic.presentation.reporting.model;

/**
 * Class to represent an expression which contains a simple value
 * 
 * @author David Martin
 */
public class ValueExpression implements Expression{
    
    /** Simple expression value */ 
    private String value;

    /**
     * Constructor
     * 
     * @param value Value to add 
     */
    public ValueExpression(String value) {
        this.value = value;
    }

    // Getters
    public String getValue() {
        return value;
    }

    /*
     * (non-Javadoc)
     * @see com.adfonic.presentation.reporting.model.Expression#evaluate(com.adfonic.presentation.reporting.model.ExpressionEvaluator)
     */
    @Override
    public String evaluate(ExpressionEvaluator evaluator) {
        return value;
    }
}
