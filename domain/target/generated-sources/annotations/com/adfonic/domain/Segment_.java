package com.adfonic.domain;

import com.adfonic.domain.Segment.DayOfWeek;
import com.adfonic.domain.Segment.SegmentSafetyLevel;
import java.math.BigDecimal;
import javax.persistence.metamodel.MapAttribute;
import javax.persistence.metamodel.SetAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(Segment.class)
public abstract class Segment_ {

	public static volatile SingularAttribute<Segment, Integer> hoursOfDayWeekend;
	public static volatile SingularAttribute<Segment, Integer> hoursOfDay;
	public static volatile SingularAttribute<Segment, GeotargetType> geotargetType;
	public static volatile SingularAttribute<Segment, SegmentSafetyLevel> safetyLevel;
	public static volatile SetAttribute<Segment, Model> excludedModels;
	public static volatile SingularAttribute<Segment, Medium> medium;
	public static volatile SetAttribute<Segment, AdSpace> adSpaces;
	public static volatile SingularAttribute<Segment, ConnectionType> connectionType;
	public static volatile SetAttribute<Segment, Platform> platforms;
	public static volatile MapAttribute<Segment, DayOfWeek, Integer> dayParting;
	public static volatile SingularAttribute<Segment, Boolean> ipAddressesListWhitelist;
	public static volatile SetAttribute<Segment, Publisher> targettedPublishers;
	public static volatile SetAttribute<Segment, Operator> operators;
	public static volatile SingularAttribute<Segment, Boolean> channelEnabled;
	public static volatile SingularAttribute<Segment, Boolean> ispOperatorListIsWhitelist;
	public static volatile SingularAttribute<Segment, Boolean> includeAdfonicNetwork;
	public static volatile SingularAttribute<Segment, Long> id;
	public static volatile SetAttribute<Segment, Vendor> vendors;
	public static volatile SetAttribute<Segment, DeviceGroup> deviceGroups;
	public static volatile SingularAttribute<Segment, Advertiser> advertiser;
	public static volatile SingularAttribute<Segment, Boolean> incentivizedAllowed;
	public static volatile SetAttribute<Segment, Model> models;
	public static volatile SingularAttribute<Segment, Boolean> mobileOperatorListIsWhitelist;
	public static volatile SetAttribute<Segment, LocationTarget> locationTargets;
	public static volatile SetAttribute<Segment, Category> includedCategories;
	public static volatile SetAttribute<Segment, Country> countries;
	public static volatile SingularAttribute<Segment, Integer> daysOfWeek;
	public static volatile SetAttribute<Segment, Browser> browsers;
	public static volatile SingularAttribute<Segment, BigDecimal> genderMix;
	public static volatile SetAttribute<Segment, Geotarget> geotargets;
	public static volatile SingularAttribute<Segment, Boolean> explicitGPSEnabled;
	public static volatile SetAttribute<Segment, Channel> channels;
	public static volatile SingularAttribute<Segment, Integer> maxAge;
	public static volatile SingularAttribute<Segment, Integer> minAge;
	public static volatile MapAttribute<Segment, Capability, Boolean> capabilityMap;
	public static volatile SingularAttribute<Segment, String> name;
	public static volatile SingularAttribute<Segment, Boolean> countryListIsWhitelist;
	public static volatile SetAttribute<Segment, String> ipAddresses;
	public static volatile SetAttribute<Segment, Category> excludedCategories;

}

