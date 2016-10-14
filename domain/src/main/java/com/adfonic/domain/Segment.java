package com.adfonic.domain;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.MapKeyClass;
import javax.persistence.MapKeyColumn;
import javax.persistence.MapKeyEnumerated;
import javax.persistence.MapKeyJoinColumn;
import javax.persistence.Table;

import org.apache.commons.collections.CollectionUtils;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import com.adfonic.util.AgeRangeTargetingLogic;
import com.adfonic.util.NonBlockingCalendarPool;
import com.adfonic.util.TimeZoneUtils;

/**
 * A segment defines targeting criteria within an advertiser's campaign.
 * Segments can be reused by advertisers across multiple campaigns.
 */
@Entity
@Table(name="SEGMENT")
public class Segment extends BusinessKey implements Named {
    private static final long serialVersionUID = 17L;

    public static final int ALL_DAYS = 127; // All days
    public static final int ALL_HOURS = 0xFFFFFF; // All 24 hours

    // TODO: eliminate these and migrate code over to use AgeRangeTargetingLogic.*
    public static final int MIN_AGE = AgeRangeTargetingLogic.MIN_AGE;
    public static final int MAX_AGE = AgeRangeTargetingLogic.MAX_AGE;
    
    public static final BigDecimal DEFAULT_GENDER_MIX = new BigDecimal("0.5");
    public static final ConnectionType DEFAULT_CONNECTION_TYPE = ConnectionType.BOTH;

    public enum DayOfWeek {
    Sunday, Monday, Tuesday, Wednesday,
        Thursday, Friday, Saturday;
    }
    
    public enum SegmentSafetyLevel{
    	OFF, BRONZE, SILVER, GOLD;
    }

