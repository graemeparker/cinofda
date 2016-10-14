package com.adfonic.domain;

import java.math.BigDecimal;
import javax.persistence.metamodel.ListAttribute;
import javax.persistence.metamodel.MapAttribute;
import javax.persistence.metamodel.SetAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(Publisher.class)
public abstract class Publisher_ {

	public static volatile ListAttribute<Publisher, PublisherRevShare> revShareHistory;
	public static volatile SingularAttribute<Publisher, Boolean> requiresRealDestination;
	public static volatile SetAttribute<Publisher, Creative> approvedCreatives;
	public static volatile SingularAttribute<Publisher, String> externalID;
	public static volatile SingularAttribute<Publisher, AdfonicUser> salesOwner;
	public static volatile SingularAttribute<Publisher, RtbConfig> rtbConfig;
	public static volatile SingularAttribute<Publisher, BigDecimal> defaultRevShare;
	public static volatile MapAttribute<Publisher, PublicationType, IntegrationType> defaultIntegrationTypeMap;
	public static volatile SingularAttribute<Publisher, PendingAdType> pendingAdType;
	public static volatile SetAttribute<Publisher, ExtendedCreativeType> thirdPartyTagVendorWhitelist;
	public static volatile SingularAttribute<Publisher, Company> company;
	public static volatile SingularAttribute<Publisher, Long> id;
	public static volatile SingularAttribute<Publisher, Boolean> key;
	public static volatile SingularAttribute<Publisher, PublisherRevShare> currentRevShare;
	public static volatile SetAttribute<Publisher, BidType> blockedBidTypes;
	public static volatile SingularAttribute<Publisher, Boolean> disclosed;
	public static volatile SingularAttribute<Publisher, Double> buyerPremium;
	public static volatile SingularAttribute<Publisher, Long> defaultAdRequestTimeout;
	public static volatile SingularAttribute<Publisher, AdfonicUser> adOpsOwner;
	public static volatile SingularAttribute<Publisher, String> name;
	public static volatile MapAttribute<Publisher, BidType, RateCard> defaultRateCardMap;
	public static volatile SingularAttribute<Publisher, RateCard> ecpmTargetRateCard;
	public static volatile SingularAttribute<Publisher, Account> account;
	public static volatile SetAttribute<Publisher, Category> excludedCategories;
	public static volatile SetAttribute<Publisher, Publication> publications;

}

