package com.adfonic.domain;

import java.math.BigDecimal;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(LocationTarget.class)
public abstract class LocationTarget_ {

	public static volatile SingularAttribute<LocationTarget, Advertiser> advertiser;
	public static volatile SingularAttribute<LocationTarget, BigDecimal> latitude;
	public static volatile SingularAttribute<LocationTarget, String> name;
	public static volatile SingularAttribute<LocationTarget, Long> id;
	public static volatile SingularAttribute<LocationTarget, BigDecimal> radiusMiles;
	public static volatile SingularAttribute<LocationTarget, BigDecimal> longitude;

}

