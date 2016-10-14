package com.adfonic.domain;

import com.adfonic.domain.MobileIpAddressRange.Source;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(MobileIpAddressRange.class)
public abstract class MobileIpAddressRange_ {

	public static volatile SingularAttribute<MobileIpAddressRange, Long> endPoint;
	public static volatile SingularAttribute<MobileIpAddressRange, Country> country;
	public static volatile SingularAttribute<MobileIpAddressRange, String> carrier;
	public static volatile SingularAttribute<MobileIpAddressRange, Long> startPoint;
	public static volatile SingularAttribute<MobileIpAddressRange, Long> id;
	public static volatile SingularAttribute<MobileIpAddressRange, Source> source;
	public static volatile SingularAttribute<MobileIpAddressRange, Integer> priority;
	public static volatile SingularAttribute<MobileIpAddressRange, Operator> operator;

}

