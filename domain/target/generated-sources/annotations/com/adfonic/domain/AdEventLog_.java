package com.adfonic.domain;

import java.math.BigDecimal;
import java.util.Date;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(AdEventLog.class)
public abstract class AdEventLog_ {

	public static volatile SingularAttribute<AdEventLog, Country> country;
	public static volatile SingularAttribute<AdEventLog, String> trackingIdentifier;
	public static volatile SingularAttribute<AdEventLog, Integer> advertiserTime;
	public static volatile SingularAttribute<AdEventLog, AdSpace> adSpace;
	public static volatile SingularAttribute<AdEventLog, Gender> gender;
	public static volatile SingularAttribute<AdEventLog, Double> latitude;
	public static volatile SingularAttribute<AdEventLog, BigDecimal> payout;
	public static volatile SingularAttribute<AdEventLog, Operator> operator;
	public static volatile SingularAttribute<AdEventLog, Geotarget> geotarget;
	public static volatile SingularAttribute<AdEventLog, IntegrationType> integrationType;
	public static volatile SingularAttribute<AdEventLog, Publication> publication;
	public static volatile SingularAttribute<AdEventLog, Date> eventTime;
	public static volatile SingularAttribute<AdEventLog, Model> model;
	public static volatile SingularAttribute<AdEventLog, Long> id;
	public static volatile SingularAttribute<AdEventLog, Double> longitude;
	public static volatile SingularAttribute<AdEventLog, BigDecimal> advertiserVAT;
	public static volatile SingularAttribute<AdEventLog, BigDecimal> cost;
	public static volatile SingularAttribute<AdEventLog, String> ipAddress;
	public static volatile SingularAttribute<AdEventLog, UserAgent> userAgent;
	public static volatile SingularAttribute<AdEventLog, Boolean> backfilled;
	public static volatile SingularAttribute<AdEventLog, AdAction> adAction;
	public static volatile SingularAttribute<AdEventLog, Creative> creative;
	public static volatile SingularAttribute<AdEventLog, Integer> ageHigh;
	public static volatile SingularAttribute<AdEventLog, BigDecimal> publisherVAT;
	public static volatile SingularAttribute<AdEventLog, Integer> ageLow;
	public static volatile SingularAttribute<AdEventLog, Campaign> campaign;
	public static volatile SingularAttribute<AdEventLog, Integer> publisherTime;

}

