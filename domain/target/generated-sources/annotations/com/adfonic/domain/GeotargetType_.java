package com.adfonic.domain;

import javax.persistence.metamodel.SetAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(GeotargetType.class)
public abstract class GeotargetType_ {

	public static volatile SingularAttribute<GeotargetType, String> name;
	public static volatile SingularAttribute<GeotargetType, Long> id;
	public static volatile SetAttribute<GeotargetType, Country> countries;
	public static volatile SingularAttribute<GeotargetType, String> type;

}

