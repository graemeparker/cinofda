package com.adfonic.domain;

import javax.persistence.metamodel.MapAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(AdserverPlugin.class)
public abstract class AdserverPlugin_ {

	public static volatile SingularAttribute<AdserverPlugin, String> systemName;
	public static volatile SingularAttribute<AdserverPlugin, String> name;
	public static volatile SingularAttribute<AdserverPlugin, Long> id;
	public static volatile SingularAttribute<AdserverPlugin, Boolean> enabled;
	public static volatile SingularAttribute<AdserverPlugin, Long> expectedResponseTimeMillis;
	public static volatile MapAttribute<AdserverPlugin, String, String> properties;

}

