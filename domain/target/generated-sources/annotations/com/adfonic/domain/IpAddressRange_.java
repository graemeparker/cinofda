package com.adfonic.domain;

import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(IpAddressRange.class)
public abstract class IpAddressRange_ {

	public static volatile SingularAttribute<IpAddressRange, Long> endPoint;
	public static volatile SingularAttribute<IpAddressRange, Long> startPoint;
	public static volatile SingularAttribute<IpAddressRange, Long> id;

}

