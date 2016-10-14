package com.adfonic.domain;

import java.util.Date;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(Audit.class)
public abstract class Audit_ {

	public static volatile SingularAttribute<Audit, String> query;
	public static volatile SingularAttribute<Audit, String> className;
	public static volatile SingularAttribute<Audit, Long> id;
	public static volatile SingularAttribute<Audit, Date> transactionTime;

}

