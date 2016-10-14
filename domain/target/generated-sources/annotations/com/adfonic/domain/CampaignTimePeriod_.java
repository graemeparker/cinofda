package com.adfonic.domain;

import java.util.Date;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(CampaignTimePeriod.class)
public abstract class CampaignTimePeriod_ {

	public static volatile SingularAttribute<CampaignTimePeriod, Date> endDate;
	public static volatile SingularAttribute<CampaignTimePeriod, Campaign> campaign;
	public static volatile SingularAttribute<CampaignTimePeriod, Long> id;
	public static volatile SingularAttribute<CampaignTimePeriod, Date> startDate;

}

