package com.adfonic.domain;

import javax.persistence.metamodel.ListAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(Destination.class)
public abstract class Destination_ {

	public static volatile SingularAttribute<Destination, Advertiser> advertiser;
	public static volatile ListAttribute<Destination, BeaconUrl> beaconUrls;
	public static volatile SingularAttribute<Destination, String> data;
	public static volatile SingularAttribute<Destination, String> finalDestination;
	public static volatile SingularAttribute<Destination, DestinationType> destinationType;
	public static volatile SingularAttribute<Destination, Long> id;
	public static volatile SingularAttribute<Destination, Boolean> dataIsFinalDestination;

}

