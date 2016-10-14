package com.adfonic.domain;

import java.math.BigDecimal;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(AudiencePrices.class)
public abstract class AudiencePrices_ {

	public static volatile SingularAttribute<AudiencePrices, BigDecimal> dataWholesale;
	public static volatile SingularAttribute<AudiencePrices, BigDecimal> dataRetail;

}