    @Id @GeneratedValue @Column(name="ID")
    private long id;
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="ADVERTISER_ID",nullable=false)
    private Advertiser advertiser;
    @NotCopied("TODO: verify this...NAME is nullable and not unique, so...?")
    @Column(name="NAME",length=255,nullable=true)
    private String name;

    // Targeting criteria
    @Column(name="DAYS_OF_WEEK",nullable=false)
    private int daysOfWeek; // lowest 7 bits, bit 0 = Sunday
    @Column(name="HOURS_OF_DAY",nullable=false)
    private int hoursOfDay; // lowest 24 bits, bit 0 = 0:00-1:00 in target user's local time zone
    @Column(name="HOURS_OF_DAY_WEEKEND",nullable=false)
    private int hoursOfDayWeekend; // same, but applies to Saturday/Sunday only
    @ManyToMany(fetch=FetchType.EAGER)
    @JoinTable(name="SEGMENT_COUNTRY",joinColumns=@JoinColumn(name="SEGMENT_ID",referencedColumnName="ID"),inverseJoinColumns=@JoinColumn(name="COUNTRY_ID",referencedColumnName="ID"))
    @Fetch(FetchMode.SELECT)
    private Set<Country> countries; // empty = don't care
    @ManyToMany(fetch=FetchType.EAGER)
    @JoinTable(name="SEGMENT_OPERATOR",joinColumns=@JoinColumn(name="SEGMENT_ID",referencedColumnName="ID"),inverseJoinColumns=@JoinColumn(name="OPERATOR_ID",referencedColumnName="ID"))
    @Fetch(FetchMode.SELECT)
    private Set<Operator> operators; // empty = don't care
    @Column(name="GENDER_MIX",nullable=true)
    private BigDecimal genderMix; // null = don't care, 1.0 = all male
    @Column(name="MIN_AGE",nullable=false)
    private int minAge;
    @Column(name="MAX_AGE",nullable=false)
    private int maxAge;
    @Column(name="MEDIUM",length=32,nullable=true)
    @Enumerated(EnumType.STRING)
    private Medium medium;

    @ManyToMany(fetch=FetchType.EAGER)
    @JoinTable(name="SEGMENT_VENDOR",joinColumns=@JoinColumn(name="SEGMENT_ID",referencedColumnName="ID"),inverseJoinColumns=@JoinColumn(name="VENDOR_ID",referencedColumnName="ID"))
    @Fetch(FetchMode.SELECT)
    private Set<Vendor> vendors; // empty = don't care
    @ManyToMany(fetch=FetchType.EAGER)
    @JoinTable(name="SEGMENT_MODEL",joinColumns=@JoinColumn(name="SEGMENT_ID",referencedColumnName="ID"),inverseJoinColumns=@JoinColumn(name="MODEL_ID",referencedColumnName="ID"))
    @Fetch(FetchMode.SELECT)
    private Set<Model> models; // empty = don't care
    @ManyToMany(fetch=FetchType.EAGER)
    @JoinTable(name="SEGMENT_BROWSER",joinColumns=@JoinColumn(name="SEGMENT_ID",referencedColumnName="ID"),inverseJoinColumns=@JoinColumn(name="BROWSER_ID",referencedColumnName="ID"))
    @Fetch(FetchMode.SELECT)
    private Set<Browser> browsers; // empty = don't care
    @ManyToMany(fetch=FetchType.EAGER)
    @JoinTable(name="SEGMENT_PLATFORM",joinColumns=@JoinColumn(name="SEGMENT_ID",referencedColumnName="ID"),inverseJoinColumns=@JoinColumn(name="PLATFORM_ID",referencedColumnName="ID"))
    @Fetch(FetchMode.SELECT)
    private Set<Platform> platforms; // empty = don't care
    @ElementCollection(fetch=FetchType.LAZY,targetClass=Boolean.class)
    @CollectionTable(name="SEGMENT_CAPABILITY_MAP",joinColumns=@JoinColumn(name="SEGMENT_ID",referencedColumnName="ID"))
    @MapKeyJoinColumn(name="CAPABILITY_ID",referencedColumnName="ID")
    @Column(name="VALUE",nullable=false)
    @Fetch(FetchMode.SELECT)
    private Map<Capability, Boolean> capabilityMap; // true = must have, false = must not have; nulls get removed from map
    
    //AD-123. Not used at the moment Jan 30 2013, will be used
    @ElementCollection(fetch=FetchType.EAGER,targetClass=Integer.class)
    @CollectionTable(name="SEGMENT_DAYPARTING",joinColumns=@JoinColumn(name="SEGMENT_ID",referencedColumnName="ID"))
    @MapKeyColumn(name="DAY_OF_WEEK",length=64,nullable=false)
    @MapKeyClass(DayOfWeek.class)
    @MapKeyEnumerated(EnumType.STRING)
    @Column(name="HOURS", nullable=false)
    @Fetch(FetchMode.SELECT)
    private Map<DayOfWeek, Integer> dayParting;

	@Column(name="CONNECTION_TYPE",length=32,nullable=false)
    @Enumerated(EnumType.STRING)
    private ConnectionType connectionType;
    @Column(name="MOBILE_OPERATOR_LIST_IS_WHITELIST",nullable=false)
    private boolean mobileOperatorListIsWhitelist; // true = targeting the operators set, false = targeting operators not in the operators set
    @Column(name="ISP_OPERATOR_LIST_IS_WHITELIST",nullable=false)
    private boolean ispOperatorListIsWhitelist; // true = targeting the operators set, false = targeting operators not in the operators set
    @ManyToMany(fetch=FetchType.LAZY)
    @JoinTable(name="SEGMENT_EXCLUDED_CATEGORY",joinColumns=@JoinColumn(name="SEGMENT_ID",referencedColumnName="ID"),inverseJoinColumns=@JoinColumn(name="CATEGORY_ID",referencedColumnName="ID"))
    @Fetch(FetchMode.SELECT)
    private Set<Category> excludedCategories;
    @ManyToMany(fetch=FetchType.LAZY)
    @JoinTable(name="SEGMENT_INCLUDED_CATEGORY",joinColumns=@JoinColumn(name="SEGMENT_ID",referencedColumnName="ID"),inverseJoinColumns=@JoinColumn(name="CATEGORY_ID",referencedColumnName="ID"))
    @Fetch(FetchMode.SELECT)
    private Set<Category> includedCategories;
    @ManyToMany(fetch=FetchType.LAZY)
    @JoinTable(name="SEGMENT_GEOTARGET",joinColumns=@JoinColumn(name="SEGMENT_ID",referencedColumnName="ID"),inverseJoinColumns=@JoinColumn(name="GEOTARGET_ID",referencedColumnName="ID"))
    @Fetch(FetchMode.SELECT)
    private Set<Geotarget> geotargets;
    @ElementCollection(fetch=FetchType.EAGER)
    @CollectionTable(name="SEGMENT_IP_ADDRESS",joinColumns=@JoinColumn(name="SEGMENT_ID"))
    @Column(name="IP_ADDRESS",length=18,nullable=false)
    @Fetch(FetchMode.SELECT)
    private Set<String> ipAddresses;
    @Column(name="IP_ADDRESS_LIST_IS_WHITELIST",nullable=false)
    private boolean ipAddressesListWhitelist;
    @Column(name="COUNTRY_LIST_IS_WHITELIST",nullable=false)
    private boolean countryListIsWhitelist; // true = targeting the countries set, false = targeting countries not in the set
    @ManyToMany(fetch=FetchType.LAZY)
    @JoinTable(name="SEGMENT_EXCLUDED_MODEL",joinColumns=@JoinColumn(name="SEGMENT_ID",referencedColumnName="ID"),inverseJoinColumns=@JoinColumn(name="MODEL_ID",referencedColumnName="ID"))
    @Fetch(FetchMode.SELECT)
    private Set<Model> excludedModels; // empty = don't care. Further restricts a targeted Platform.
    @Column(name="INCENTIVIZED_ALLOWED",nullable=false)
    private boolean incentivizedAllowed;
    @ManyToMany(fetch=FetchType.LAZY)
    @JoinTable(name="SEGMENT_AD_SPACE",joinColumns=@JoinColumn(name="SEGMENT_ID",referencedColumnName="ID"),inverseJoinColumns=@JoinColumn(name="AD_SPACE_ID",referencedColumnName="ID"))
    @Fetch(FetchMode.SELECT)
    private Set<AdSpace> adSpaces;
    @ManyToMany(fetch=FetchType.EAGER)
    @JoinTable(name="SEGMENT_CHANNEL",joinColumns=@JoinColumn(name="SEGMENT_ID",referencedColumnName="ID"),inverseJoinColumns=@JoinColumn(name="CHANNEL_ID",referencedColumnName="ID"))
    @Fetch(FetchMode.SELECT)
    private Set<Channel> channels;
    @Column(name="CHANNEL_ENABLED",nullable=false)
    private boolean channelEnabled;
    @Column(name="INCLUDE_ADFONIC_NETWORK",nullable=false)
    private boolean includeAdfonicNetwork;
    @ManyToMany(fetch=FetchType.LAZY)
    @JoinTable(name="SEGMENT_PUBLISHER",joinColumns=@JoinColumn(name="SEGMENT_ID",referencedColumnName="ID"),inverseJoinColumns=@JoinColumn(name="PUBLISHER_ID",referencedColumnName="ID"))
    @Fetch(FetchMode.SELECT)
    private Set<Publisher> targettedPublishers;
    
    @ManyToMany(fetch=FetchType.LAZY)
    @JoinTable(name="SEGMENT_LOCATION_TARGET",joinColumns=@JoinColumn(name="SEGMENT_ID",referencedColumnName="ID"),inverseJoinColumns=@JoinColumn(name="LOCATION_TARGET_ID",referencedColumnName="ID"))
    @Fetch(FetchMode.SELECT)
    private Set<LocationTarget> locationTargets; 
    
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="GEOTARGET_TYPE_ID",nullable=true)
    private GeotargetType geotargetType;
    
    @Column(name="EXPLICIT_GPS_ENABLED",nullable=false)
    private boolean explicitGPSEnabled;

    @ManyToMany(fetch=FetchType.LAZY)
    @JoinTable(name="SEGMENT_DEVICE_GROUP",joinColumns=@JoinColumn(name="SEGMENT_ID",referencedColumnName="ID"),inverseJoinColumns=@JoinColumn(name="DEVICE_GROUP_ID",referencedColumnName="ID"))
    @Fetch(FetchMode.SELECT)
    private Set<DeviceGroup> deviceGroups; 
    
    @Column(name="SAFETY_LEVEL",nullable=false)
    @Enumerated(EnumType.STRING)
    private SegmentSafetyLevel safetyLevel;
    


    //========================================================================
    // ***** WARNING *****
    // WHENEVER YOU ADD NEWS FIELDS, YOU NEED TO CONSIDER WHETHER OR NOT THEY
    // ARE INCLUDED IN THE FIELDS THAT GET COPIED WHEN copyFrom IS INVOKED.
    // ***** YOU ALSO NEED TO INCREMENT THE serialVersionUID. *****
    //========================================================================

    {
        this.daysOfWeek = ALL_DAYS; // All days
        this.hoursOfDay = ALL_HOURS; // All 24 hours
        this.hoursOfDayWeekend = ALL_HOURS; // Same
        this.countries = new HashSet<Country>();
        this.operators = new HashSet<Operator>();
        this.minAge = MIN_AGE;
        this.maxAge = MAX_AGE;
        this.genderMix = DEFAULT_GENDER_MIX;
        this.vendors = new HashSet<Vendor>();
        this.models = new HashSet<Model>();
        this.capabilityMap = new HashMap<Capability, Boolean>();
        this.platforms = new HashSet<Platform>();
        this.browsers = new HashSet<Browser>();
        this.excludedCategories = new HashSet<Category>();
        this.includedCategories = new HashSet<Category>();
        this.connectionType = DEFAULT_CONNECTION_TYPE;
        this.mobileOperatorListIsWhitelist = true;
        this.ispOperatorListIsWhitelist = true;
        this.geotargets = new HashSet<Geotarget>();
        this.ipAddresses = new HashSet<String>();
        this.ipAddressesListWhitelist = true;
        this.countryListIsWhitelist = true;
        this.excludedModels = new HashSet<Model>();
        this.incentivizedAllowed = false;
        this.adSpaces = new HashSet<AdSpace>();
        this.channels = new HashSet<Channel>();
        this.targettedPublishers = new HashSet<Publisher>();
        this.channelEnabled = false;
        this.dayParting = new HashMap<DayOfWeek,Integer>();
        this.locationTargets = new HashSet<LocationTarget>();
        this.explicitGPSEnabled = false;
        this.deviceGroups = new HashSet<DeviceGroup>();
        this.safetyLevel = SegmentSafetyLevel.SILVER;
    }

    Segment() {}

    /** Use factory method from Advertiser to construct. */
    public Segment(Advertiser advertiser) {
        this.advertiser = advertiser;
    }

    public long getId() { return id; };

    public Advertiser getAdvertiser() { return advertiser; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    /** For auditing purposes only. */
    public int getDaysOfWeek() { return daysOfWeek; }
    public void setDaysOfWeek(int daysOfWeek) {
        this.daysOfWeek = daysOfWeek;
    }

    public boolean[] getDaysOfWeekAsArray() {
        boolean[] result = new boolean[7];

        int pos = 0;
        for (DayOfWeek day : DayOfWeek.values()) {
            result[pos++] = (daysOfWeek & (1 << day.ordinal())) != 0;
        }
        return result;
    }

    public void setDaysOfWeekAsArray(boolean[] dayBits) {
        int value = 0;
        int pos = 0;
        for (DayOfWeek day : DayOfWeek.values()) {
            if (dayBits[pos++]) {
                value += 1 << day.ordinal();
            }
        }
        this.daysOfWeek = value;
    }

    /** For auditing purposes only. */
    public int getHoursOfDay() { return hoursOfDay; }
    public void setHoursOfDay(int hoursOfDay) {
        this.hoursOfDay = hoursOfDay;
    }

    public boolean[] getHoursOfDayAsArray() {
        boolean[] result = new boolean[24];
        int value = hoursOfDay;
        for (int i = 0; i < 24; i++) {
            result[i] = (value & 1) == 1;
            value = value >>> 1;
        }
        return result;
    }

    public void setHoursOfDayAsArray(boolean[] hourBits) {
        int value = 0;
        for (int i = 23; i >= 0; i--) {
            value = value << 1;
            if (hourBits[i]) {
                ++value;
            }
        }
        this.hoursOfDay = value;
    }

    /** For auditing purposes only. */
    public int getHoursOfDayWeekend() { return hoursOfDayWeekend; }
    public void setHoursOfDayWeekend(int hoursOfDayWeekend) {
        this.hoursOfDayWeekend = hoursOfDayWeekend;
    }

    public boolean[] getHoursOfDayWeekendAsArray() {
        boolean[] result = new boolean[24];
        int value = hoursOfDayWeekend;
        for (int i = 0; i < 24; i++) {
            result[i] = (value & 1) == 1;
            value = value >>> 1;
        }
        return result;
    }


    /**
     * Logic method to determine if the segment is enabled for
     * the given time in any time zone in the given country.
     */
    public boolean isTimeEnabled(Country country, Date date) {
        if (country == null) {
            // Without a country, all we can do is see if this segment
            // is enabled for all days/hours.
            return !isTimeTargeted();
        }

        // Use TimeZoneUtils since it caches for us
        String[] tzIDs = TimeZoneUtils.getAvailableIDs(country.getIsoCode());
        for (String tzID : tzIDs) {
            TimeZone tz = TimeZoneUtils.getTimeZoneNonBlocking(tzID);
            if (tz != null && isTimeEnabled(tz, date)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Logic method to determine if the segment is enabled for the
     * given time in the given time zone.
     */
    public boolean isTimeEnabled(TimeZone timeZone, Date date) {
        Calendar c = NonBlockingCalendarPool.acquireCalendar(date, timeZone);
        try {
            // DAY_OF_WEEK constants are SUNDAY=1 through SATURDAY=7
            int dayOfWeek = c.get(Calendar.DAY_OF_WEEK) - 1;

            if ((daysOfWeek & (1 << dayOfWeek)) != 0) {
                int hourOfDay = c.get(Calendar.HOUR_OF_DAY);
                if (dayOfWeek == DayOfWeek.Sunday.ordinal() || dayOfWeek == DayOfWeek.Saturday.ordinal()) {
                    return (hoursOfDayWeekend & (1 << hourOfDay)) != 0;
                } else {
                    return (hoursOfDay & (1 << hourOfDay)) != 0;
                }
            }
            return false;
        } finally {
            NonBlockingCalendarPool.releaseCalendar(c);
        }
    }

    public void setHoursOfDayWeekendAsArray(boolean[] hourBits) {
        int value = 0;
        for (int i = 23; i >= 0; i--) {
            value = value << 1;
            if (hourBits[i]) {
                ++value;
            }
        }
        this.hoursOfDayWeekend = value;
    }

    public Set<Country> getCountries() { return countries; }

    public boolean addCountries(Set<Country> newCountries) {
        if(CollectionUtils.isEmpty(newCountries)) {
            return false;
        }
        boolean addedSomething = false;
        for(Country country : newCountries) {
            if(!countries.contains(country)) {
                countries.add(country);
                addedSomething = true;
            }
        }
        return addedSomething;
    }

    public Set<Operator> getOperators() { return operators; }

    public boolean addOperators(Set<Operator> newOperators) {
        if(CollectionUtils.isEmpty(newOperators)) {
            return false;
        }
        boolean addedSomething = false;
        for(Operator operator : newOperators) {
            if(!operators.contains(operator)) {
                operators.add(operator);
                addedSomething = true;
            }
        }
        return addedSomething;
    }

    public Set<Model> getModels() { return models; }

    public boolean addModels(Set<Model> newModels) {
        if(CollectionUtils.isEmpty(newModels)) {
            return false;
        }
        boolean addedSomething = false;
        for(Model model : newModels) {
            if(!models.contains(model)) {
                models.add(model);
                addedSomething = true;
            }
        }
        return addedSomething;
    }


    public Set<Vendor> getVendors() { return vendors; }

    public boolean addVendors(Set<Vendor> newVendors) {
        if(CollectionUtils.isEmpty(newVendors)) {
            return false;
        }
        boolean addedSomething = false;
        for(Vendor vendor : newVendors) {
            if(!vendors.contains(vendor)) {
                vendors.add(vendor);
                addedSomething = true;
            }
        }
        return addedSomething;
    }


    public Set<Browser> getBrowsers() { return browsers; }

    public boolean addBrowsers(Set<Browser> newBrowsers) {
        if(CollectionUtils.isEmpty(newBrowsers)) {
            return false;
        }
        boolean addedSomething = false;
        for(Browser browser : newBrowsers) {
            if(!browsers.contains(browser)) {
                browsers.add(browser);
                addedSomething = true;
            }
        }
        return addedSomething;
    }


    public Map<Capability,Boolean> getCapabilityMap() {
        return capabilityMap;
    }

    public Set<Platform> getPlatforms() {
        return platforms;
    }

    public int getMinAge() { return minAge; }
    public void setMinAge(int minAge) { this.minAge = minAge; }

    public int getMaxAge() { return maxAge; }
    public void setMaxAge(int maxAge) { this.maxAge = maxAge; }

    public Medium getMedium() { return medium; }
    public void setMedium(Medium medium) { this.medium = medium; }

    public BigDecimal getGenderMix() { return genderMix; }
    public void setGenderMix(BigDecimal genderMix) {
        this.genderMix = genderMix;
    }

    public Set<Category> getExcludedCategories() { return excludedCategories; }

    public Set<Category> getIncludedCategories() { return includedCategories; }

    public Set<Geotarget> getGeotargets() { return geotargets; }

    // Helper methods to check if various types of targeting are enabled
    public boolean isEveryDay() {
        return daysOfWeek == ALL_DAYS;
    }

    public boolean isAllHoursWeekdays() {
        return hoursOfDay == ALL_HOURS;
    }

    public boolean isAllHoursWeekends() {
        return hoursOfDayWeekend == ALL_HOURS;
    }

    public boolean isGeographyTargeted() {
        return !countries.isEmpty();
    }

    public boolean isTimeTargeted() {
        return !(isEveryDay() && isAllHoursWeekdays() && isAllHoursWeekends());
    }

    public boolean isConnectionTargeted() {
        return (connectionType != ConnectionType.BOTH) || (!operators.isEmpty());
    }

    public boolean isDeviceTargeted() {
        return !vendors.isEmpty() || !models.isEmpty() || !platforms.isEmpty() || !capabilityMap.isEmpty() || !browsers.isEmpty();
    }

    public boolean isDemographicTargeted() {
        return minAge != MIN_AGE || maxAge != MAX_AGE
            || (genderMix != null && (DEFAULT_GENDER_MIX.compareTo(genderMix) != 0));
    }

    /**
     * Auditing helpers
     */
    public String getCountriesAsString() {
        return NamedUtils.namedCollectionToString(countries);
    }

    public String getOperatorsAsString() {
        return NamedUtils.namedCollectionToString(operators);
    }

    public String getModelsAsString() {
        return NamedUtils.namedCollectionToString(models);
    }

    public String getVendorsAsString() {
        return NamedUtils.namedCollectionToString(vendors);
    }

    public String getBrowsersAsString() {
        return NamedUtils.namedCollectionToString(browsers);
    }

    public String getChannelsAsString() {
        return NamedUtils.namedCollectionToString(channels);
    }

    public String getPlatformsAsString() {
        return NamedUtils.namedCollectionToString(platforms);
    }

    public String getTargetedPublishersAsString() {
        return NamedUtils.namedCollectionToString(targettedPublishers);
    }
    
    public String getLocationTargetsAsString() {
        return NamedUtils.namedCollectionToString(locationTargets);
    }

    public boolean getMobileOperatorListIsWhitelist() {
        return mobileOperatorListIsWhitelist;
    }

    public void setMobileOperatorListIsWhitelist(boolean mobileOperatorListIsWhitelist) {
        this.mobileOperatorListIsWhitelist = mobileOperatorListIsWhitelist;
    }

    public boolean getIspOperatorListIsWhitelist() {
        return ispOperatorListIsWhitelist;
    }

    public void setIspOperatorListIsWhitelist(boolean ispOperatorListIsWhitelist) {
        this.ispOperatorListIsWhitelist = ispOperatorListIsWhitelist;
    }

    public ConnectionType getConnectionType() {
        return connectionType;
    }

    public void setConnectionType(ConnectionType connectionType) {
        this.connectionType = connectionType;
    }

    public Set<String> getIpAddresses() {
        return ipAddresses;
    }

    public boolean isIpAddressesListWhitelist() {
        return ipAddressesListWhitelist;
    }

    public void setIpAddressesListWhitelist(boolean ipAddressesListWhitelist) {
        this.ipAddressesListWhitelist = ipAddressesListWhitelist;
    }

    public boolean getCountryListIsWhitelist() {
        return countryListIsWhitelist;
    }

    public void setCountryListIsWhitelist(boolean countryListIsWhitelist) {
        this.countryListIsWhitelist = countryListIsWhitelist;
    }

    public Set<Model> getExcludedModels() {
        return excludedModels;
    }

    public boolean isIncentivizedAllowed() {
        return incentivizedAllowed;
    }
    public void setIncentivizedAllowed(boolean incentivizedAllowed) {
        this.incentivizedAllowed = incentivizedAllowed;
    }

    public Set<AdSpace> getAdSpaces() {
        return adSpaces;
    }

    public Set<Channel> getChannels() {
        return channels;
    }

    public boolean isChannelEnabled() {
        return channelEnabled;
    }
    public void setChannelEnabled(boolean channelEnabled) {
        this.channelEnabled = channelEnabled;
    }
    public void setChannels(Set<Channel> channels) {
        this.channels=channels;
    }

    public boolean isIncludeAdfonicNetwork() {
        return includeAdfonicNetwork;
    }

    public void setIncludeAdfonicNetwork(boolean includeAdfonicNetwork) {
        this.includeAdfonicNetwork = includeAdfonicNetwork;
    }

    public Set<Publisher> getTargettedPublishers() {
        return targettedPublishers;
    }

    public void setTargettedPublishers(Set<Publisher> targettedPublishers) {
        this.targettedPublishers = targettedPublishers;
    }
    
    public Map<DayOfWeek, Integer> getDayParting() {
		return dayParting;
	}

	public Set<LocationTarget> getLocationTargets() {
		return locationTargets;
	}

	public void setLocationTargets(Set<LocationTarget> locationTargets) {
		this.locationTargets = locationTargets;
	}

	public GeotargetType getGeotargetType() {
		return geotargetType;
	}

	public void setGeotargetType(GeotargetType geotargetType) {
		this.geotargetType = geotargetType;
	}

	public boolean isExplicitGPSEnabled() {
		return explicitGPSEnabled;
	}

	public void setExplicitGPSEnabled(boolean explicitGPSEnabled) {
		this.explicitGPSEnabled = explicitGPSEnabled;
	}

	public Set<DeviceGroup> getDeviceGroups() {
		return deviceGroups;
	}

	public void setDeviceGroups(Set<DeviceGroup> deviceGroups) {
		this.deviceGroups = deviceGroups;
	}
    
	public SegmentSafetyLevel getSafetyLevel(){
    	return  safetyLevel;
    }
    
    public void setSafetyLevel(SegmentSafetyLevel safetyLevel){
    	this.safetyLevel = safetyLevel;
    }
    
    //For AuditLog purposes
    public String getGenderMixHumanReadable(){
    	StringBuffer sb = new StringBuffer();
    	sb.append(genderMix.multiply(new BigDecimal(100)).intValue()).append("% / ");
    	sb.append(new BigDecimal(1).subtract(genderMix).multiply(new BigDecimal(100)).intValue()).append("%");
    	return sb.toString();
    }
    
    public String getDeviceGroupHumanReadable() {
        if (deviceGroups == null || deviceGroups.isEmpty()){
            return "ALL";
        }
        StringBuilder sb = new StringBuilder();
        boolean sawFirst = false;
        for (DeviceGroup dg : deviceGroups) {
            if (sawFirst) {
                sb.append(',');
            } else {
                sawFirst = true;
            }
            sb.append(dg.getSystemName());
        }
        return sb.toString();
    }
    
    public String getMobileOperatorsHumanReadable() {
        if(connectionType.equals("WIFI") || connectionType.equals("NONE")){
            return "NONE";
        }
        if (operators == null || operators.isEmpty()){
            return "ALL";
        }
        StringBuilder sb = new StringBuilder();
        boolean sawFirst = false;
        for (Operator o : operators) {
            if(o.isMobileOperator()){
                if (sawFirst) {
                    sb.append(',');
                } else {
                    sawFirst = true;
                }
                
                sb.append(o.getName());
            }
        }
        return sb.toString();
    }
    
    public String getIspOperatorsHumanReadable() {
        if(connectionType.equals("OPERATOR") || connectionType.equals("NONE")){
            return "NONE";
        }
        if (operators == null || operators.isEmpty()){
            return "ALL";
        }
        StringBuilder sb = new StringBuilder();
        boolean sawFirst = false;
        for (Operator o : operators) {
            if(!o.isMobileOperator()){
                if (sawFirst) {
                    sb.append(',');
                } else {
                    sawFirst = true;
                }
                
                sb.append(o.getName());
            }
        }
        return sb.toString();
    }
}
