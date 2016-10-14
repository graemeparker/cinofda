package com.adfonic.domain;

import java.util.Date;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(AudienceDataFee.class)
public abstract class AudienceDataFee_ {

	public static volatile SingularAttribute<AudienceDataFee, CampaignAudience> campaignAudience;
	public static volatile SingularAttribute<AudienceDataFee, Boolean> isMaximumForVendor;
	public static volatile SingularAttribute<AudienceDataFee, CampaignDataFee> campaignDataFee;
	public static volatile SingularAttribute<AudienceDataFee, Date> startTime;
	public static volatile SingularAttribute<AudienceDataFee, Long> id;
	public static volatile SingularAttribute<AudienceDataFee, Date> endTime;
	public static volatile SingularAttribute<AudienceDataFee, AudiencePrices> audiencePrices;

}

