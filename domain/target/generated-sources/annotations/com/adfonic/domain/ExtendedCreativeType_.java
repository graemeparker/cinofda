package com.adfonic.domain;

import javax.persistence.metamodel.MapAttribute;
import javax.persistence.metamodel.SetAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(ExtendedCreativeType.class)
public abstract class ExtendedCreativeType_ {

	public static volatile SetAttribute<ExtendedCreativeType, Feature> features;
	public static volatile SetAttribute<ExtendedCreativeType, ExtendedCreativeTypeMacro> macros;
	public static volatile SingularAttribute<ExtendedCreativeType, Boolean> hidden;
	public static volatile SingularAttribute<ExtendedCreativeType, String> name;
	public static volatile MapAttribute<ExtendedCreativeType, ContentForm, String> templateMap;
	public static volatile SingularAttribute<ExtendedCreativeType, MediaType> mediaType;
	public static volatile SingularAttribute<ExtendedCreativeType, Long> id;
	public static volatile SingularAttribute<ExtendedCreativeType, Boolean> useDynamicTemplates;
	public static volatile SingularAttribute<ExtendedCreativeType, Boolean> clickRedirectRequired;

}

