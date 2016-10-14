package com.byyd.elasticsearch.model;

public class SimpleSearchExpression implements SearchExpression {

    public enum SearchFieldOperator {
        NOT_EQUAL("!"), EQUAL(":"), LT(":<"), LTE(":<="), GT(":>"), GTE(":>=");
        private String operator;

        private SearchFieldOperator(String operator) {
            this.operator = operator;
        }

        public String getValue() {
            return operator;
        }
    }

    private String name;
    private String value;
    private SearchFieldOperator operator;

    /**
     * Search based on 'name' EQUAL 'value'.
     */
    public SimpleSearchExpression(String name, String value) {
        super();
        this.name = name;
        this.value = value;
        this.operator = SearchFieldOperator.EQUAL;
    }

    public SimpleSearchExpression(String name, String value, SearchFieldOperator operator) {
        super();
        this.name = name;
        this.value = value;
        this.operator = operator;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public SearchFieldOperator getOperator() {
        return operator;
    }

    @Override
    public String evaluate() {
        StringBuilder sb = new StringBuilder("(");
        if (operator == SearchFieldOperator.NOT_EQUAL) {
            sb.append(operator.getValue());
        }
        sb.append(name);
        if (operator == SearchFieldOperator.NOT_EQUAL) {
            sb.append(SearchFieldOperator.EQUAL.getValue());
        } else {
            sb.append(operator.getValue());
        }
        if (operator == SearchFieldOperator.EQUAL) {
            sb.append("\"");
        }
        sb.append(value);
        if (operator == SearchFieldOperator.EQUAL) {
            sb.append("\"");
        }
        sb.append(")");
        return sb.toString();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("SimpleSearchExpression ").append(this.evaluate());
        return builder.toString();
    }
}
