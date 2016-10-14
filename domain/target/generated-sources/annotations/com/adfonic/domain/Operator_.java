package com.adfonic.domain;

import javax.persistence.metamodel.SetAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(Operator.class)
public abstract class Operator_ {

	public static volatile SingularAttribute<Operator, Country> country;
	public static volatile SetAttribute<Operator, String> aliases;
	public static volatile SetAttribute<Operator, OperatorAlias> operatorAliases;
	public static volatile SingularAttribute<Operator, String> name;
	public static volatile SingularAttribute<Operator, Long> id;
	public static volatile SingularAttribute<Operator, Boolean> mobileOperator;
	public static volatile SingularAttribute<Operator, OperatorGroup> group;

}

