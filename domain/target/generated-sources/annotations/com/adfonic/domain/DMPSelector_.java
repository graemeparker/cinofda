package com.adfonic.domain;

import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(DMPSelector.class)
public abstract class DMPSelector_ {

	public static volatile SingularAttribute<DMPSelector, Boolean> hidden;
	public static volatile SingularAttribute<DMPSelector, String> name;
	public static volatile SingularAttribute<DMPSelector, Long> muidSegmentId;
	public static volatile SingularAttribute<DMPSelector, Integer> displayOrder;
	public static volatile SingularAttribute<DMPSelector, String> externalID;
	public static volatile SingularAttribute<DMPSelector, Publisher> publisher;
	public static volatile SingularAttribute<DMPSelector, Long> dmpVendorId;
	public static volatile SingularAttribute<DMPSelector, DMPAttribute> dmpAttribute;
	public static volatile SingularAttribute<DMPSelector, Long> id;
	public static volatile SingularAttribute<DMPSelector, AudiencePrices> audiencePrices;

}

