package com.adfonic.domain;

import javax.persistence.metamodel.SetAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(DMPAudience.class)
public abstract class DMPAudience_ {

	public static volatile SingularAttribute<DMPAudience, Audience> audience;
	public static volatile SingularAttribute<DMPAudience, String> userEnteredDMPSelectorExternalId;
	public static volatile SingularAttribute<DMPAudience, DMPVendor> dmpVendor;
	public static volatile SingularAttribute<DMPAudience, Long> id;
	public static volatile SetAttribute<DMPAudience, DMPSelector> dmpSelectors;

}

