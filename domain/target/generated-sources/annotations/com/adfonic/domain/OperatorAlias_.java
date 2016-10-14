package com.adfonic.domain;

import com.adfonic.domain.OperatorAlias.Type;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(OperatorAlias.class)
public abstract class OperatorAlias_ {

	public static volatile SingularAttribute<OperatorAlias, String> alias;
	public static volatile SingularAttribute<OperatorAlias, Long> id;
	public static volatile SingularAttribute<OperatorAlias, Type> type;
	public static volatile SingularAttribute<OperatorAlias, Operator> operator;

}

