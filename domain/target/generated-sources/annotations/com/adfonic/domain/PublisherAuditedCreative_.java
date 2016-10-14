package com.adfonic.domain;

import com.adfonic.domain.PublisherAuditedCreative.Status;
import java.util.Date;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(PublisherAuditedCreative.class)
public abstract class PublisherAuditedCreative_ {

	public static volatile SingularAttribute<PublisherAuditedCreative, Date> latestFetchTime;
	public static volatile SingularAttribute<PublisherAuditedCreative, Long> messageCount;
	public static volatile SingularAttribute<PublisherAuditedCreative, Long> clickCount;
	public static volatile SingularAttribute<PublisherAuditedCreative, Date> creationTime;
	public static volatile SingularAttribute<PublisherAuditedCreative, Date> latestClickTime;
	public static volatile SingularAttribute<PublisherAuditedCreative, Creative> creative;
	public static volatile SingularAttribute<PublisherAuditedCreative, String> externalReference;
	public static volatile SingularAttribute<PublisherAuditedCreative, String> lastAuditRemarks;
	public static volatile SingularAttribute<PublisherAuditedCreative, Date> latestImpressionTime;
	public static volatile SingularAttribute<PublisherAuditedCreative, Publisher> publisher;
	public static volatile SingularAttribute<PublisherAuditedCreative, Long> id;
	public static volatile SingularAttribute<PublisherAuditedCreative, Long> impressionCount;
	public static volatile SingularAttribute<PublisherAuditedCreative, Status> status;

}

