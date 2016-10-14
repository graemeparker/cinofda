package com.adfonic.domain;

import javax.persistence.metamodel.SetAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(Model.class)
public abstract class Model_ {

	public static volatile SingularAttribute<Model, Boolean> deleted;
	public static volatile SingularAttribute<Model, Boolean> hidden;
	public static volatile SingularAttribute<Model, Vendor> vendor;
	public static volatile SingularAttribute<Model, String> name;
	public static volatile SingularAttribute<Model, String> externalID;
	public static volatile SingularAttribute<Model, Long> id;
	public static volatile SetAttribute<Model, Platform> platforms;
	public static volatile SingularAttribute<Model, DeviceGroup> deviceGroup;

}

