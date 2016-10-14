package com.adfonic.domain;

import javax.persistence.metamodel.MapAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(Component.class)
public abstract class Component_ {

	public static volatile SingularAttribute<Component, String> systemName;
	public static volatile SingularAttribute<Component, Format> format;
	public static volatile SingularAttribute<Component, String> name;
	public static volatile SingularAttribute<Component, Long> id;
	public static volatile MapAttribute<Component, DisplayType, ContentSpec> contentSpecMap;
	public static volatile SingularAttribute<Component, Boolean> required;

}

