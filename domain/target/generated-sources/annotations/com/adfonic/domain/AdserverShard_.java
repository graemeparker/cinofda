package com.adfonic.domain;

import com.adfonic.domain.AdserverShard.Mode;
import java.util.Date;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(AdserverShard.class)
public abstract class AdserverShard_ {

	public static volatile SingularAttribute<AdserverShard, Mode> mode;
	public static volatile SingularAttribute<AdserverShard, Date> lastUpdated;
	public static volatile SingularAttribute<AdserverShard, Boolean> rtb;
	public static volatile SingularAttribute<AdserverShard, String> name;
	public static volatile SingularAttribute<AdserverShard, Long> id;

}

