package com.adfonic.reporting.service.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.adfonic.reporting.service.parameter.Filter;


@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface Filters {
	
	/**
	 * Enum array of valid filters 
	 * @return
	 */
	Filter[] values();
}
