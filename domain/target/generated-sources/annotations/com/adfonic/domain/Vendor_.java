package com.adfonic.domain;

import javax.persistence.metamodel.SetAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(Vendor.class)
public abstract class Vendor_ {

	public static volatile SetAttribute<Vendor, Model> models;
	public static volatile SetAttribute<Vendor, String> aliases;
	public static volatile SingularAttribute<Vendor, String> name;
	public static volatile SingularAttribute<Vendor, Boolean> reviewed;
	public static volatile SingularAttribute<Vendor, Long> id;

}

