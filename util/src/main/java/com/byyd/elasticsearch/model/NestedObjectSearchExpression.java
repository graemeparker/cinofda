package com.byyd.elasticsearch.model;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.NotImplementedException;

public class NestedObjectSearchExpression implements SearchExpression {

    private String nestedObjectname;
    List<SimpleSearchExpression> termsExpressions;
    private Boolean addNotFilter;

    public NestedObjectSearchExpression(String nestedObjectname, List<SimpleSearchExpression> termsExpressions, Boolean addNotFilter) {
        this.nestedObjectname = nestedObjectname;
        if (termsExpressions == null) {
            this.termsExpressions = new ArrayList<>();
        } else {
            this.termsExpressions = termsExpressions;
        }
        this.addNotFilter = addNotFilter;
    }

    public String getNestedObjectname() {
        return nestedObjectname;
    }

    public List<SimpleSearchExpression> getTermsExpressions() {
        return termsExpressions;
    }

    public Boolean getAddNotFilter() {
        return addNotFilter;
    }

    @Override
    public String evaluate() {
        throw new NotImplementedException("evaluate() not implemented for class " + this.getClass().getName());
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("NestedSearchExpression");
        StringBuilder sb = new StringBuilder();
        for (SimpleSearchExpression expression : termsExpressions) {
            sb.append(expression.evaluate());
        }
        if (addNotFilter) {
            builder.append(" with NOT filter");
        }
        builder.append(" for object '").append(this.nestedObjectname).append("' and term expressions ").append(sb);
        return builder.toString();
    }
}
