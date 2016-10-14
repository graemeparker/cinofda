package com.adfonic.reporting.service.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.adfonic.reporting.service.parameter.Group;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Groups {
	
	/**
	 * Enum array of valid groups 
	 * @return
	 */
	Group[] values();
}
