package com.adfonic.domain;

import java.math.BigDecimal;
import java.util.Date;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(CampaignAgencyDiscount.class)
public abstract class CampaignAgencyDiscount_ {

	public static volatile SingularAttribute<CampaignAgencyDiscount, Date> endDate;
	public static volatile SingularAttribute<CampaignAgencyDiscount, Campaign> campaign;
	public static volatile SingularAttribute<CampaignAgencyDiscount, BigDecimal> discount;
	public static volatile SingularAttribute<CampaignAgencyDiscount, Long> id;
	public static volatile SingularAttribute<CampaignAgencyDiscount, Date> startDate;

}

