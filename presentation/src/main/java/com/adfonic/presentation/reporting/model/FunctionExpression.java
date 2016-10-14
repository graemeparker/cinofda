package com.adfonic.presentation.reporting.model;

/**
 * Class to represent an expression which consists on a function (function) 
 * with one parameter (columnName). This parameter is a reference to a dataset 
 * of one concrete column of the report.
 * 
 * @author David Martin
 */
public class FunctionExpression implements Expression{

    /** Enum type to define existing function */
    public enum Function {
        SUM
    }
    
    /** 
     * Function to apply 
     * @see Function */
    private Function function;
    
    /** Column name where the function apply */
    private String columnName;
    
    /**
     * Constructor
     * 
     * @param function Function to apply
     * @param columnName Column name where the function apply
     */
    public FunctionExpression(Function function, String columnName) {
        this.function = function;
        this.columnName = columnName;
    }

    // Getters
    public Function getFunction() {
        return function;
    }

    public String getColumnName() {
        return columnName;
    }

    /*
     * (non-Javadoc)
     * @see com.adfonic.presentation.reporting.model.Expression#evaluate(com.adfonic.presentation.reporting.model.ExpressionEvaluator)
     */
    @Override
    public String evaluate(ExpressionEvaluator evaluator) {
        String evaluation = null;
        if (function!=null && columnName!=null){
            String dataSet = evaluator.evaluateDataset(columnName);
            evaluation = evaluator.evaluateFunction(function, dataSet);
        }
        return evaluation;
    }
}
