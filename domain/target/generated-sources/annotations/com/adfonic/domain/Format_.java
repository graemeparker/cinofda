package com.adfonic.domain;

import javax.persistence.metamodel.ListAttribute;
import javax.persistence.metamodel.SetAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(Format.class)
public abstract class Format_ {

	public static volatile ListAttribute<Format, Component> components;
	public static volatile SingularAttribute<Format, String> systemName;
	public static volatile SingularAttribute<Format, String> name;
	public static volatile ListAttribute<Format, DisplayType> displayTypes;
	public static volatile SingularAttribute<Format, Long> id;
	public static volatile SetAttribute<Format, DeviceGroup> deviceGroups;

}

