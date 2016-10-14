package com.adfonic.domain;

import java.math.BigDecimal;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(CampaignOverallSpend.class)
public abstract class CampaignOverallSpend_ {

	public static volatile SingularAttribute<CampaignOverallSpend, BigDecimal> amount;
	public static volatile SingularAttribute<CampaignOverallSpend, Campaign> campaign;
	public static volatile SingularAttribute<CampaignOverallSpend, Long> id;
	public static volatile SingularAttribute<CampaignOverallSpend, BigDecimal> budget;

}

