package com.adfonic.domain;

import javax.persistence.metamodel.SetAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(IntegrationTypeMediaType.class)
public abstract class IntegrationTypeMediaType_ {

	public static volatile SingularAttribute<IntegrationTypeMediaType, IntegrationType> integrationType;
	public static volatile SetAttribute<IntegrationTypeMediaType, ContentForm> contentForms;
	public static volatile SingularAttribute<IntegrationTypeMediaType, MediaType> mediaType;
	public static volatile SingularAttribute<IntegrationTypeMediaType, Long> id;

}

