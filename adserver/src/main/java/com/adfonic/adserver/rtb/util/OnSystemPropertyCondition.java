package com.adfonic.adserver.rtb.util;

import java.util.Map;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * 
 * @author mvanek
 *
 */
public class OnSystemPropertyCondition implements Condition {

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        Map<String, Object> attributes = metadata.getAnnotationAttributes(ConditionalOnSystemProperty.class.getName());
        String systemPropertyName = (String) attributes.get("name");
        String expectedSystemPropertyValue = (String) attributes.get("value");

        String propertyValue = System.getProperty(systemPropertyName);
        if (expectedSystemPropertyValue.equals(ConditionalOnSystemProperty.PROPERTY_EXIST)) {
            return propertyValue != null; //just exist - value is irelevant
        } else if (expectedSystemPropertyValue.equals(ConditionalOnSystemProperty.PROPERTY_IS_NULL)) {
            return propertyValue == null;
        } else {
            return expectedSystemPropertyValue.equals(propertyValue);
        }
    }
}
