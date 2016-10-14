package com.adfonic.domain;

import java.math.BigDecimal;
import java.util.Date;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(AccountFixedMargin.class)
public abstract class AccountFixedMargin_ {

	public static volatile SingularAttribute<AccountFixedMargin, BigDecimal> margin;
	public static volatile SingularAttribute<AccountFixedMargin, Date> endDate;
	public static volatile SingularAttribute<AccountFixedMargin, Company> company;
	public static volatile SingularAttribute<AccountFixedMargin, Long> id;
	public static volatile SingularAttribute<AccountFixedMargin, Date> startDate;

}

