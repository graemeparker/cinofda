package com.adfonic.domain;

import javax.persistence.metamodel.SetAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(PublicationBundle.class)
public abstract class PublicationBundle_ {

	public static volatile SingularAttribute<PublicationBundle, String> name;
	public static volatile SingularAttribute<PublicationBundle, Long> id;
	public static volatile SetAttribute<PublicationBundle, Publication> publications;

}

