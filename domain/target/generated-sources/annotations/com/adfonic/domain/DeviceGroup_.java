package com.adfonic.domain;

import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(DeviceGroup.class)
public abstract class DeviceGroup_ {

	public static volatile SingularAttribute<DeviceGroup, String> systemName;
	public static volatile SingularAttribute<DeviceGroup, Boolean> hidden;
	public static volatile SingularAttribute<DeviceGroup, Long> id;
	public static volatile SingularAttribute<DeviceGroup, String> constraints;

}

