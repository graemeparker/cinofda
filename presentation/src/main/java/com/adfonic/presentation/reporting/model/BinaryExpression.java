package com.adfonic.presentation.reporting.model;

/**
 * This class represents a binary expression. A binary expression is defined as one operation between two elements. 
 * This class uses Expression interface to represent the two elements of the operation and define a set of operators 
 * that can be used to perform the operation. 
 * 
 * @author David Martin
 */
public class BinaryExpression implements Expression{
    
    /** 
     * First part of the operation
     * @see Expression */
    private Expression expression1;
    
    /** 
     * Second part of the operation
     * @see Expression */
    private Expression expression2;
    
    /**
     * Operator which applies in the operation
     * @see Operator */
    private Operator operator;
    
    /**
     * 
     * @param expression1 First part of the operation
     * @param operator Operator which applies in the operation
     * @param expression2 Second part of the operation
     */
    public BinaryExpression(Expression expression1, Operator operator, Expression expression2) {
        this.expression1 = expression1;
        this.expression2 = expression2;
        this.operator = operator;
    }
    
    // Getters
    public Expression getExpression1() {
        return expression1;
    }
    public Expression getExpression2() {
        return expression2;
    }
    public Operator getOperator() {
        return operator;
    }

    /*
     * (non-Javadoc)
     * @see com.adfonic.presentation.reporting.model.Expression#evaluate(com.adfonic.presentation.reporting.model.ExpressionEvaluator)
     */
    @Override
    public String evaluate(ExpressionEvaluator evaluator) {
        String evaluation = "";
        if (expression1!=null && expression2!=null && operator!=null){
            evaluation = evaluator.evaluateOperator(expression1.evaluate(evaluator), operator, expression2.evaluate(evaluator));
        }
        return evaluation;
    }
    
    /** Enum type to define the kind of operators that can apply into a binary expression */
    public enum Operator {
        DIV('/');
        private char value;

        private Operator(char value) {
                this.value = value;
        }
        
        public String getStringOperator(){
            return String.valueOf(value);
        }
    }
}
