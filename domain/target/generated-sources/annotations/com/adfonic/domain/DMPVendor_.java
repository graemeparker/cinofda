package com.adfonic.domain;

import javax.persistence.metamodel.SetAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(DMPVendor.class)
public abstract class DMPVendor_ {

	public static volatile SetAttribute<DMPVendor, DMPAttribute> dmpAttributes;
	public static volatile SingularAttribute<DMPVendor, Boolean> restricted;
	public static volatile SingularAttribute<DMPVendor, String> name;
	public static volatile SetAttribute<DMPVendor, Publisher> publishers;
	public static volatile SingularAttribute<DMPVendor, AudiencePrices> defaultAudiencePrices;
	public static volatile SingularAttribute<DMPVendor, Long> id;

}

