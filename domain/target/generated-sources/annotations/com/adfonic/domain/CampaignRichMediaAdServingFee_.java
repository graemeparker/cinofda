package com.adfonic.domain;

import java.math.BigDecimal;
import java.util.Date;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(CampaignRichMediaAdServingFee.class)
public abstract class CampaignRichMediaAdServingFee_ {

	public static volatile SingularAttribute<CampaignRichMediaAdServingFee, Date> endDate;
	public static volatile SingularAttribute<CampaignRichMediaAdServingFee, BigDecimal> richMediaAdServingFee;
	public static volatile SingularAttribute<CampaignRichMediaAdServingFee, Campaign> campaign;
	public static volatile SingularAttribute<CampaignRichMediaAdServingFee, Long> id;
	public static volatile SingularAttribute<CampaignRichMediaAdServingFee, Date> startDate;

}

