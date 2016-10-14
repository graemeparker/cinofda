package com.adfonic.domain;

import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(DisplayType.class)
public abstract class DisplayType_ {

	public static volatile SingularAttribute<DisplayType, String> systemName;
	public static volatile SingularAttribute<DisplayType, String> name;
	public static volatile SingularAttribute<DisplayType, Long> id;
	public static volatile SingularAttribute<DisplayType, String> constraints;

}

