package com.adfonic.domain;

import com.adfonic.domain.BidSeat.BidSeatType;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(BidSeat.class)
public abstract class BidSeat_ {

	public static volatile SingularAttribute<BidSeat, String> description;
	public static volatile SingularAttribute<BidSeat, String> seatId;
	public static volatile SingularAttribute<BidSeat, TargetPublisher> targetPublisher;
	public static volatile SingularAttribute<BidSeat, Long> id;
	public static volatile SingularAttribute<BidSeat, BidSeatType> type;

}

