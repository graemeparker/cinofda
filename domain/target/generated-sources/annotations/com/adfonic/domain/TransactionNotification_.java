package com.adfonic.domain;

import java.math.BigDecimal;
import java.util.Date;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(TransactionNotification.class)
public abstract class TransactionNotification_ {

	public static volatile SingularAttribute<TransactionNotification, Advertiser> advertiser;
	public static volatile SingularAttribute<TransactionNotification, String> reference;
	public static volatile SingularAttribute<TransactionNotification, BigDecimal> amount;
	public static volatile SingularAttribute<TransactionNotification, Long> id;
	public static volatile SingularAttribute<TransactionNotification, User> user;
	public static volatile SingularAttribute<TransactionNotification, Date> timestamp;

}

