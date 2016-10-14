package com.adfonic.domain;

import java.math.BigDecimal;
import java.util.Date;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(BidDeduction.class)
public abstract class BidDeduction_ {

	public static volatile SingularAttribute<BidDeduction, BigDecimal> amount;
	public static volatile SingularAttribute<BidDeduction, ThirdPartyVendor> thirdPartyVendor;
	public static volatile SingularAttribute<BidDeduction, Date> endDate;
	public static volatile SingularAttribute<BidDeduction, Campaign> campaign;
	public static volatile SingularAttribute<BidDeduction, String> thirdPartyVendorFreeText;
	public static volatile SingularAttribute<BidDeduction, Long> id;
	public static volatile SingularAttribute<BidDeduction, Boolean> payerIsByyd;
	public static volatile SingularAttribute<BidDeduction, Date> startDate;

}

