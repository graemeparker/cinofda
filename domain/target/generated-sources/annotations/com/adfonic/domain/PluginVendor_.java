package com.adfonic.domain;

import javax.persistence.metamodel.SetAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(PluginVendor.class)
public abstract class PluginVendor_ {

	public static volatile SingularAttribute<PluginVendor, String> name;
	public static volatile SingularAttribute<PluginVendor, Long> id;
	public static volatile SingularAttribute<PluginVendor, String> apiPassword;
	public static volatile SetAttribute<PluginVendor, CampaignTrigger> campaignTriggers;
	public static volatile SingularAttribute<PluginVendor, String> apiUser;

}

