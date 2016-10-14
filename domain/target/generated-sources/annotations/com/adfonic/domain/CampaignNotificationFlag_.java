package com.adfonic.domain;

import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(CampaignNotificationFlag.class)
public abstract class CampaignNotificationFlag_ extends com.adfonic.domain.NotificationFlag_ {

	public static volatile SingularAttribute<CampaignNotificationFlag, Advertiser> advertiser;
	public static volatile SingularAttribute<CampaignNotificationFlag, Campaign> campaign;

}

