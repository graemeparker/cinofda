package com.adfonic.presentation.reporting.model;

/**
 * Interface to reflect the operations that all expression have to implement
 * 
 * @author David Martin
 */
public interface Expression {

    /**
     * This method evaluate the expression using the ExpressionEvaluator 
     * passed by input parameter
     * 
     * @param evaluator Expression evaluator passed to resolve the expression. 
     * This parameter depends of the file service and its implementation 
     * 
     * @return String with the expression evaluated
     */
    public String evaluate(ExpressionEvaluator evaluator);
}
