package com.adfonic.domain;

import com.adfonic.domain.CampaignTrigger.PluginType;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(CampaignTrigger.class)
public abstract class CampaignTrigger_ {

	public static volatile SingularAttribute<CampaignTrigger, PluginVendor> pluginVendor;
	public static volatile SingularAttribute<CampaignTrigger, PluginType> pluginType;
	public static volatile SingularAttribute<CampaignTrigger, Boolean> deleted;
	public static volatile SingularAttribute<CampaignTrigger, Campaign> campaign;
	public static volatile SingularAttribute<CampaignTrigger, Long> id;

}

