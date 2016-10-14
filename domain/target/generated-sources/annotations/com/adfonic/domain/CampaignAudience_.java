package com.adfonic.domain;

import java.util.Date;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(CampaignAudience.class)
public abstract class CampaignAudience_ {

	public static volatile SingularAttribute<CampaignAudience, Integer> recencyDaysTo;
	public static volatile SingularAttribute<CampaignAudience, Boolean> include;
	public static volatile SingularAttribute<CampaignAudience, Audience> audience;
	public static volatile SingularAttribute<CampaignAudience, Boolean> deleted;
	public static volatile SingularAttribute<CampaignAudience, Date> recencyDateTo;
	public static volatile SingularAttribute<CampaignAudience, AudienceDataFee> audienceDataFee;
	public static volatile SingularAttribute<CampaignAudience, Integer> recencyDaysFrom;
	public static volatile SingularAttribute<CampaignAudience, Campaign> campaign;
	public static volatile SingularAttribute<CampaignAudience, Long> id;
	public static volatile SingularAttribute<CampaignAudience, Date> recencyDateFrom;

}

