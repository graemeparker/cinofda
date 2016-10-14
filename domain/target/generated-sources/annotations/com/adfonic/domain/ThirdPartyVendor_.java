package com.adfonic.domain;

import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(ThirdPartyVendor.class)
public abstract class ThirdPartyVendor_ {

	public static volatile SingularAttribute<ThirdPartyVendor, ThirdPartyVendorType> thirdPartyVendorType;
	public static volatile SingularAttribute<ThirdPartyVendor, String> name;
	public static volatile SingularAttribute<ThirdPartyVendor, Long> id;

}

