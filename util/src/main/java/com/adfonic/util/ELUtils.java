package com.adfonic.util;

import java.util.Map;

import javax.el.ELContext;
import javax.el.ELResolver;
import javax.el.ExpressionFactory;

/**
 * Utilities for evaluating EL templates in a provider-agnostic manner.
 */
public final class ELUtils {
    private ELUtils() {
    }

    /**
     * Evaluate an EL template
     */
    public static String evaluateTemplate(String template, Map<String, Object> templateProperties, ELContext elContext, ExpressionFactory expressionFactory) {
        if (templateProperties != null) {
            ELResolver elResolver = elContext.getELResolver();
            for (Map.Entry<String, Object> entry : templateProperties.entrySet()) {
                elResolver.setValue(elContext, null, entry.getKey(), entry.getValue());
            }
        }

        return (String) expressionFactory.createValueExpression(elContext, template, String.class).getValue(elContext);
    }
}
