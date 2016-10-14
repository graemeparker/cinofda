package com.adfonic.domain;

import java.math.BigDecimal;
import java.util.Date;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(AccountDetail.class)
public abstract class AccountDetail_ {

	public static volatile SingularAttribute<AccountDetail, TransactionType> transactionType;
	public static volatile SingularAttribute<AccountDetail, String> reference;
	public static volatile SingularAttribute<AccountDetail, BigDecimal> amount;
	public static volatile SingularAttribute<AccountDetail, BigDecimal> total;
	public static volatile SingularAttribute<AccountDetail, String> description;
	public static volatile SingularAttribute<AccountDetail, String> opportunity;
	public static volatile SingularAttribute<AccountDetail, BigDecimal> tax;
	public static volatile SingularAttribute<AccountDetail, Long> id;
	public static volatile SingularAttribute<AccountDetail, Date> transactionTime;
	public static volatile SingularAttribute<AccountDetail, Account> account;

}

