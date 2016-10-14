package com.adfonic.domain;

import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(DeviceIdentifierType.class)
public abstract class DeviceIdentifierType_ {

	public static volatile SingularAttribute<DeviceIdentifierType, String> systemName;
	public static volatile SingularAttribute<DeviceIdentifierType, Boolean> hidden;
	public static volatile SingularAttribute<DeviceIdentifierType, Integer> precedenceOrder;
	public static volatile SingularAttribute<DeviceIdentifierType, String> name;
	public static volatile SingularAttribute<DeviceIdentifierType, Long> id;
	public static volatile SingularAttribute<DeviceIdentifierType, Boolean> secure;
	public static volatile SingularAttribute<DeviceIdentifierType, String> validationRegex;
	public static volatile SingularAttribute<DeviceIdentifierType, String> trusteIdType;

}

