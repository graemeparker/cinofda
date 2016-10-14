package com.adfonic.domain;

import com.adfonic.domain.Advertiser.Status;
import java.math.BigDecimal;
import java.util.Date;
import javax.persistence.metamodel.MapAttribute;
import javax.persistence.metamodel.SetAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(Advertiser.class)
public abstract class Advertiser_ {

	public static volatile SingularAttribute<Advertiser, Date> creationTime;
	public static volatile SingularAttribute<Advertiser, String> externalID;
	public static volatile SingularAttribute<Advertiser, Boolean> managedDelivery;
	public static volatile SingularAttribute<Advertiser, AdfonicUser> salesOwner;
	public static volatile SetAttribute<Advertiser, Segment> segments;
	public static volatile SetAttribute<Advertiser, Campaign> campaigns;
	public static volatile SingularAttribute<Advertiser, Company> company;
	public static volatile SingularAttribute<Advertiser, Long> id;
	public static volatile SingularAttribute<Advertiser, Boolean> enableRtbBidSeat;
	public static volatile SingularAttribute<Advertiser, Boolean> key;
	public static volatile SingularAttribute<Advertiser, BigDecimal> notifyLimit;
	public static volatile SingularAttribute<Advertiser, Boolean> managedTrafficking;
	public static volatile MapAttribute<Advertiser, Integer, BudgetSpend> dailySpendMap;
	public static volatile SetAttribute<Advertiser, Destination> destinations;
	public static volatile SetAttribute<Advertiser, Advertiser> crossTargetAdvertisers;
	public static volatile SingularAttribute<Advertiser, Boolean> conversionProtected;
	public static volatile SetAttribute<Advertiser, BidSeat> advertiserRtbBidSeats;
	public static volatile SetAttribute<Advertiser, User> users;
	public static volatile SingularAttribute<Advertiser, AdfonicUser> adOpsOwner;
	public static volatile SingularAttribute<Advertiser, String> notifyAdditionalEmails;
	public static volatile SingularAttribute<Advertiser, BigDecimal> dailyBudget;
	public static volatile SetAttribute<Advertiser, AdvertiserNotificationFlag> notificationFlags;
	public static volatile SingularAttribute<Advertiser, BidSeat> pmpBidSeat;
	public static volatile SingularAttribute<Advertiser, String> name;
	public static volatile SingularAttribute<Advertiser, Account> account;
	public static volatile SingularAttribute<Advertiser, Status> status;

}

