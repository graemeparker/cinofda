package com.adfonic.domain;

import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(Geotarget.class)
public abstract class Geotarget_ {

	public static volatile SingularAttribute<Geotarget, Country> country;
	public static volatile SingularAttribute<Geotarget, Double> displayLatitude;
	public static volatile SingularAttribute<Geotarget, Double> displayLongitude;
	public static volatile SingularAttribute<Geotarget, GeotargetType> geotargetType;
	public static volatile SingularAttribute<Geotarget, String> name;
	public static volatile SingularAttribute<Geotarget, Long> id;

}

