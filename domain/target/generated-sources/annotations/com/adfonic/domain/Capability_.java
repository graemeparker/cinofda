package com.adfonic.domain;

import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(Capability.class)
public abstract class Capability_ {

	public static volatile SingularAttribute<Capability, String> name;
	public static volatile SingularAttribute<Capability, String> description;
	public static volatile SingularAttribute<Capability, Long> id;
	public static volatile SingularAttribute<Capability, String> constraints;

}

