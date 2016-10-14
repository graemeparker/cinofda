package com.adfonic.domain;

import java.math.BigDecimal;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(CampaignTargetCTR.class)
public abstract class CampaignTargetCTR_ {

	public static volatile SingularAttribute<CampaignTargetCTR, BigDecimal> targetCTR;
	public static volatile SingularAttribute<CampaignTargetCTR, Campaign> campaign;
	public static volatile SingularAttribute<CampaignTargetCTR, Long> id;

}

