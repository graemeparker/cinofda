package com.byyd.elasticsearch.model;

import java.util.List;

import com.byyd.elasticsearch.model.BinarySearchExpression.SearchLogicalOperator;

public class MultipleSearchExpression implements SearchExpression {

    private List<SearchExpression> expressions;
    private SearchLogicalOperator logicalOperator;

    public MultipleSearchExpression(List<SearchExpression> expressions, SearchLogicalOperator logicalOperator) {
        super();
        this.expressions = expressions;
        this.logicalOperator = logicalOperator;
    }

    public List<SearchExpression> getExpressions() {
        return expressions;
    }

    public SearchLogicalOperator getLogicalOperator() {
        return logicalOperator;
    }

    @Override
    public String evaluate() {
        StringBuilder sb = new StringBuilder("(");
        if (!expressions.isEmpty()) {
            for (int i = 0; i < expressions.size(); i++) {
                if (i > 0) {
                    sb.append(logicalOperator);
                }
                sb.append(expressions.get(i).evaluate());
            }
        }
        return sb.append(")").toString();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("MultipleSearchExpression ").append(this.evaluate());
        return builder.toString();
    }
}
