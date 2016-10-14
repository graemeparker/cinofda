package com.adfonic.domain;

import java.math.BigDecimal;
import javax.persistence.metamodel.MapAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(RateCard.class)
public abstract class RateCard_ {

	public static volatile MapAttribute<RateCard, Country, BigDecimal> minimumBidMap;
	public static volatile SingularAttribute<RateCard, BigDecimal> defaultMinimum;
	public static volatile SingularAttribute<RateCard, Long> id;

}

