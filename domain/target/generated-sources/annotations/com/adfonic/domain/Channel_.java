package com.adfonic.domain;

import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(Channel.class)
public abstract class Channel_ {

	public static volatile SingularAttribute<Channel, String> name;
	public static volatile SingularAttribute<Channel, Long> id;

}

