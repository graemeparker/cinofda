package com.adfonic.domain;

import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(BeaconUrl.class)
public abstract class BeaconUrl_ {

	public static volatile SingularAttribute<BeaconUrl, Destination> destination;
	public static volatile SingularAttribute<BeaconUrl, Long> id;
	public static volatile SingularAttribute<BeaconUrl, String> url;

}

