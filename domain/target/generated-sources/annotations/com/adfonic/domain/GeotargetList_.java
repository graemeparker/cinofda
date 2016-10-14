package com.adfonic.domain;

import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(GeotargetList.class)
public abstract class GeotargetList_ {

	public static volatile SingularAttribute<GeotargetList, Geotarget> geotarget;
	public static volatile SingularAttribute<GeotargetList, Long> geotargetById;
	public static volatile SingularAttribute<GeotargetList, Long> id;

}

