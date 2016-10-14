package com.adfonic.domain;

import javax.persistence.metamodel.MapAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(AssetBundle.class)
public abstract class AssetBundle_ {

	public static volatile SingularAttribute<AssetBundle, DisplayType> displayType;
	public static volatile SingularAttribute<AssetBundle, Long> id;
	public static volatile MapAttribute<AssetBundle, Component, Asset> assetMap;
	public static volatile SingularAttribute<AssetBundle, Creative> creative;

}

