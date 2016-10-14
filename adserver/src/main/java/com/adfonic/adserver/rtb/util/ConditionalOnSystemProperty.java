package com.adfonic.adserver.rtb.util;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Conditional;

/**
 * 
 * @author mvanek
 *
 */
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Conditional(OnSystemPropertyCondition.class)
public @interface ConditionalOnSystemProperty {

    public static final String PROPERTY_IS_NULL = "!-sysprop-is-null-!";

    // Null surrogate as annotations cannot use "default null"
    public static final String PROPERTY_EXIST = "!-sysprop-not-null-!";

    public String name();

    public String value() default PROPERTY_EXIST;

}
