package com.adfonic.domain;

import com.adfonic.domain.CampaignBid.BidModelType;
import java.math.BigDecimal;
import java.util.Date;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(CampaignBid.class)
public abstract class CampaignBid_ {

	public static volatile SingularAttribute<CampaignBid, BigDecimal> amount;
	public static volatile SingularAttribute<CampaignBid, Date> endDate;
	public static volatile SingularAttribute<CampaignBid, Campaign> campaign;
	public static volatile SingularAttribute<CampaignBid, Boolean> maximum;
	public static volatile SingularAttribute<CampaignBid, Long> id;
	public static volatile SingularAttribute<CampaignBid, Date> startDate;
	public static volatile SingularAttribute<CampaignBid, BidType> bidType;
	public static volatile SingularAttribute<CampaignBid, BidModelType> bidModelType;

}

