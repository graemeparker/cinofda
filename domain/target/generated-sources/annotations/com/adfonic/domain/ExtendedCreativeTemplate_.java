package com.adfonic.domain;

import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(ExtendedCreativeTemplate.class)
public abstract class ExtendedCreativeTemplate_ {

	public static volatile SingularAttribute<ExtendedCreativeTemplate, String> templateOriginal;
	public static volatile SingularAttribute<ExtendedCreativeTemplate, ContentForm> contentForm;
	public static volatile SingularAttribute<ExtendedCreativeTemplate, Long> id;
	public static volatile SingularAttribute<ExtendedCreativeTemplate, String> templatePreprocessed;
	public static volatile SingularAttribute<ExtendedCreativeTemplate, Creative> creative;

}

