package com.adfonic.domain;

import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(ExtendedCreativeTypeMacro.class)
public abstract class ExtendedCreativeTypeMacro_ {

	public static volatile SingularAttribute<ExtendedCreativeTypeMacro, String> replacementString;
	public static volatile SingularAttribute<ExtendedCreativeTypeMacro, String> matchString;
	public static volatile SingularAttribute<ExtendedCreativeTypeMacro, Long> id;
	public static volatile SingularAttribute<ExtendedCreativeTypeMacro, ExtendedCreativeType> extendedCreativeType;

}

