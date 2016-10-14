package com.adfonic.domain;

import java.math.BigDecimal;
import java.util.Date;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(AdvertiserMediaCostMargin.class)
public abstract class AdvertiserMediaCostMargin_ {

	public static volatile SingularAttribute<AdvertiserMediaCostMargin, Date> endDate;
	public static volatile SingularAttribute<AdvertiserMediaCostMargin, Company> company;
	public static volatile SingularAttribute<AdvertiserMediaCostMargin, Long> id;
	public static volatile SingularAttribute<AdvertiserMediaCostMargin, Date> startDate;
	public static volatile SingularAttribute<AdvertiserMediaCostMargin, BigDecimal> mediaCostMargin;

}

