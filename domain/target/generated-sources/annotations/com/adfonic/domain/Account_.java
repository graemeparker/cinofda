package com.adfonic.domain;

import java.math.BigDecimal;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(Account.class)
public abstract class Account_ {

	public static volatile SingularAttribute<Account, BigDecimal> balance;
	public static volatile SingularAttribute<Account, AccountType> accountType;
	public static volatile SingularAttribute<Account, Long> id;

}

