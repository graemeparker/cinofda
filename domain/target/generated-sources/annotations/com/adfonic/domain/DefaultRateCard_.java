package com.adfonic.domain;

import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(DefaultRateCard.class)
public abstract class DefaultRateCard_ {

	public static volatile SingularAttribute<DefaultRateCard, RateCard> rateCard;
	public static volatile SingularAttribute<DefaultRateCard, Long> id;
	public static volatile SingularAttribute<DefaultRateCard, BidType> bidType;

}

