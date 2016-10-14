package com.adfonic.domain;

import com.adfonic.domain.AdserverStatus.Status;
import java.util.Date;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(AdserverStatus.class)
public abstract class AdserverStatus_ {

	public static volatile SingularAttribute<AdserverStatus, Date> lastUpdated;
	public static volatile SingularAttribute<AdserverStatus, String> name;
	public static volatile SingularAttribute<AdserverStatus, String> description;
	public static volatile SingularAttribute<AdserverStatus, Long> id;
	public static volatile SingularAttribute<AdserverStatus, AdserverShard> shard;
	public static volatile SingularAttribute<AdserverStatus, Status> status;

}

