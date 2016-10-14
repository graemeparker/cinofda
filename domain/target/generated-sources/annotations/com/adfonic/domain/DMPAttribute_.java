package com.adfonic.domain;

import javax.persistence.metamodel.SetAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(DMPAttribute.class)
public abstract class DMPAttribute_ {

	public static volatile SingularAttribute<DMPAttribute, DMPVendor> dmpVendor;
	public static volatile SingularAttribute<DMPAttribute, String> name;
	public static volatile SingularAttribute<DMPAttribute, Integer> displayOrder;
	public static volatile SingularAttribute<DMPAttribute, Long> id;
	public static volatile SetAttribute<DMPAttribute, DMPSelector> dmpSelectors;

}

