package com.byyd.elasticsearch.model;

public class BinarySearchExpression implements SearchExpression{
    
    public enum SearchLogicalOperator {
        AND("AND"),
        OR("OR");
        
        private String operator;
        private SearchLogicalOperator(String operator){
            this.operator = operator;
        }
        public String getValue() {
            return operator;
        }
    }
    
    private SearchExpression exp1;
    private SearchExpression exp2;
    private SearchLogicalOperator logicalOperator;
    
    public BinarySearchExpression(SearchExpression exp1, SearchExpression exp2, SearchLogicalOperator logicalOperator) {
        super();
        this.exp1 = exp1;
        this.exp2 = exp2;
        this.logicalOperator = logicalOperator;
    }

    public SearchExpression getExp1() {
        return exp1;
    }

    public SearchExpression getExp2() {
        return exp2;
    }

    public SearchLogicalOperator getLogicalOperator() {
        return logicalOperator;
    }

    @Override
    public String evaluate(){
        return new StringBuilder("(").append(exp1.evaluate()).append(logicalOperator.getValue()).append(exp2.evaluate()).append(")").toString();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("BinarySearchExpression [exp1=(").append(exp1.toString()).append("), exp2=(").append(exp2.toString()).append("), logicalOperator=").append(logicalOperator.getValue()).append("]");
        return builder.toString();
    }
}
