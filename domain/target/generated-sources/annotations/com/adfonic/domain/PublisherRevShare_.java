package com.adfonic.domain;

import java.math.BigDecimal;
import java.util.Date;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(PublisherRevShare.class)
public abstract class PublisherRevShare_ {

	public static volatile SingularAttribute<PublisherRevShare, BigDecimal> revShare;
	public static volatile SingularAttribute<PublisherRevShare, Date> endDate;
	public static volatile SingularAttribute<PublisherRevShare, Publisher> publisher;
	public static volatile SingularAttribute<PublisherRevShare, Long> id;
	public static volatile SingularAttribute<PublisherRevShare, Date> startDate;

}

