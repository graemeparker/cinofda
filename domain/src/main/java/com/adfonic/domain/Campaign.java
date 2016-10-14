package com.adfonic.domain;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

import javax.persistence.CascadeType;
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
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.MapKeyClass;
import javax.persistence.MapKeyColumn;
import javax.persistence.MapKeyJoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLDeleteAll;
import org.hibernate.annotations.Where;

import com.adfonic.domain.CampaignBid.BidModelType;

/**
 * A campaign is an advertiser's group of ads that are to be tracked
 * together.  A campaign has an optional start and end date, and an ordered
 * list of segments (the order is for display purposes).
 *
 * A campaign is also the "owner" of Creatives, though this relationship
 * is traversed via the Segments.
 */
@Entity
@Table(name="CAMPAIGN")
public class Campaign extends BusinessKey implements Named, HasExternalID {
    private static final long serialVersionUID = 19L;

    public enum Status {
        NEW, NEW_REVIEW, DELETED, PENDING, PENDING_PAUSED, ACTIVE, PAUSED, COMPLETED, STOPPED;
    }
    
    public enum InventoryTargetingType {
    	RUN_OF_NETWORK, WHITELIST, CATEGORY, PRIVATE_MARKET_PLACE;
    }
    
    public enum BudgetType {
    	MONETARY, IMPRESSIONS, CLICKS
    }
    
    public enum BiddingStrategy {
        MEDIA_COST_OPTIMISATION, AVERAGE_MAXIMUM_BID
    }
    
    protected enum CapPeriodSecondsEnum{
    	HOUR (60*60), DAY  (60*60*24), WEEK (60*60*24*7), MONTH(60*60*24*30);
    	private Integer seconds;
    	private CapPeriodSecondsEnum(Integer seconds){this.seconds = seconds;}
    	public Integer getSeconds(){return seconds;}
    }

