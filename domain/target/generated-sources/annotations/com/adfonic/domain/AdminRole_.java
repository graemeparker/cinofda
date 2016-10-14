package com.adfonic.domain;

import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(AdminRole.class)
public abstract class AdminRole_ {

	public static volatile SingularAttribute<AdminRole, String> name;
	public static volatile SingularAttribute<AdminRole, Long> id;

}

