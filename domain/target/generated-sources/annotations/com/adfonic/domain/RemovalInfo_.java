package com.adfonic.domain;

import com.adfonic.domain.RemovalInfo.RemovalType;
import java.util.Date;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(RemovalInfo.class)
public abstract class RemovalInfo_ {

	public static volatile SingularAttribute<RemovalInfo, RemovalType> removalType;
	public static volatile SingularAttribute<RemovalInfo, Long> id;
	public static volatile SingularAttribute<RemovalInfo, Date> removalTime;

}

