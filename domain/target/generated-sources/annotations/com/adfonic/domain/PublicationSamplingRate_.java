package com.adfonic.domain;

import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(PublicationSamplingRate.class)
public abstract class PublicationSamplingRate_ {

	public static volatile SingularAttribute<PublicationSamplingRate, Integer> samplingRate;
	public static volatile SingularAttribute<PublicationSamplingRate, Long> id;
	public static volatile SingularAttribute<PublicationSamplingRate, Long> publicationId;

}

