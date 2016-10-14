package com.adfonic.domain;

import java.math.BigDecimal;
import java.util.Date;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(CampaignTradingDeskMargin.class)
public abstract class CampaignTradingDeskMargin_ {

	public static volatile SingularAttribute<CampaignTradingDeskMargin, Date> endDate;
	public static volatile SingularAttribute<CampaignTradingDeskMargin, BigDecimal> tradingDeskMargin;
	public static volatile SingularAttribute<CampaignTradingDeskMargin, Campaign> campaign;
	public static volatile SingularAttribute<CampaignTradingDeskMargin, Long> id;
	public static volatile SingularAttribute<CampaignTradingDeskMargin, Date> startDate;

}

