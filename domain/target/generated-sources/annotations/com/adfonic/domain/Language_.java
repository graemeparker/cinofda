package com.adfonic.domain;

import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(Language.class)
public abstract class Language_ {

	public static volatile SingularAttribute<Language, String> isoCode;
	public static volatile SingularAttribute<Language, String> name;
	public static volatile SingularAttribute<Language, Long> id;

}

