package com.adfonic.domain;

import javax.persistence.metamodel.MapAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(Browser.class)
public abstract class Browser_ {

	public static volatile SingularAttribute<Browser, Integer> browserOrder;
	public static volatile MapAttribute<Browser, String, String> headerMap;
	public static volatile SingularAttribute<Browser, String> name;
	public static volatile SingularAttribute<Browser, Long> id;

}

