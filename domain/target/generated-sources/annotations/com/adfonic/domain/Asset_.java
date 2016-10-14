package com.adfonic.domain;

import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(Asset.class)
public abstract class Asset_ {

	public static volatile SingularAttribute<Asset, byte[]> data;
	public static volatile SingularAttribute<Asset, String> externalID;
	public static volatile SingularAttribute<Asset, Long> id;
	public static volatile SingularAttribute<Asset, ContentType> contentType;
	public static volatile SingularAttribute<Asset, Creative> creative;

}

