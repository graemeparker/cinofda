package com.adfonic.domain;

import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(Platform.class)
public abstract class Platform_ {

	public static volatile SingularAttribute<Platform, String> systemName;
	public static volatile SingularAttribute<Platform, String> name;
	public static volatile SingularAttribute<Platform, String> description;
	public static volatile SingularAttribute<Platform, Long> id;
	public static volatile SingularAttribute<Platform, String> constraints;

}

