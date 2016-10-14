package com.adfonic.domain;

import javax.persistence.metamodel.SetAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(OperatorGroup.class)
public abstract class OperatorGroup_ {

	public static volatile SetAttribute<OperatorGroup, Operator> operators;
	public static volatile SingularAttribute<OperatorGroup, String> name;
	public static volatile SingularAttribute<OperatorGroup, Long> id;

}