    @Id @GeneratedValue @Column(name="ID")
    private long id;
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="ADVERTISER_ID",nullable=false)
    private Advertiser advertiser;

    @NotCopied("Copied campaigns get their own new name")
    @Column(name="NAME",length=255,nullable=false)
    private String name;

    @NotCopied("Auto-generated when created (or copied)")
    @Column(name="EXTERNAL_ID",length=255,nullable=true)
    private String externalID;

    @Column(name="DESCRIPTION",nullable=true)
    @Lob
    private String description;

    // This date is INCLUSIVE (at least it is in adserver)
    @NotCopied("TODO: verify this")
    @Column(name="START_DATE",nullable=true)
    @Temporal(TemporalType.TIMESTAMP)
    private Date startDate;

    // This date is EXCLUSIVE (at least it is in adserver)
    @NotCopied("TODO: verify this")
    @Column(name="END_DATE",nullable=true)
    @Temporal(TemporalType.TIMESTAMP)
    private Date endDate;

    @NotCopied("This gets set automatically during status transitions")
    @Column(name="ACTIVATION_DATE",nullable=true)
    @Temporal(TemporalType.TIMESTAMP)
    private Date activationDate;

    @NotCopied("This gets set automatically during status transitions")
    @Column(name="DEACTIVATION_DATE",nullable=true)
    @Temporal(TemporalType.TIMESTAMP)
    private Date deactivationDate;

    @ManyToMany(fetch=FetchType.LAZY)
    @JoinTable(name="CAMPAIGN_SEGMENT",joinColumns=@JoinColumn(name="CAMPAIGN_ID",referencedColumnName="ID"),inverseJoinColumns=@JoinColumn(name="SEGMENT_ID",referencedColumnName="ID"))
    //@OrderColumn(name="CAMPAIGN_ORDER",nullable=false,insertable=true,updatable=true)
    @OrderBy("id")
    private List<Segment> segments;

    @Column(name="DAILY_BUDGET",nullable=true)
    private BigDecimal dailyBudget; // null if none

    @Column(name="DAILY_BUDGET_WEEKDAY",nullable=true)
    private BigDecimal dailyBudgetWeekday; // null if none

    @Column(name="DAILY_BUDGET_WEEKEND",nullable=true)
    private BigDecimal dailyBudgetWeekend; // null if none

    @Column(name="DAILY_BUDGET_ALERT_ENABLED",nullable=false)
    private boolean dailyBudgetAlertEnabled;

    @Column(name="OVERALL_BUDGET",nullable=true)
    private BigDecimal overallBudget; // null if none

    @Column(name="OVERALL_BUDGET_ALERT_ENABLED",nullable=false)
    private boolean overallBudgetAlertEnabled;
    
 
    @Column(name="DAILY_BUDGET_IMPRESSIONS",nullable=true)
    private BigDecimal dailyBudgetImpressions; // null if none
    
    @Column(name="OVERALL_BUDGET_IMPRESSIONS",nullable=true)
    private BigDecimal overallBudgetImpressions; // null if none
    
    @Column(name="DAILY_BUDGET_CLICKS",nullable=true)
    private BigDecimal dailyBudgetClicks; // null if none
    
    @Column(name="OVERALL_BUDGET_CLICKS",nullable=true)
    private BigDecimal overallBudgetClicks; // null if none
    
    @Column(name="DAILY_BUDGET_CONVERSIONS",nullable=true)
    private BigDecimal dailyBudgetConversions; // null if none
    
    @Column(name="OVERALL_BUDGET_CONVERSIONS",nullable=true)
    private BigDecimal overallBudgetConversions; // null if none
    
    @Column(name="BUDGET_TYPE",nullable=true)
    @Enumerated(EnumType.STRING)
    private BudgetType budgetType;
    

    // Careful here. Even tho the relationship is marked as LAZY, OneToOne lazies are not supported by Hibernate.
    // The object will always be hydrated.
    @NotCopied("A trigger creates a new one for every new (or copied) campaign")
    @OneToOne(mappedBy="campaign",fetch=FetchType.LAZY)
    private CampaignOverallSpend overallSpend;

    @NotCopied("Every new (or copied) campaign has its own status flow")
    @Column(name="STATUS",length=32,nullable=false)
    @Enumerated(EnumType.STRING)
    private Status status;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="DEFAULT_LANGUAGE_ID",nullable=false)
    private Language defaultLanguage; // used for new creatives

    @Column(name="DISABLE_LANGUAGE_MATCH",nullable=false)
    private boolean disableLanguageMatch;

    @Column(name="REFERENCE",length=255,nullable=true)
    private String reference;
    
    @Column(name="OPPORTUNITY",length=255,nullable=true)
    private String opportunity;

    @ManyToMany(fetch=FetchType.LAZY)
    @JoinTable(name="CAMPAIGN_TRANSPARENT_NETWORK",joinColumns=@JoinColumn(name="CAMPAIGN_ID",referencedColumnName="ID"),inverseJoinColumns=@JoinColumn(name="TRANSPARENT_NETWORK_ID",referencedColumnName="ID"))
    private Set<TransparentNetwork> transparentNetworks; // empty = Performance network

    @NotCopied("This is something we apply directly, the advertiser can't control it")
    @Column(name="BOOST_FACTOR",nullable=false)
    private double boostFactor = 1.0; // default is no boost up or down

    @NotCopied
    @Column(name="SUMMARY_DISPLAY_DISABLED",nullable=false)
    private boolean summaryDisplayDisabled; // true == owner has marked hidden on tools

    @Column(name="PRICE_OVERRIDDEN",nullable=false)
    private boolean priceOverridden;

    @Column(name="INSTALL_TRACKING_ADX_ENABLED",nullable=false)
    private boolean installTrackingAdXEnabled;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="CATEGORY_ID",nullable=false)
    private Category category;
    /**
     * Frequency capping.  If capImpressions is non-null, the adserver will
     * show each creative to a unique user a maximum of capImpressions times
     * every capPeriodSeconds.
     *
     * If capImpressions is null, the system will use an adserver-defined
     * default that may change over time.
     *
     * If capImpressions is zero or negative, the campaign will be treated
     * as having no cap.
     */
    @Column(name="CAP_IMPRESSIONS",nullable=true)
    private Integer capImpressions;

    @Column(name="CAP_PERIOD_SECONDS",nullable=true)
    private Integer capPeriodSeconds;
    
    // MAD-3278 
    @Column(name="CAP_PER_CAMPAIGN",nullable=false)
    private boolean capPerCampaign;

    // Install tracking for iPhone apps
    @Column(name="INSTALL_TRACKING_ENABLED",nullable=false)
    private boolean installTrackingEnabled;

    @NotCopied("Do not copy verification status, must be reverified")
    @Column(name="INSTALL_TRACKING_VERIFIED",nullable=false)
    private boolean installTrackingVerified;

    @Column(name="APPLICATION_ID",length=255,nullable=true)
    private String applicationID;

    @Column(name="CONVERSION_TRACKING_ENABLED",nullable=false)
    private boolean conversionTrackingEnabled;

    @NotCopied("Do not copy verification status, must be reverified")
    @Column(name="CONVERSION_TRACKING_VERIFIED",nullable=false)
    private boolean conversionTrackingVerified;

    @Column(name="HOUSE_AD",nullable=false)
    private boolean houseAd;
    @Column(name="THROTTLE",nullable=false)
    private int throttle;

    @NotCopied("This is created automatically by accounting logic in the database")
    @ElementCollection(fetch=FetchType.LAZY,targetClass=BudgetSpend.class)
    @CollectionTable(name="CAMPAIGN_DAILY_SPEND",joinColumns=@JoinColumn(name="CAMPAIGN_ID",referencedColumnName="ID"))
    @MapKeyColumn(name="DATE_ID")
    @MapKeyClass(Integer.class)
    private Map<Integer,BudgetSpend> dailySpendMap;

    @OneToMany(fetch=FetchType.LAZY)
    //@OrderColumn(name="CAMPAIGN_ORDER",nullable=false,insertable=true,updatable=false)
    @OrderBy("id")
    @JoinColumn(name="CAMPAIGN_ID",nullable=true)
    private List<Creative> creatives;

    // Careful here. Even tho the relationship is marked as LAZY, OneToOne lazies are not supported by Hibernate.
    // The object will always be hydrated.
    @OneToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="CURRENT_BID_ID",nullable=true)
    private CampaignBid currentBid;
    
    @OneToMany(fetch = FetchType.LAZY)
    @JoinTable(name="CAMPAIGN_CURRENT_BID_DEDUCTION",joinColumns=@JoinColumn(name="CAMPAIGN_ID",referencedColumnName="ID"),inverseJoinColumns=@JoinColumn(name="BID_DEDUCTION_ID",referencedColumnName="BID_DEDUCTION_ID"))
    private Set<BidDeduction> currentBidDeductions;

    @NotCopied("This accumulates naturally as bids are created")
    @OneToMany(mappedBy="campaign",fetch=FetchType.LAZY)
    private Set<CampaignBid> historicalBids;

    @NotCopied
    @OneToMany(mappedBy="campaign",fetch=FetchType.LAZY, cascade=CascadeType.PERSIST)
    private Set<CampaignTimePeriod> timePeriods;

    @NotCopied
    @OneToMany(mappedBy="campaign",fetch=FetchType.LAZY)
    private Set<CampaignNotificationFlag> notificationFlags;

    private static final CampaignTimePeriod GAP = new CampaignTimePeriod();

    private transient CampaignTimePeriod currentTimePeriod;
    private transient CampaignTimePeriod nextTimePeriod;

    @NotCopied("Don't copy auto-optimisation fields when a campaign is copied")
    @Column(name="TARGET_CPA",nullable=true)
    private BigDecimal targetCPA;

    @NotCopied("Don't copy auto-optimisation fields when a campaign is copied")
    @Column(name="OPTIMIZATION_MAX_REMOVAL",nullable=true)
    private BigDecimal optimizationMaxRemoval ;

    @NotCopied("Don't copy auto-optimisation fields when a campaign is copied")
    @Column(name="OPTIMIZATION_MULTIPLIER",nullable=true)
    private BigDecimal optimizationMultiplier;

    @Column(name="ADVERTISER_DOMAIN",length=255,nullable=true)
    private String advertiserDomain;
    
    // BL-275: Even distribution
    @Column(name="EVEN_DISTRIBUTION_OVERALL_BUDGET",nullable=false)
    private boolean evenDistributionOverallBudget;

    @Column(name="EVEN_DISTRIBUTION_DAILY_BUDGET",nullable=false)
    private boolean evenDistributionDailyBudget;

    @ManyToMany(fetch=FetchType.LAZY)
    @JoinTable(name="CAMPAIGN_DEVICE_IDENTIFIER_TYPE",joinColumns=@JoinColumn(name="CAMPAIGN_ID",referencedColumnName="ID"),inverseJoinColumns=@JoinColumn(name="DEVICE_IDENTIFIER_TYPE_ID",referencedColumnName="ID"))
    private Set<DeviceIdentifierType> deviceIdentifierTypes;

    @OneToMany(fetch=FetchType.LAZY)
    @JoinTable(name="CAMPAIGN_REMOVED_PUBLICATION_MAP",joinColumns=@JoinColumn(name="CAMPAIGN_ID",referencedColumnName="ID"),inverseJoinColumns=@JoinColumn(name="REMOVAL_INFO_ID",referencedColumnName="ID"))
    @MapKeyJoinColumn(name="PUBLICATION_ID",referencedColumnName="ID")
    private Map<Publication,RemovalInfo> removedPublications;

    @NotCopied("Every campaign will have its own set of watchers")
    @ManyToMany(fetch=FetchType.LAZY)
    @JoinTable(name="CAMPAIGN_WATCHER",joinColumns=@JoinColumn(name="CAMPAIGN_ID",referencedColumnName="ID"),inverseJoinColumns=@JoinColumn(name="ADFONIC_USER_ID",referencedColumnName="ID"))
    private Set<AdfonicUser> watchers;
    
    // Careful here. Even tho the relationship is marked as LAZY, OneToOne lazies are not supported by Hibernate.
    // The object will always be hydrated.
    @OneToOne(mappedBy="campaign",fetch=FetchType.LAZY)
    private CampaignTargetCTR targetCTR;

    @OneToOne(mappedBy="campaign",fetch=FetchType.LAZY)
    private CampaignTargetCVR targetCVR;
    
    @OneToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="CURRENT_RM_AD_SERVING_FEE_ID",nullable=true)
    private CampaignRichMediaAdServingFee currentRichMediaAdServingFee;

    @NotCopied("This accumulates naturally as rich media serving fees are created")
    @OneToMany(mappedBy="campaign",fetch=FetchType.LAZY)
    private Set<CampaignRichMediaAdServingFee> historicalRMAdServingFees;
    
    @OneToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="CURRENT_TRADING_DESK_MARGIN_ID",nullable=true)
    private CampaignTradingDeskMargin currentTradingDeskMargin;

    @NotCopied("This accumulates naturally as trading desk margin fees are created")
    @OneToMany(mappedBy="campaign",fetch=FetchType.LAZY)
    private Set<CampaignTradingDeskMargin> historicalTDMarginFees;
    
    @OneToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="CURRENT_DATA_FEE_ID",nullable=true)
    private CampaignDataFee currentDataFee;

    @OneToMany(fetch = FetchType.LAZY)
	@JoinColumn(name = "CAMPAIGN_ID", nullable = true)
    private Set<CampaignDataFee> historicalDataFees;
    
    @Column(name="INVENTORY_TARGETING_TYPE", nullable=true)
    @Enumerated(EnumType.STRING)
    private InventoryTargetingType inventoryTargetingType;
    
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="PUBLICATION_LIST_ID",nullable=true)
    private PublicationList publicationList;
    
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="PRIVATE_MARKET_PLACE_DEAL_ID",nullable=true)
    private PrivateMarketPlaceDeal privateMarketPlaceDeal;

    @OneToMany(fetch = FetchType.LAZY)
	@JoinColumn(name = "CAMPAIGN_ID", nullable = true)
    @Where(clause = "DELETED <> 1")
    @SQLDelete(sql = "UPDATE CAMPAIGN_AUDIENCE SET DELETED = 1 WHERE CAMPAIGN_ID = ? and ID = ?")
    @SQLDeleteAll (sql = "UPDATE CAMPAIGN_AUDIENCE SET DELETED = 1 WHERE CAMPAIGN_ID = ?")
    private Set<CampaignAudience> campaignAudiences;

    // Careful here. Even tho the relationship is marked as LAZY, OneToOne lazies are not supported by Hibernate.
    // The object will always be hydrated.
    // TODO: change the JoinColumn annotation to nullable=false
    @OneToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="CAMPAIGN_AGENCY_DISCOUNT_ID",nullable=true)
    private CampaignAgencyDiscount currentAgencyDiscount;

    @NotCopied("This accumulates naturally as bids are created")
    @OneToMany(mappedBy="campaign",fetch=FetchType.LAZY)
    private Set<CampaignAgencyDiscount> historicalAgencyDiscounts;
    
    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "CAMPAIGN_ID", nullable = true)
    @Where(clause = "DELETED <> 1")
    @SQLDelete(sql = "UPDATE CAMPAIGN_TRIGGER SET DELETED = 1 WHERE CAMPAIGN_ID = ? and ID = ?")
    @SQLDeleteAll (sql = "UPDATE CAMPAIGN_TRIGGER SET DELETED = 1 WHERE CAMPAIGN_ID = ?")
    private Set<CampaignTrigger> campaignTriggers;
    
    @ElementCollection(fetch=FetchType.LAZY,  targetClass = BiddingStrategy.class)
    @CollectionTable(name = "CAMPAIGN_BIDDING_STRATEGY", joinColumns=@JoinColumn(name="CAMPAIGN_ID",referencedColumnName="ID"))
    @Column(name = "BIDDING_STRATEGY", nullable = false)
    @Enumerated(EnumType.STRING)
    private Set<BiddingStrategy> biddingStrategies;
    
    @OneToOne(fetch=FetchType.EAGER)
    @JoinColumn(name="CURRENCY_EXCHANGE_RATE_ID",nullable=true)
    private CurrencyExchangeRate currencyExchangeRate;
    
    @Column(name="EXCHANGE_RATE",nullable=true)
    private BigDecimal exchangeRate;
    
    @Column(name="EXCHANGE_RATE_ADMIN_CHANGED",nullable=false)
    private boolean exchangeRateAdminChange;

    // MAD-3350 - Variable Bid Price
    @Column(name="MAX_BID_THRESHOLD",nullable=true)
    private BigDecimal maxBidThreshold;

    //========================================================================
    // ***** WARNING *****
    // WHENEVER YOU ADD NEWS FIELDS, YOU NEED TO CONSIDER WHETHER OR NOT THEY
    // ARE INCLUDED IN THE FIELDS THAT GET COPIED WHEN copyFrom IS INVOKED.
    // ***** YOU ALSO NEED TO INCREMENT THE serialVersionUID. *****
    //========================================================================

    {
        this.externalID = UUID.randomUUID().toString();
        this.segments = new LinkedList<Segment>();
        this.status = Status.NEW;
        this.creatives = new ArrayList<Creative>();
        this.historicalBids = new HashSet<CampaignBid>();
        this.dailySpendMap = new HashMap<Integer,BudgetSpend>();
        this.transparentNetworks = new HashSet<TransparentNetwork>();
        this.timePeriods = new HashSet<CampaignTimePeriod>();
        this.timePeriods.add(new CampaignTimePeriod(this, null, null));
        this.disableLanguageMatch = true;
        this.houseAd = false;
        this.throttle = 100;
        this.notificationFlags = new HashSet<CampaignNotificationFlag>();
        this.deviceIdentifierTypes = new TreeSet<DeviceIdentifierType>();
        this.removedPublications = new HashMap<Publication,RemovalInfo>();
        this.watchers = new HashSet<AdfonicUser>();
        this.historicalRMAdServingFees = new HashSet<CampaignRichMediaAdServingFee>();
        this.historicalTDMarginFees = new HashSet<CampaignTradingDeskMargin>();
        this.campaignAudiences = new HashSet<CampaignAudience>();
        this.historicalAgencyDiscounts = new HashSet<CampaignAgencyDiscount>();
        this.campaignTriggers = new HashSet<CampaignTrigger>();
        this.currentBidDeductions = new HashSet<BidDeduction>();
        this.exchangeRateAdminChange = false;
        // MAD-3453 default cap is 'per campaign'
        this.capPerCampaign = true; 
    }

    Campaign() {}

    public Campaign(Advertiser advertiser) {
        this(advertiser, null);
    }

    public Campaign(Advertiser advertiser, String name) {
        this.advertiser = advertiser;
        this.name = name;
    }

    public long getId() { return id; };

    public Advertiser getAdvertiser() { return advertiser; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getExternalID() { return externalID; }
    public void setExternalID(String externalID) { this.externalID = externalID; }

    public String getDescription() { return description; }
    public void setDescription(String description) {
        this.description = description;
    }

    public Date getStartDate() { return startDate; }
    public Date getEndDate() { return endDate; }
    // deliberately no setter for startDate/endDate...they're set dynamically

    public Date getActivationDate() { return activationDate; }
    public Date getDeactivationDate() { return deactivationDate; }
    // deliberately no setter for these either...they're set dynamically

    /** A run of network campaign is one without any defined segments. */
    public boolean isRunOfNetwork() {
        return segments.isEmpty();
    }

    public List<Segment> getSegments() { return segments; }

    public BigDecimal getDailyBudget() { return dailyBudget; }
    public void setDailyBudget(BigDecimal dailyBudget) {
        this.dailyBudget = dailyBudget;
    }

    public boolean isDailyBudgetAlertEnabled() { return dailyBudgetAlertEnabled; }
    public void setDailyBudgetAlertEnabled(boolean dailyBudgetAlertEnabled) {
        this.dailyBudgetAlertEnabled = dailyBudgetAlertEnabled;
    }

    public BigDecimal getOverallBudget() { return overallBudget; }
    public void setOverallBudget(BigDecimal overallBudget) {
        this.overallBudget = overallBudget;
    }

    public boolean isOverallBudgetAlertEnabled() { return overallBudgetAlertEnabled; }
    public void setOverallBudgetAlertEnabled(boolean overallBudgetAlertEnabled) {
        this.overallBudgetAlertEnabled = overallBudgetAlertEnabled;
    }

    public BigDecimal getDailyBudgetWeekday() {
        return dailyBudgetWeekday;
    }

    public void setDailyBudgetWeekday(BigDecimal dailyBudgetWeekday) {
        this.dailyBudgetWeekday = dailyBudgetWeekday;
    }

    public BigDecimal getDailyBudgetWeekend() {
        return dailyBudgetWeekend;
    }

    public void setDailyBudgetWeekend(BigDecimal dailyBudgetWeekend) {
        this.dailyBudgetWeekend = dailyBudgetWeekend;
    }

    public Status getStatus() { return status; }
    public void setStatus(Status status) {
        if (this.status == status) return; // no change
        if (status == Status.ACTIVE) {
            if (activationDate == null) {
                activationDate = new Date();
            }

            // Ensure that reactivation will clear the deactivation
            if (deactivationDate != null) {
                deactivationDate = null;
            }
        }
        if (status == Status.STOPPED || status == Status.COMPLETED) {
            deactivationDate = new Date();
        }
        this.status = status;
    }

    /**
     * A default call to transitionStatus
     * 
     * @param newStatus
     * @return
     */
    public boolean transitionStatus(Status newStatus) {
        return transitionStatus(newStatus, null);
    }    
    
    /**
     * Attempts to change status and returns true if it did.
     * This is business logic for the User only -- the system is free
     * to call setStatus() however it would like.
     * 
     * @param newStatus
     * @param allowedStatuses - a {@link List} of {@link Status} to be allowed to switch between (prior to the main logic).
     * @return
     */
    public boolean transitionStatus(Status newStatus, List<Status> allowedStatuses) {

        if (newStatus == status) return true; // no change

    	if(allowedStatuses!=null && !allowedStatuses.contains(newStatus)) {
    		return false;
    	}        
        
        boolean allowed;
        switch (status) {
            case NEW:
            case NEW_REVIEW:  //  [=> DELETED (delete)]
            	allowed = newStatus == Status.DELETED;
            	break;
            case PENDING: //  [=> DELETED (delete)]  [=> PENDING_PAUSED (pause)]
                allowed = newStatus == Status.DELETED || newStatus == Status.PAUSED;
                if(allowed && newStatus == Status.PAUSED) {
                    newStatus = Status.PENDING_PAUSED;
                }
                break;
            case PENDING_PAUSED: // [=> PENDING (run)]
                allowed = newStatus == Status.ACTIVE;
                if(allowed && newStatus == Status.ACTIVE) {
                    newStatus = Status.PENDING;
                }
                break;
            case ACTIVE: // [=> PAUSED (pause)] [=> STOPPED (stop)] 
                allowed = newStatus == Status.PAUSED || newStatus == Status.STOPPED;
                break;
            case PAUSED: // [=> ACTIVE (reactivate)] [=> STOPPED (stop)] 
                allowed = newStatus == Status.ACTIVE || newStatus == Status.STOPPED;
                break;
            case STOPPED:
            case COMPLETED: // [=> ACTIVE (reactivate)] [=> PAUSED (pause)]
                allowed = newStatus == Status.ACTIVE || newStatus == Status.PAUSED;
                break;
            default:
                allowed = false; // All other changes not allowed
                break;
        }
        if (allowed) {
            setStatus(newStatus);
        }
        return allowed;
    }

    public List<Creative> getCreatives() { return creatives; }

    /**
     * Factory method to create new Creatives. Creative still needs
     * to be attached to this campaign at a later stage using
     * addCreative()
     */
    public Creative newCreative(Segment segment, Format format) {
        Creative c = new Creative(segment, format);
        c.setLanguage(defaultLanguage);
        return c;
    }

    public Creative makeNewCreative(Segment segment, Format format) {
        Creative c = new Creative(this, segment, format);
        c.setLanguage(defaultLanguage);
        //creatives.add(c);
        return c;
    }

    public void addCreative(Creative creative) {
        creative.initializeCampaign(this);
        creatives.add(creative);
    }

    public CampaignBid getCurrentBid() { return currentBid; }

    public CampaignBid createNewBid(BidType bidType, BigDecimal amount, Date date, BidModelType modelType) {
        // Archive previous bid
        //if (currentBid != null) {
        //    currentBid.setEndDate(date);
        //}
        // Create the new one
        currentBid = new CampaignBid(this, bidType, amount, modelType);
        currentBid.setStartDate(date);
        historicalBids.add(currentBid);
        return currentBid;
    }

    public Set<BidDeduction> getCurrentBidDeductions() {
		return currentBidDeductions;
	}

	public void setCurrentBidDeductions(Set<BidDeduction> currentBidDeductions) {
		this.currentBidDeductions = currentBidDeductions;
	}

	public Set<CampaignBid> getHistoricalBids() { return historicalBids; }

    public CampaignBid getBidForDate(Date date) {
        for (CampaignBid cb : historicalBids) {
            if (!date.before(cb.getStartDate())) {
                Date cbEnd = cb.getEndDate();
                if ((cbEnd == null) || date.before(cbEnd)) {
                    return cb;
                }
            }
        }
        return null; // no bid for that time
    }

    public Language getDefaultLanguage() { return defaultLanguage; }
    public void setDefaultLanguage(Language defaultLanguage) {
        this.defaultLanguage = defaultLanguage;
    }

    public boolean getDisableLanguageMatch() {
        return disableLanguageMatch;
    }

    public void setDisableLanguageMatch(boolean disableLanguageMatch) {
        this.disableLanguageMatch = disableLanguageMatch;
    }

    public boolean isSummaryDisplayDisabled() {
        return summaryDisplayDisabled;
    }

    public void setSummaryDisplayDisabled(boolean summaryDisplayDisabled) {
        this.summaryDisplayDisabled = summaryDisplayDisabled;
    }

    // Status helpers

    // new is reserved in EL
    public boolean isStatusNew() { return status == Status.NEW; }

    public boolean isApproved() {
        return (status.ordinal() >= Status.ACTIVE.ordinal());
    }

    public boolean isEditable() {
        return status == Status.NEW || status == Status.NEW_REVIEW;
    }

    public BigDecimal getOverallSpend() {
        return overallSpend == null ? null : overallSpend.getAmount();
    }
    public CampaignOverallSpend getOverallSpendNoSeriouslyGetOverallSpend() {
        return overallSpend;
    }

    public Map<Integer,BudgetSpend> getDailySpendMap() { return dailySpendMap; }

    public String getReference() { return reference; }
    public void setReference(String reference) {
        this.reference = reference;
    }

    public String getOpportunity() {
		return opportunity;
	}

	public void setOpportunity(String opportunity) {
		this.opportunity = opportunity;
	}

	public double getBoostFactor() { return boostFactor; }
    public void setBoostFactor(double boostFactor) {
        this.boostFactor = boostFactor;
    }


    public Set<TransparentNetwork> getTransparentNetworks() { return transparentNetworks; }

    public boolean isTransparent() {
        return !transparentNetworks.isEmpty();
    }

    public Integer getCapImpressions() { return capImpressions; }
    public void setCapImpressions(Integer capImpressions) {
        this.capImpressions = capImpressions;
    }
    public Integer getCapPeriodSeconds() { return capPeriodSeconds; }
    public void setCapPeriodSeconds(Integer capPeriodSeconds) {
        this.capPeriodSeconds = capPeriodSeconds;
    }
    
    public boolean isCapPerCampaign() {
        return capPerCampaign;
    }

    public void setCapPerCampaign(boolean capPerCampaign) {
        this.capPerCampaign = capPerCampaign;
    }

    public void clearTimePeriods() {
        timePeriods.clear();
        startDate = null;
        endDate = null;
    }


    /** Notice that this is static...this has nothing to do with an instance
        of Campaign.  It's there so you can fully validate a full set of
        "replacement" CampaignTimePeriods before blowing away the old set
        (i.e. pm.deletePersistentAll(campaign.getTimePeriods())) and calling
        newTimePeriods with set that passed this validation method.
        Note: we enforce that you pass a Set instead of Collection so that
        de-duping has already been taken care of.
    */
    public static void validateTimePeriods(Set<CampaignTimePeriod> timePeriods) throws Exception {
        Set<CampaignTimePeriod> validated = new HashSet<CampaignTimePeriod>();
        for (CampaignTimePeriod timePeriod : timePeriods) {
            if (timePeriod.getStartDate() != null &&
                     timePeriod.getEndDate() != null &&
                     !timePeriod.getStartDate().before(timePeriod.getEndDate())) {
                throw new Exception("Invalid date range...start date must be prior to end date: " + timePeriod);
            }

            // Make sure it doesn't overlap any of the already validated ones
            for (CampaignTimePeriod existing : validated) {
                if (timePeriod.overlaps(existing)) {
                    throw new Exception("Overlapping time periods: " + existing + " and " + timePeriod);
                }
            }

            // It's good
            validated.add(timePeriod);
        }
    }

    public Set<CampaignTimePeriod> getTimePeriods() { return timePeriods; }

    @SuppressWarnings("unchecked")
    public List<CampaignTimePeriod> getSortedTimePeriods() {
        List<CampaignTimePeriod> sorted = new ArrayList<CampaignTimePeriod>(timePeriods.size()); // initialCapacity for ++performance
        sorted.addAll(timePeriods);
        Collections.sort(sorted);
        return sorted;
    }

    /** Are we active not only status-wise, but also scheduling-wise? */
    public boolean isCurrentlyActive() {
        return status == Status.ACTIVE &&
            (timePeriods.isEmpty() || (getCurrentTimePeriod() != null));
    }

    private synchronized CampaignTimePeriod determineCurrentTimePeriod() {
        if (currentTimePeriod != null) {
            if (currentTimePeriod == GAP) {
                // We think we're in a gap...make sure we still should be
                if (nextTimePeriod == null || !nextTimePeriod.isCurrent()) {
                    return currentTimePeriod; // Still in the gap
                }
            }
            else if (currentTimePeriod.isCurrent()) {
                // Already good to go.  We probably got called by a thread that
                // was waiting for the synchronized call while another thread
                // already took care of things.
                return currentTimePeriod;
            }
        }

        // Since currentTimePeriod should never be null once "determined", we
        // always initialize it to GAP.  That way, if we don't find any time
        // period that is current, or future, we'll at least be able to denote
        // such a case.
        currentTimePeriod = GAP;

        // By default we'll assume there's no upcoming time period
        nextTimePeriod = null;

        // Iterate through a sorted list of all of the time periods
        List<CampaignTimePeriod> sorted = getSortedTimePeriods();
        Iterator<CampaignTimePeriod> iter = sorted.iterator();
        while (iter.hasNext()) {
            CampaignTimePeriod timePeriod = iter.next();
            if (timePeriod.isCurrent()) {
                // Found the one that is now current
                currentTimePeriod = timePeriod;
                // Keep track of the one coming up next
                nextTimePeriod = iter.hasNext() ? iter.next() : null;
                break;
            }
            else if (timePeriod.isFuture()) {
                // We haven't found the current one, but we hit one that's
                // in the future...this means we're currently in a "gap".
                currentTimePeriod = GAP;
                // Keep track of this time period being up next
                nextTimePeriod = timePeriod;
                break;
            }
        }
        return currentTimePeriod;
    }

    public CampaignTimePeriod getCurrentTimePeriod() {
        if (currentTimePeriod == null) {
            // This must be the first time this method was called
            synchronized (this) {
                if (currentTimePeriod == null) {
                    return mindTheGap(determineCurrentTimePeriod());
                }
            }
        }

        // Check to see if we're in the middle of a "gap"
        if (currentTimePeriod == GAP) {
            // See if the next time period applies yet
            if (nextTimePeriod != null && nextTimePeriod.isCurrent()) {
                // Yup, time to advance to the next time period
                return mindTheGap(determineCurrentTimePeriod());
            }

            // Nope, we're still in the gap
            return null;
        }
        // Make sure the current time period is still valid
        else if (currentTimePeriod.isCurrent()) {
            return currentTimePeriod;
        }
        else {
            // Time to advance to the next time period, if available
            return mindTheGap(determineCurrentTimePeriod());
        }
    }

    private static CampaignTimePeriod mindTheGap(CampaignTimePeriod ctp) {
        return ctp == GAP ? null : ctp;
    }

    public boolean isInstallTrackingEnabled() {
        return installTrackingEnabled;
    }

    public void setInstallTrackingEnabled(boolean installTrackingEnabled) {
        this.installTrackingEnabled = installTrackingEnabled;
    }

    public boolean isInstallTrackingVerified() {
        return installTrackingVerified;
    }

    public void setInstallTrackingVerified(boolean installTrackingVerified) {
        this.installTrackingVerified = installTrackingVerified;
    }

    public String getApplicationID() {
        return applicationID;
    }

    public void setApplicationID(String applicationID) {
        this.applicationID = applicationID;
    }

    public boolean isConversionTrackingEnabled() {
        return conversionTrackingEnabled;
    }

    public void setConversionTrackingEnabled(boolean conversionTrackingEnabled) {
        this.conversionTrackingEnabled = conversionTrackingEnabled;
    }

    public boolean isConversionTrackingVerified() {
        return conversionTrackingVerified;
    }

    public void setConversionTrackingVerified(boolean conversionTrackingVerified) {
        this.conversionTrackingVerified = conversionTrackingVerified;
    }

    public boolean isPriceOverridden() {
        return this.priceOverridden;
    }

    public void setPriceOverridden(boolean priceOverridden) {
        this.priceOverridden = priceOverridden;
    }

    public boolean isHouseAd() {
        return houseAd;
    }
    public void setHouseAd(boolean houseAd) {
        this.houseAd = houseAd;
    }

    public int getThrottle() {
        return throttle;
    }
    public void setThrottle(int throttle) {
        this.throttle = throttle;
    }

    public Set<CampaignNotificationFlag> getNotificationFlags() {
        return notificationFlags;
    }

    public BigDecimal getTargetCPA() {
        return targetCPA;
    }

    public void setTargetCPA(BigDecimal targetCPA) {
        this.targetCPA = targetCPA;
    }

    public BigDecimal getOptimizationMaxRemoval() {
        return optimizationMaxRemoval;
    }

    public void setOptimizationMaxRemoval(BigDecimal optimizationMaxRemoval) {
        this.optimizationMaxRemoval = optimizationMaxRemoval;
    }

    public BigDecimal getOptimizationMultiplier() {
        return optimizationMultiplier;
    }

    public void setOptimizationMultiplier(BigDecimal optimizationMultiplier) {
        this.optimizationMultiplier = optimizationMultiplier;
    }

    public boolean isInstallTrackingAdXEnabled() {
        return installTrackingAdXEnabled;
    }

    public void setInstallTrackingAdXEnabled(boolean installTrackingAdXEnabled) {
        this.installTrackingAdXEnabled = installTrackingAdXEnabled;
    }

    public String getAdvertiserDomain() {
        return advertiserDomain;
    }
    public void setAdvertiserDomain(String advertiserDomain) {
        this.advertiserDomain = advertiserDomain;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public Set<DeviceIdentifierType> getDeviceIdentifierTypes() {
        return deviceIdentifierTypes;
    }

    public Map<Publication,RemovalInfo> getRemovedPublications() {
        return removedPublications;
    }

    /**
     * Has the given publication been removed from this creative?
     * @return true if the publication has been removed, or false if the
     * publication has not been removed or has been marked UNREMOVED
     */
    public boolean isPublicationRemoved(Publication publication) {
        RemovalInfo removalInfo = removedPublications.get(publication);
        // RemovalInfo can also represent "unremoved" -- so check for that
        return removalInfo != null && !removalInfo.isUnremoval();
    }

    /**
     * Has a publication (that probably was previously removed) been unremoved?
     * @return true if the publication has been marked as UNREMOVED
     */
    public boolean isPublicationUnremoved(Publication publication) {
        RemovalInfo removalInfo = removedPublications.get(publication);
        return removalInfo != null && removalInfo.isUnremoval();
    }

    public RemovalInfo getPublicationRemovalInfo(Publication publication) {
        return removedPublications.get(publication);
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public void setCurrentTimePeriod(CampaignTimePeriod currentTimePeriod) {
        this.currentTimePeriod = currentTimePeriod;
    }

    public void setNextTimePeriod(CampaignTimePeriod nextTimePeriod) {
        this.nextTimePeriod = nextTimePeriod;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public Set<AdfonicUser> getWatchers() {
        return watchers;
    }

	public CampaignTargetCTR getTargetCTR() {
		return targetCTR;
	}

	public void setTargetCTR(CampaignTargetCTR targetCTR) {
		this.targetCTR = targetCTR;
	}

	public CampaignTargetCVR getTargetCVR() {
		return targetCVR;
	}

	public void setTargetCVR(CampaignTargetCVR targetCVR) {
		this.targetCVR = targetCVR;
	}

    public boolean isEvenDistributionOverallBudget() {
        return evenDistributionOverallBudget;
    }

    public boolean isEvenDistributionDailyBudget() {
        return evenDistributionDailyBudget;
    }

    public void setEvenDistributionOverallBudget(
            boolean evenDistributionOverallBudget) {
        this.evenDistributionOverallBudget = evenDistributionOverallBudget;
    }

    public void setEvenDistributionDailyBudget(boolean evenDistributionDailyBudget) {
        this.evenDistributionDailyBudget = evenDistributionDailyBudget;
    }

	public CampaignRichMediaAdServingFee getCurrentRichMediaAdServingFee() {
		return currentRichMediaAdServingFee;
	}
	
	public CampaignRichMediaAdServingFee createNewRichMediaAdServingFee(BigDecimal amount, Date date) {
	    if(amount==null || amount.equals(new BigDecimal(0))){
	        currentRichMediaAdServingFee = null;
        }
	    // Archive previous bid
//        if (currentRichMediaAdServingFee != null) {
//            currentRichMediaAdServingFee.setEndDate(date);
//        }
        // Create the new one
	    currentRichMediaAdServingFee = new CampaignRichMediaAdServingFee(this,amount);
	    currentRichMediaAdServingFee.setStartDate(date);
	    historicalRMAdServingFees.add(currentRichMediaAdServingFee);
        return currentRichMediaAdServingFee;
    }

	public CampaignTradingDeskMargin getCurrentTradingDeskMargin() {
		return currentTradingDeskMargin;
	}
	
	public CampaignTradingDeskMargin createNewTradingDeskMargin(BigDecimal amount, Date date) {
	    if(amount==null || amount.equals(new BigDecimal(0))){
	        currentTradingDeskMargin = null;
	    }
        // Archive previous bid
//        if (currentTradingDeskMargin != null) {
//            currentTradingDeskMargin.setEndDate(date);
//        }
        // Create the new one
        currentTradingDeskMargin = new CampaignTradingDeskMargin(this,amount);
        currentTradingDeskMargin.setStartDate(date);
        historicalTDMarginFees.add(currentTradingDeskMargin);
        return currentTradingDeskMargin;
    }

	public Set<CampaignRichMediaAdServingFee> getHistoricalRMAdServingFees() {
        return historicalRMAdServingFees;
    }
	
	public CampaignRichMediaAdServingFee getRichMediaAdServingFeeForDate(Date date) {
        for (CampaignRichMediaAdServingFee cb : historicalRMAdServingFees) {
            if (!date.before(cb.getStartDate())) {
                Date cbEnd = cb.getEndDate();
                if ((cbEnd == null) || date.before(cbEnd)) {
                    return cb;
                }
            }
        }
        return null; // no bid for that time
    }

    public Set<CampaignTradingDeskMargin> getHistoricalTDMarginFees() {
        return historicalTDMarginFees;
    }

    public CampaignTradingDeskMargin getTradingDeskMarginForDate(Date date) {
        for (CampaignTradingDeskMargin cb : historicalTDMarginFees) {
            if (!date.before(cb.getStartDate())) {
                Date cbEnd = cb.getEndDate();
                if ((cbEnd == null) || date.before(cbEnd)) {
                    return cb;
                }
            }
        }
        return null; // no bid for that time
    }
    
    public CampaignDataFee getCurrentDataFee() {
		return currentDataFee;
    }
    
    public void setCurrentDataFee(CampaignDataFee currentDataFee) {
		this.currentDataFee = currentDataFee;
	}

	public Set<CampaignDataFee> getHistoricalDataFees() {
        return historicalDataFees;
    }

	public InventoryTargetingType getInventoryTargetingType() {
		return inventoryTargetingType;
	}

	public void setInventoryTargetingType(InventoryTargetingType inventoryTargetingType) {
		this.inventoryTargetingType = inventoryTargetingType;
	}

	public PublicationList getPublicationList() {
		return publicationList;
	}

	public void setPublicationList(PublicationList publicationList) {
		this.publicationList = publicationList;
	}

	public BigDecimal getDailyBudgetImpressions() {
		return dailyBudgetImpressions;
	}

	public void setDailyBudgetImpressions(BigDecimal dailyBudgetImpressions) {
		this.dailyBudgetImpressions = dailyBudgetImpressions;
	}

	public BigDecimal getOverallBudgetImpressions() {
		return overallBudgetImpressions;
	}

	public void setOverallBudgetImpressions(BigDecimal overallBudgetImpressions) {
		this.overallBudgetImpressions = overallBudgetImpressions;
	}

	public BigDecimal getDailyBudgetClicks() {
		return dailyBudgetClicks;
	}

	public void setDailyBudgetClicks(BigDecimal dailyBudgetClicks) {
		this.dailyBudgetClicks = dailyBudgetClicks;
	}

	public BigDecimal getOverallBudgetClicks() {
		return overallBudgetClicks;
	}

	public void setOverallBudgetClicks(BigDecimal overallBudgetClicks) {
		this.overallBudgetClicks = overallBudgetClicks;
	}

	public BigDecimal getDailyBudgetConversions() {
		return dailyBudgetConversions;
	}

	public void setDailyBudgetConversions(BigDecimal dailyBudgetConversions) {
		this.dailyBudgetConversions = dailyBudgetConversions;
	}

	public BigDecimal getOverallBudgetConversions() {
		return overallBudgetConversions;
	}

	public void setOverallBudgetConversions(BigDecimal overallBudgetConversions) {
		this.overallBudgetConversions = overallBudgetConversions;
	}

	public BudgetType inferBudgetType() {
		if(dailyBudgetImpressions != null || overallBudgetImpressions != null) {
			return BudgetType.IMPRESSIONS;
		}
		if(dailyBudgetClicks != null || overallBudgetClicks != null) {
			return BudgetType.CLICKS;
		}
		return BudgetType.MONETARY;
	}
	
	public BudgetType getBudgetType() {
		if(budgetType == null) {
			return inferBudgetType();
		}
		return budgetType;
	}

	public void setBudgetType(BudgetType budgetType) {
		this.budgetType = budgetType;
	}
    
	public void nullifyMonetaryBudgetFields() {
		setDailyBudget(null);
		setDailyBudgetWeekday(null);
		setDailyBudgetWeekend(null);
		setOverallBudget(null);
	}
	
	public void nullifyImpressionsBudgetFields() {
		setDailyBudgetImpressions(null);
		setOverallBudgetImpressions(null);
	}
	
	public void nullifyClicksBudgetFields() {
		setDailyBudgetClicks(null);
		setOverallBudgetClicks(null);
	}

	public PrivateMarketPlaceDeal getPrivateMarketPlaceDeal() {
		return privateMarketPlaceDeal;
	}

	public void setPrivateMarketPlaceDeal(
			PrivateMarketPlaceDeal privateMarketPlaceDeal) {
		this.privateMarketPlaceDeal = privateMarketPlaceDeal;
	}
    
	public String getPrivateMarketPlaceDealAsString(){
	    if(this.privateMarketPlaceDeal!=null){
	        return Long.toString(this.privateMarketPlaceDeal.getId());
	    }
	    return "";
	}

    public Set<CampaignAudience> getCampaignAudiences() {
        return campaignAudiences;
    }

    public void setCampaignAudiences(Set<CampaignAudience> campaignAudiences) {
        this.campaignAudiences = campaignAudiences;
    }

	public CampaignAgencyDiscount getCurrentAgencyDiscount() {
		return currentAgencyDiscount;
	}

	public BigDecimal getCurrentAgencyDiscountAmount() {
		if(currentAgencyDiscount == null) {
			return null;
		} else {
			return currentAgencyDiscount.getDiscount();
		}
	}

	public void setCurrentAgencyDiscount(
			CampaignAgencyDiscount currentAgencyDiscount) {
		this.currentAgencyDiscount = currentAgencyDiscount;
	}

	public Set<CampaignAgencyDiscount> getHistoricalAgencyDiscounts() {
		return historicalAgencyDiscounts;
	}

	public void setHistoricalAgencyDiscounts(
			Set<CampaignAgencyDiscount> historicalAgencyDiscounts) {
		this.historicalAgencyDiscounts = historicalAgencyDiscounts;
	}
    
    public Set<CampaignTrigger> getCampaignTriggers() {
        return campaignTriggers;
    }
    
    public void setCampaignTriggers(Set<CampaignTrigger> campaignTriggers) {
        this.campaignTriggers = campaignTriggers;
    }

    public Set<BiddingStrategy> getBiddingStrategies() {
        return biddingStrategies;
    }

    public void setBiddingStrategies(Set<BiddingStrategy> biddingStrategies) {
        this.biddingStrategies = biddingStrategies;
    }

    public CurrencyExchangeRate getCurrencyExchangeRate() {
        return currencyExchangeRate;
    }

    public void setCurrencyExchangeRate(CurrencyExchangeRate currencyExchangeRate) {
        this.currencyExchangeRate = currencyExchangeRate;
    }

    public BigDecimal getExchangeRate() {
        return exchangeRate;
    }

    public void setExchangeRate(BigDecimal exchangeRate) {
        this.exchangeRate = exchangeRate;
    }

    public boolean isExchangeRateAdminChange() {
        return exchangeRateAdminChange;
    }

    public void setExchangeRateAdminChange(boolean exchangeRateAdminChange) {
        this.exchangeRateAdminChange = exchangeRateAdminChange;
    }

    public CampaignAgencyDiscount createNewAgencyDiscount(BigDecimal amount, Date date) {
        currentAgencyDiscount = new CampaignAgencyDiscount(this, amount);
        currentAgencyDiscount.setStartDate(date);
        historicalAgencyDiscounts.add(currentAgencyDiscount);
        return currentAgencyDiscount;
    }
    
    public boolean isMediaCostOptimisationEnabled(){
        return hasBiddingStrategy(BiddingStrategy.MEDIA_COST_OPTIMISATION);
    }
    
    public boolean isAverageMaximumBidEnabled(){
        return hasBiddingStrategy(BiddingStrategy.AVERAGE_MAXIMUM_BID);
    }
    
    private boolean hasBiddingStrategy(BiddingStrategy biddingStrategy) {
    	Boolean result = false;
        if (this.biddingStrategies!=null){
            result = this.biddingStrategies.contains(biddingStrategy);
        }
        return result;
    }
    
    // For AuditLog purposes
    public CapPeriodSecondsEnum getCapPeriodSecondsHumanReadable(){
    	CapPeriodSecondsEnum result = null;
    	if (this.capPeriodSeconds!=null){
	    	switch (this.capPeriodSeconds) {
	    	    case 60*60:
	    	        result = CapPeriodSecondsEnum.HOUR;
	    	        break;
				case 60*60*24:
					result = CapPeriodSecondsEnum.DAY;
					break;
				case 60*60*24*7:
					result = CapPeriodSecondsEnum.WEEK;
					break;
				default:
					result = CapPeriodSecondsEnum.MONTH;
					break;
			}
    	}
    	return result;
    }
    
    // For AuditLog purposes
    public String getTimePeriodsHumanReadable(){
    	String result = null;
    	if (this.timePeriods!=null){
    		StringBuilder sb = new StringBuilder();
    		Date dStartDate = null;
    		Date dEndDate = null;
    		SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
    		CampaignTimePeriod[] timePeriodsArray = getSortedTimePeriods().toArray(new CampaignTimePeriod[timePeriods.size()]);
    		for (int i=0; i<timePeriods.size();i++){
    			if (i>0){
    				sb.append(" and ");
    			}
    			CampaignTimePeriod timePeriod = timePeriodsArray[i];
    			dStartDate = timePeriod.getStartDate();
    	    	if (dStartDate==null){
    	    		sb.append("'ASAP'");
    	    	}else{
    	    		sb.append(sdf.format(dStartDate));
    	    	}
    			sb.append(" to ");
    			dEndDate = timePeriod.getEndDate();
    			if (dEndDate==null){
    				sb.append("'No end Date'");
    			}else{
    				sb.append(sdf.format(dEndDate));
    			}
    		}
    		result = sb.toString();
    	}
    	return result;
    }
    
    public String getCampaignTriggersHumanReadable(){
        String result = null;
        if (this.campaignTriggers!=null){
            StringBuilder sb = new StringBuilder();
            CampaignTrigger[] campaignTriggers = this.getCampaignTriggers().toArray(new CampaignTrigger[this.campaignTriggers.size()]);
            for (int i=0; i<campaignTriggers.length;i++){
                if (i>0){
                    sb.append(" and ");
                }
                sb.append(campaignTriggers[i].getPluginVendor().getName());
                sb.append(" [").append(campaignTriggers[i].getPluginType().name()).append("]");
            }
            result = sb.toString();
        }
        return result;
    }

    public BigDecimal getMaxBidThreshold() {
        return maxBidThreshold;
    }

    public void setMaxBidThreshold(BigDecimal maxBidThreshold) {
        this.maxBidThreshold = maxBidThreshold;
    }
}
