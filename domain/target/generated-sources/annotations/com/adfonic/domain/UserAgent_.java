package com.adfonic.domain;

import java.util.Date;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(UserAgent.class)
public abstract class UserAgent_ {

	public static volatile SingularAttribute<UserAgent, String> userAgentHeader;
	public static volatile SingularAttribute<UserAgent, Model> model;
	public static volatile SingularAttribute<UserAgent, Long> id;
	public static volatile SingularAttribute<UserAgent, Date> dateLastSeen;

}

