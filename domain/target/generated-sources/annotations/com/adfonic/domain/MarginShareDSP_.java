package com.adfonic.domain;

import java.math.BigDecimal;
import java.util.Date;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(MarginShareDSP.class)
public abstract class MarginShareDSP_ {

	public static volatile SingularAttribute<MarginShareDSP, BigDecimal> margin;
	public static volatile SingularAttribute<MarginShareDSP, Date> endDate;
	public static volatile SingularAttribute<MarginShareDSP, Company> company;
	public static volatile SingularAttribute<MarginShareDSP, Long> id;
	public static volatile SingularAttribute<MarginShareDSP, Date> startDate;

}

