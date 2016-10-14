package com.adfonic.domain;

/**
 * Annotation used to express that the annotated field is deliberately not
 * included when the entity is being copied, i.e. in a copyFrom() method.
 */
public @interface NotCopied {
    String value() default "";
}