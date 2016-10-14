package com.adfonic.domain;

import com.adfonic.domain.AdSpace.ColorScheme;
import com.adfonic.domain.AdSpace.Status;
import java.util.Date;
import javax.persistence.metamodel.SetAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(AdSpace.class)
public abstract class AdSpace_ {

	public static volatile SingularAttribute<AdSpace, UnfilledAction> unfilledAction;
	public static volatile SetAttribute<AdSpace, Format> formats;
	public static volatile SingularAttribute<AdSpace, Date> creationTime;
	public static volatile SetAttribute<AdSpace, Feature> approvedFeatures;
	public static volatile SingularAttribute<AdSpace, String> externalID;
	public static volatile SetAttribute<AdSpace, Feature> deniedFeatures;
	public static volatile SingularAttribute<AdSpace, Boolean> backfillEnabled;
	public static volatile SingularAttribute<AdSpace, Date> reactivationTime;
	public static volatile SingularAttribute<AdSpace, Publication> publication;
	public static volatile SingularAttribute<AdSpace, String> name;
	public static volatile SingularAttribute<AdSpace, Boolean> useAdSignifier;
	public static volatile SingularAttribute<AdSpace, Long> id;
	public static volatile SingularAttribute<AdSpace, ColorScheme> colorScheme;
	public static volatile SingularAttribute<AdSpace, Status> status;

}

