package com.adfonic.domain;

import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(MobileNetwork.class)
public abstract class MobileNetwork_ {

	public static volatile SingularAttribute<MobileNetwork, String> mnc;
	public static volatile SingularAttribute<MobileNetwork, Long> id;
	public static volatile SingularAttribute<MobileNetwork, String> mcc;
	public static volatile SingularAttribute<MobileNetwork, Operator> operator;

}

