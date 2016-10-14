package com.adfonic.domain;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.persistence.*;

import com.adfonic.util.DateUtils;

/**
 * Represents one "advertiser" identity of a given company -- either the sole
 * advertiser identity, or one of several agencies associated with the company,
 * depending on Company.accountTypeFlags.
 */
@Entity
@Table(name="ADVERTISER")
public class Advertiser extends BusinessKey implements Named, HasExternalID {
    private static final long serialVersionUID = 2L;

    public enum Status { ACTIVE, INACTIVE }

    @Id @GeneratedValue @Column(name="ID")
    private long id;
    @Column(name="EXTERNAL_ID",length=255,nullable=false)
    private String externalID;
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="COMPANY_ID",nullable=false)
    private Company company;
    @Column(name="NAME",length=255,nullable=false)
    private String name;
    @Column(name="CREATION_TIME",nullable=false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date creationTime;
    @Column(name="STATUS",length=16,nullable=false)
    @Enumerated(EnumType.STRING)
    private Status status;
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="ACCOUNT_ID",nullable=false)
    private Account account;
    @OneToMany(mappedBy="advertiser",fetch=FetchType.LAZY)
    private Set<Segment> segments;
    @OneToMany(mappedBy="advertiser",fetch=FetchType.LAZY)
    private Set<Campaign> campaigns;
    @OneToMany(mappedBy="advertiser",fetch=FetchType.LAZY)
    private Set<Destination> destinations;
    @Column(name="DAILY_BUDGET",nullable=true)
    private BigDecimal dailyBudget; // null if disabled
    @Column(name="NOTIFY_LIMIT",nullable=true)
    private BigDecimal notifyLimit; // null if disabled
    @Column(name="NOTIFY_ADDITIONAL_EMAILS",length=1024,nullable=true)
    private String notifyAdditionalEmails;
    @ElementCollection(fetch=FetchType.LAZY,targetClass=BudgetSpend.class)
    @CollectionTable(name="ADVERTISER_DAILY_SPEND",joinColumns=@JoinColumn(name="ADVERTISER_ID",referencedColumnName="ID"))
    @MapKeyColumn(name="DATE_ID")
    @MapKeyClass(Integer.class)
    private Map<Integer,BudgetSpend> dailySpendMap;
    //@OneToMany(mappedBy="advertiser",fetch=FetchType.LAZY)
    @ManyToMany(mappedBy="advertisers",fetch=FetchType.EAGER)
    private Set<User> users;

    // adops support fields
    @Column(name="IS_KEY",nullable=false)
    private boolean key;
    @Column(name="IS_MANAGED_TRAFFICKING",nullable=false)
    private boolean managedTrafficking;
    @Column(name="IS_MANAGED_DELIVERY",nullable=false)
    private boolean managedDelivery;
    @Column(name="IS_CONVERSION_PROTECTED",nullable=false)
    private boolean conversionProtected;
    @ManyToOne(fetch=FetchType.EAGER)
    @JoinColumn(name="AD_OPS_OWNER_ID",nullable=true)
    private AdfonicUser adOpsOwner;
    @ManyToOne(fetch=FetchType.EAGER)
    @JoinColumn(name="SALES_OWNER_ID",nullable=true)
    private AdfonicUser salesOwner;

    @OneToMany(mappedBy="advertiser",fetch=FetchType.LAZY)
    private Set<AdvertiserNotificationFlag> notificationFlags;

    @ManyToMany(fetch=FetchType.LAZY)
    @JoinTable(name="ADVERTISER_CROSS_TARGET",joinColumns=@JoinColumn(name="ADVERTISER_ID",referencedColumnName="ID"),inverseJoinColumns=@JoinColumn(name="OTHER_ADVERTISER_ID",referencedColumnName="ID"))
    private Set<Advertiser> crossTargetAdvertisers;
    
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="BID_SEAT_ID",nullable=false)
    private BidSeat pmpBidSeat;
    
    @Column(name="ENABLE_RTB_BID_SEAT", nullable=false)
    private Boolean  enableRtbBidSeat;
    
    @ManyToMany(fetch=FetchType.LAZY)
    @JoinTable(name="ADVERTISER_BID_SEAT",joinColumns=@JoinColumn(name="ADVERTISER_ID",referencedColumnName="ID"),inverseJoinColumns=@JoinColumn(name="BID_SEAT_ID",referencedColumnName="ID"))
    private Set<BidSeat> advertiserRtbBidSeats;
    
    @OneToOne(fetch=FetchType.EAGER)
    @JoinColumn(name="DEFAULT_CURRENCY_EXCHANGE_RATE_ID", nullable=true)
    private CurrencyExchangeRate defaultCurrencyExchangeRate;

    {
        this.externalID = UUID.randomUUID().toString();
        this.creationTime = new Date();
        this.status = Status.ACTIVE;
        this.account = new Account(AccountType.ADVERTISER);
        this.segments = new HashSet<Segment>();
        this.campaigns = new HashSet<Campaign>();
        this.destinations = new HashSet<Destination>();
        this.dailySpendMap = new HashMap<Integer,BudgetSpend>();
        this.users = new HashSet<User>();
        this.key = false;
        this.managedDelivery = false;
        this.managedTrafficking = false;
        this.notificationFlags = new HashSet<AdvertiserNotificationFlag>();
        this.crossTargetAdvertisers = new HashSet<Advertiser>();
        this.advertiserRtbBidSeats = new HashSet<BidSeat>();
    }

    Advertiser() {}

    public Advertiser(Company company, String name) {
        this.company = company;
        this.name = name;
    }

    public long getId() { return id; };

    public String getExternalID() {
        return externalID;
    }

    public Company getCompany() {
        return company;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public Date getCreationTime() {
        return creationTime;
    }
    public void setCreationTime(Date creationTime) {
        this.creationTime = creationTime;
    }

    public Status getStatus() {
        return status;
    }
    public void setStatus(Status status) {
        this.status = status;
    }

    public Account getAccount() {
        return account;
    }

    public Set<Segment> getSegments() {
        return segments;
    }

    public Set<Campaign> getCampaigns() {
        return campaigns;
    }

    public Set<Destination> getDestinations() {
        return destinations;
    }

    public Segment newSegment() {
        Segment segment = new Segment(this);
        segments.add(segment);
        return segment;
    }

    public Destination newDestination(DestinationType destinationType, String urlString, List<BeaconUrl> beacons) {
    	return newDestination(destinationType, urlString, beacons, true, null);
    }
    
    public Destination newDestination(DestinationType destinationType, String urlString, List<BeaconUrl> beacons, boolean dataIsFinalDestination, String finalDestination) {
        Destination destination = new Destination(this, destinationType, urlString, dataIsFinalDestination, finalDestination);
        if(beacons!=null){
            for(BeaconUrl beacon : beacons){
                destination.addBeaconUrl(beacon);
            }
        }
        destinations.add(destination);
        return destination;
    }

    public BigDecimal getDailyBudget() {
        return dailyBudget;
    }
    public void setDailyBudget(BigDecimal dailyBudget) {
        this.dailyBudget = dailyBudget;
    }

    public BigDecimal getNotifyLimit() {
        return notifyLimit;
    }
    public void setNotifyLimit(BigDecimal notifyLimit) {
        this.notifyLimit = notifyLimit;
    }

    public String getNotifyAdditionalEmails() {
        return notifyAdditionalEmails;
    }
    public void setNotifyAdditionalEmails(String notifyAdditionalEmails) {
        this.notifyAdditionalEmails = notifyAdditionalEmails;
    }

    public Map<Integer,BudgetSpend> getDailySpendMap() { return dailySpendMap; }

    public BudgetSpend getBudgetSpend(Date date) {
        return dailySpendMap.get(DateUtils.getTimeID(date, company.getDefaultTimeZone()) / 100);
    }

    public Set<User> getUsers() {
        return users;
    }

    public boolean isKey() {
        return key;
    }
    public void setKey(boolean key) {
        this.key = key;
    }

    public boolean isManagedTrafficking() {
        return managedTrafficking;
    }

    public void setManagedTrafficking(boolean managedTrafficking) {
        this.managedTrafficking = managedTrafficking;
    }

    public boolean isManagedDelivery() {
        return managedDelivery;
    }

    public void setManagedDelivery(boolean managedDelivery) {
        this.managedDelivery = managedDelivery;
    }

    public AdfonicUser getAdOpsOwner() {
        return adOpsOwner;
    }
    public void setAdOpsOwner(AdfonicUser adOpsOwner) {
        this.adOpsOwner = adOpsOwner;
    }

    public AdfonicUser getSalesOwner() {
        return salesOwner;
    }
    public void setSalesOwner(AdfonicUser salesOwner) {
        this.salesOwner = salesOwner;
    }

    public Set<AdvertiserNotificationFlag> getNotificationFlags() {
        return notificationFlags;
    }

    public Set<Advertiser> getCrossTargetAdvertisers() {
        return crossTargetAdvertisers;
    }
    
	public BidSeat getPmpBidSeat() {
        return pmpBidSeat;
    }

    public void setPmpBidSeat(BidSeat pmpBidSeat) {
        this.pmpBidSeat = pmpBidSeat;
    }

    public boolean isConversionProtected() {
		return conversionProtected;
	}

	public void setConversionProtected(boolean conversionProtected) {
		this.conversionProtected = conversionProtected;
	}

    public Boolean getEnableRtbBidSeat() {
        return enableRtbBidSeat;
    }

    public void setEnableRtbBidSeat(Boolean enableRtbBidSeat) {
        this.enableRtbBidSeat = enableRtbBidSeat;
    }

    public Set<BidSeat> getAdvertiserRtbBidSeats() {
        return advertiserRtbBidSeats;
    }

    public void setAdvertiserRtbBidSeats(Set<BidSeat> advertiserRtbBidSeats) {
        this.advertiserRtbBidSeats = advertiserRtbBidSeats;
    }
    
    public CurrencyExchangeRate getDefaultCurrencyExchangeRate() {
        return defaultCurrencyExchangeRate;
    }

    public void setDefaultCurrencyExchangeRate(CurrencyExchangeRate defaultCurrencyExchangeRate) {
        this.defaultCurrencyExchangeRate = defaultCurrencyExchangeRate;
    }
}
