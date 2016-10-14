package com.adfonic.domain;

import com.adfonic.domain.CampaignStoppage.Reason;
import java.util.Date;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(CampaignStoppage.class)
public abstract class CampaignStoppage_ {

	public static volatile SingularAttribute<CampaignStoppage, Reason> reason;
	public static volatile SingularAttribute<CampaignStoppage, Date> reactivateDate;
	public static volatile SingularAttribute<CampaignStoppage, Campaign> campaign;
	public static volatile SingularAttribute<CampaignStoppage, Long> id;
	public static volatile SingularAttribute<CampaignStoppage, Date> timestamp;

}

