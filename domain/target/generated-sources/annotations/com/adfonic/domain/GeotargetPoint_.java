package com.adfonic.domain;

import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(GeotargetPoint.class)
public abstract class GeotargetPoint_ {

	public static volatile SingularAttribute<GeotargetPoint, Geotarget> geotarget;
	public static volatile SingularAttribute<GeotargetPoint, Double> latitude;
	public static volatile SingularAttribute<GeotargetPoint, Long> id;
	public static volatile SingularAttribute<GeotargetPoint, Double> longitude;

}

