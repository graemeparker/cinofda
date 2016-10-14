package com.adfonic.domain;

import java.math.BigDecimal;
import javax.persistence.metamodel.MapAttribute;
import javax.persistence.metamodel.SetAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(TransparentNetwork.class)
public abstract class TransparentNetwork_ {

	public static volatile MapAttribute<TransparentNetwork, Country, Float> topCountries;
	public static volatile SetAttribute<TransparentNetwork, Company> advertisers;
	public static volatile SingularAttribute<TransparentNetwork, UploadedContent> icon;
	public static volatile SingularAttribute<TransparentNetwork, BigDecimal> clickThroughRate;
	public static volatile SingularAttribute<TransparentNetwork, String> description;
	public static volatile SingularAttribute<TransparentNetwork, Integer> requests;
	public static volatile MapAttribute<TransparentNetwork, BidType, RateCard> rateCardMap;
	public static volatile SingularAttribute<TransparentNetwork, Integer> uniqueUsers;
	public static volatile SingularAttribute<TransparentNetwork, String> name;
	public static volatile SingularAttribute<TransparentNetwork, UploadedContent> screenShot;
	public static volatile SingularAttribute<TransparentNetwork, Boolean> closed;
	public static volatile SingularAttribute<TransparentNetwork, Long> id;
	public static volatile SetAttribute<TransparentNetwork, Publication> publications;

}

