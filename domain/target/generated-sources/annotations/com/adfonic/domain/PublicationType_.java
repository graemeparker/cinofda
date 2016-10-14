package com.adfonic.domain;

import javax.persistence.metamodel.SetAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(PublicationType.class)
public abstract class PublicationType_ {

	public static volatile SingularAttribute<PublicationType, IntegrationType> defaultIntegrationType;
	public static volatile SingularAttribute<PublicationType, String> systemName;
	public static volatile SingularAttribute<PublicationType, String> name;
	public static volatile SingularAttribute<PublicationType, Long> id;
	public static volatile SingularAttribute<PublicationType, Medium> medium;
	public static volatile SingularAttribute<PublicationType, TrackingIdentifierType> defaultTrackingIdentifierType;
	public static volatile SetAttribute<PublicationType, Platform> platforms;

}

