package com.adfonic.domain;

import com.adfonic.domain.Audience.Status;
import java.util.Date;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(Audience.class)
public abstract class Audience_ {

	public static volatile SingularAttribute<Audience, Advertiser> advertiser;
	public static volatile SingularAttribute<Audience, FirstPartyAudience> firstPartyAudience;
	public static volatile SingularAttribute<Audience, Date> creationTime;
	public static volatile SingularAttribute<Audience, DMPAudience> dmpAudience;
	public static volatile SingularAttribute<Audience, String> name;
	public static volatile SingularAttribute<Audience, String> externalID;
	public static volatile SingularAttribute<Audience, Long> id;
	public static volatile SingularAttribute<Audience, Status> status;

}

