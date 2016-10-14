package com.adfonic.domain;

import javax.persistence.metamodel.MapAttribute;
import javax.persistence.metamodel.SetAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(IntegrationType.class)
public abstract class IntegrationType_ {

	public static volatile SetAttribute<IntegrationType, BeaconMode> supportedBeaconModes;
	public static volatile SetAttribute<IntegrationType, Feature> supportedFeatures;
	public static volatile SingularAttribute<IntegrationType, String> systemName;
	public static volatile SingularAttribute<IntegrationType, Integer> versionRangeEnd;
	public static volatile SingularAttribute<IntegrationType, String> prefix;
	public static volatile SingularAttribute<IntegrationType, String> name;
	public static volatile SingularAttribute<IntegrationType, Long> id;
	public static volatile SingularAttribute<IntegrationType, Integer> versionRangeStart;
	public static volatile MapAttribute<IntegrationType, MediaType, IntegrationTypeMediaType> mediaTypeMap;

}

