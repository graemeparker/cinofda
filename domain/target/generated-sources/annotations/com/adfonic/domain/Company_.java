package com.adfonic.domain;

import com.adfonic.domain.Company.AdvertiserCategory;
import com.adfonic.domain.Company.PublisherCategory;
import java.math.BigDecimal;
import java.util.Date;
import javax.persistence.metamodel.SetAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(Company.class)
public abstract class Company_ {

	public static volatile SingularAttribute<Company, PublicationList> publicationBlackList;
	public static volatile SingularAttribute<Company, Country> country;
	public static volatile SetAttribute<Company, BidSeat> companyRtbBidSeats;
	public static volatile SingularAttribute<Company, Boolean> isInvoiceDateInGMT;
	public static volatile SingularAttribute<Company, BigDecimal> autoTopupAmount;
	public static volatile SingularAttribute<Company, Boolean> taxableAdvertiser;
	public static volatile SingularAttribute<Company, Date> creationTime;
	public static volatile SingularAttribute<Company, Date> postPayActivationDate;
	public static volatile SetAttribute<Company, IpAddressRange> ipAddressRanges;
	public static volatile SetAttribute<Company, AccountFixedMargin> historicalAccountFixedMargins;
	public static volatile SetAttribute<Company, Advertiser> advertisers;
	public static volatile SetAttribute<Company, Role> roles;
	public static volatile SingularAttribute<Company, String> externalID;
	public static volatile SingularAttribute<Company, BigDecimal> discount;
	public static volatile SingularAttribute<Company, MarginShareDSP> currentMarginShareDSP;
	public static volatile SingularAttribute<Company, AdvertiserMediaCostMargin> currentMediaCostMargin;
	public static volatile SingularAttribute<Company, User> accountManager;
	public static volatile SingularAttribute<Company, PublisherCategory> publisherCategory;
	public static volatile SingularAttribute<Company, PaymentOptions> paymentOptions;
	public static volatile SetAttribute<Company, MarginShareDSP> historicalMarginShareDSPs;
	public static volatile SingularAttribute<Company, BigDecimal> creditLimit;
	public static volatile SingularAttribute<Company, Boolean> backfill;
	public static volatile SingularAttribute<Company, UploadedContent> logo;
	public static volatile SingularAttribute<Company, Long> id;
	public static volatile SingularAttribute<Company, Boolean> enableRtbBidSeat;
	public static volatile SingularAttribute<Company, Integer> postPayTermDays;
	public static volatile SingularAttribute<Company, Boolean> individual;
	public static volatile SingularAttribute<Company, CompanyDirectCost> companyDirectCost;
	public static volatile SingularAttribute<Company, String> taxCode;
	public static volatile SingularAttribute<Company, AccountFixedMargin> currentAccountFixedMargin;
	public static volatile SingularAttribute<Company, String> autoTopupAuthTransactionId;
	public static volatile SetAttribute<Company, DMPVendor> restrictedDMPVendors;
	public static volatile SingularAttribute<Company, BigDecimal> autoTopupLimit;
	public static volatile SetAttribute<Company, User> users;
	public static volatile SingularAttribute<Company, Boolean> taxablePublisher;
	public static volatile SingularAttribute<Company, PublicationList> publicationWhiteList;
	public static volatile SingularAttribute<Company, String> defaultTimeZone;
	public static volatile SingularAttribute<Company, Integer> accountTypeFlags;
	public static volatile SingularAttribute<Company, AdvertiserCategory> advertiserCategory;
	public static volatile SetAttribute<Company, AdvertiserMediaCostMargin> historicalMediaCostMargins;
	public static volatile SetAttribute<Company, NotificationFlag> notificationFlags;
	public static volatile SingularAttribute<Company, AffiliateProgram> affiliateProgram;
	public static volatile SingularAttribute<Company, String> name;
	public static volatile SingularAttribute<Company, Publisher> publisher;

}

