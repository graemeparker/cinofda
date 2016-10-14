package com.adfonic.domain;

import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(ContentType.class)
public abstract class ContentType_ {

	public static volatile SingularAttribute<ContentType, String> name;
	public static volatile SingularAttribute<ContentType, Boolean> animated;
	public static volatile SingularAttribute<ContentType, Long> id;
	public static volatile SingularAttribute<ContentType, String> mimeType;

}

