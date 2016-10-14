package com.adfonic.presentation.reporting.model;

import com.adfonic.presentation.reporting.model.BinaryExpression.Operator;
import com.adfonic.presentation.reporting.model.FunctionExpression.Function;

/**
 * Interface define the methods to be implemented by any ExpressionEvaluator
 * 
 * @author David Martin
 */
public interface ExpressionEvaluator {
    /**
     * Apply an operator to a binary expression 
     * 
     * @param expr1 First part of the expression
     * @param operator Operator to be applied
     * @param expr2 Second part of the expression
     * 
     * @return String with the result of the evaluation 
     */
    public String evaluateOperator(String expr1, Operator operator, String expr2);
    
    /**
     * Apply the function to an expression
     * 
     * @param function Function to apply
     * @param expr Expression which is affected by the function
     * 
     * @return String with the result of the evaluation
     */
    public String evaluateFunction(Function function, String expr);
    
    /**
     * Resolve the dataset for 
     * 
     * @param columnName Column name to 
     * 
     * @return String with the result of the evaluation
     */
    public String evaluateDataset(String columnName);
}
