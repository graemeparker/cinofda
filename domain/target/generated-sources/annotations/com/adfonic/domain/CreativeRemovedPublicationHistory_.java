package com.adfonic.domain;

import com.adfonic.domain.RemovalInfo.RemovalType;
import java.util.Date;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(CreativeRemovedPublicationHistory.class)
public abstract class CreativeRemovedPublicationHistory_ {

	public static volatile SingularAttribute<CreativeRemovedPublicationHistory, RemovalType> removalType;
	public static volatile SingularAttribute<CreativeRemovedPublicationHistory, AdfonicUser> adfonicUser;
	public static volatile SingularAttribute<CreativeRemovedPublicationHistory, Publication> publication;
	public static volatile SingularAttribute<CreativeRemovedPublicationHistory, Long> id;
	public static volatile SingularAttribute<CreativeRemovedPublicationHistory, User> user;
	public static volatile SingularAttribute<CreativeRemovedPublicationHistory, Creative> creative;
	public static volatile SingularAttribute<CreativeRemovedPublicationHistory, Date> removalTime;

}

