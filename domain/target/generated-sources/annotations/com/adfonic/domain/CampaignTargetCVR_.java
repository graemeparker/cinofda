package com.adfonic.domain;

import java.math.BigDecimal;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(CampaignTargetCVR.class)
public abstract class CampaignTargetCVR_ {

	public static volatile SingularAttribute<CampaignTargetCVR, Campaign> campaign;
	public static volatile SingularAttribute<CampaignTargetCVR, Long> id;
	public static volatile SingularAttribute<CampaignTargetCVR, BigDecimal> targetCVR;

}

