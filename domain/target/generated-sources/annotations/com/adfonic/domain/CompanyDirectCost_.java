package com.adfonic.domain;

import java.math.BigDecimal;
import java.util.Date;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(CompanyDirectCost.class)
public abstract class CompanyDirectCost_ {

	public static volatile SingularAttribute<CompanyDirectCost, Date> endDate;
	public static volatile SingularAttribute<CompanyDirectCost, BigDecimal> directCost;
	public static volatile SingularAttribute<CompanyDirectCost, Company> company;
	public static volatile SingularAttribute<CompanyDirectCost, Long> id;
	public static volatile SingularAttribute<CompanyDirectCost, Date> startDate;

}

