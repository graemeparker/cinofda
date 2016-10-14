package com.adfonic.domain;

import com.adfonic.domain.Publication.AdOpsStatus;
import com.adfonic.domain.Publication.PublicationAttributeKey;
import com.adfonic.domain.Publication.PublicationSafetyLevel;
import com.adfonic.domain.Publication.Status;
import java.math.BigDecimal;
import java.util.Date;
import javax.persistence.metamodel.ListAttribute;
import javax.persistence.metamodel.MapAttribute;
import javax.persistence.metamodel.SetAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(Publication.class)
public abstract class Publication_ {

	public static volatile SingularAttribute<Publication, Date> creationTime;
	public static volatile MapAttribute<Publication, PublicationAttributeKey, String> publicationAttributes;
	public static volatile SingularAttribute<Publication, PublicationSafetyLevel> safetyLevel;
	public static volatile SetAttribute<Publication, Creative> approvedCreatives;
	public static volatile SingularAttribute<Publication, Boolean> installTrackingDisabled;
	public static volatile SingularAttribute<Publication, String> urlString;
	public static volatile SingularAttribute<Publication, String> description;
	public static volatile SingularAttribute<Publication, String> externalID;
	public static volatile SetAttribute<Publication, AdfonicUser> watchers;
	public static volatile ListAttribute<Publication, AdSpace> adSpaces;
	public static volatile SingularAttribute<Publication, Date> submissionTime;
	public static volatile SingularAttribute<Publication, AdfonicUser> assignedTo;
	public static volatile SingularAttribute<Publication, TrackingIdentifierType> trackingIdentifierType;
	public static volatile SetAttribute<Publication, Category> statedCategories;
	public static volatile SingularAttribute<Publication, String> reference;
	public static volatile SingularAttribute<Publication, Boolean> summaryDisplayDisabled;
	public static volatile SingularAttribute<Publication, TransparentNetwork> transparentNetwork;
	public static volatile SetAttribute<Publication, ExtendedCreativeType> thirdPartyTagVendorWhitelist;
	public static volatile SingularAttribute<Publication, Boolean> autoApproval;
	public static volatile SingularAttribute<Publication, Long> id;
	public static volatile SingularAttribute<Publication, Long> adRequestTimeout;
	public static volatile SingularAttribute<Publication, String> friendlyName;
	public static volatile SetAttribute<Publication, BidType> blockedBidTypes;
	public static volatile SetAttribute<Publication, Creative> deniedCreatives;
	public static volatile SingularAttribute<Publication, Boolean> disclosed;
	public static volatile SetAttribute<Publication, Language> languages;
	public static volatile SingularAttribute<Publication, String> rtbId;
	public static volatile SingularAttribute<Publication, PublicationType> publicationType;
	public static volatile ListAttribute<Publication, PublicationHistory> history;
	public static volatile MapAttribute<Publication, BidType, RateCard> rateCardMap;
	public static volatile SingularAttribute<Publication, BigDecimal> genderMix;
	public static volatile SingularAttribute<Publication, Boolean> backfillEnabled;
	public static volatile SingularAttribute<Publication, Date> approvedDate;
	public static volatile SetAttribute<Publication, PublicationProvidedInfo> publicationProvidedInfos;
	public static volatile SingularAttribute<Publication, IntegrationType> defaultIntegrationType;
	public static volatile SingularAttribute<Publication, Boolean> incentivized;
	public static volatile SingularAttribute<Publication, Boolean> matchUserLanguage;
	public static volatile SingularAttribute<Publication, Integer> maxAge;
	public static volatile SingularAttribute<Publication, Integer> minAge;
	public static volatile SingularAttribute<Publication, String> name;
	public static volatile SingularAttribute<Publication, AdOpsStatus> adOpsStatus;
	public static volatile SingularAttribute<Publication, Integer> samplingRate;
	public static volatile SingularAttribute<Publication, Publisher> publisher;
	public static volatile SingularAttribute<Publication, Category> category;
	public static volatile SingularAttribute<Publication, RateCard> ecpmTargetRateCard;
	public static volatile SetAttribute<Publication, Category> excludedCategories;
	public static volatile SingularAttribute<Publication, Status> status;

}

