package com.adfonic.domain;

import com.adfonic.domain.PrivateMarketPlaceDeal.AuctionType;
import java.math.BigDecimal;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(PrivateMarketPlaceDeal.class)
public abstract class PrivateMarketPlaceDeal_ {

	public static volatile SingularAttribute<PrivateMarketPlaceDeal, BigDecimal> amount;
	public static volatile SingularAttribute<PrivateMarketPlaceDeal, String> dealId;
	public static volatile SingularAttribute<PrivateMarketPlaceDeal, Publisher> publisher;
	public static volatile SingularAttribute<PrivateMarketPlaceDeal, Long> id;
	public static volatile SingularAttribute<PrivateMarketPlaceDeal, AuctionType> auctionType;

}

