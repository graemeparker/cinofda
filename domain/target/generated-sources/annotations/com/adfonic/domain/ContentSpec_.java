package com.adfonic.domain;

import javax.persistence.metamodel.SetAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(ContentSpec.class)
public abstract class ContentSpec_ {

	public static volatile SingularAttribute<ContentSpec, String> manifest;
	public static volatile SingularAttribute<ContentSpec, String> name;
	public static volatile SingularAttribute<ContentSpec, Long> id;
	public static volatile SetAttribute<ContentSpec, ContentType> contentTypes;

}

