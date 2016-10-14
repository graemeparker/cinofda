package com.adfonic.domain;

import javax.persistence.metamodel.SetAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(Region.class)
public abstract class Region_ {

	public static volatile SingularAttribute<Region, String> name;
	public static volatile SingularAttribute<Region, Long> id;
	public static volatile SetAttribute<Region, Country> countries;

}

