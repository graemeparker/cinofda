package com.adfonic.domain;

import java.math.BigDecimal;
import java.util.Date;
import javax.persistence.metamodel.SetAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(CampaignDataFee.class)
public abstract class CampaignDataFee_ {

	public static volatile SingularAttribute<CampaignDataFee, Date> endDate;
	public static volatile SetAttribute<CampaignDataFee, AudienceDataFee> audienceDataFee;
	public static volatile SingularAttribute<CampaignDataFee, Campaign> campaign;
	public static volatile SingularAttribute<CampaignDataFee, BigDecimal> dataFee;
	public static volatile SingularAttribute<CampaignDataFee, Long> id;
	public static volatile SingularAttribute<CampaignDataFee, Date> startDate;

}

