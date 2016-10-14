package com.adfonic.domain;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

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
import javax.persistence.MapKeyEnumerated;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.SecondaryTable;
import javax.persistence.SecondaryTables;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.apache.commons.lang.BooleanUtils;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import com.adfonic.util.AgeRangeTargetingLogic;

/**
 * A publication is the container for ad spaces.  It could be
 * a mobile site, an application, or some other conceptual grouping.
 * The ad spaces are in a list (rather than a set) so that they
 * can be ordered by the publisher for display purposes.
 */
@Entity
@Table(name="PUBLICATION")
@SecondaryTables({
    @SecondaryTable(name ="PUBLICATION_SAMPLINGRATE", pkJoinColumns = @PrimaryKeyJoinColumn(name = "PUBLICATION_ID", referencedColumnName = "ID")),
    @SecondaryTable(name = "PUBLICATION_PROVIDED_INFO", pkJoinColumns = @PrimaryKeyJoinColumn(name = "PUBLICATION_ID", referencedColumnName = "ID"))
})
public class Publication extends BusinessKey implements Named, HasExternalID {
    private static final long serialVersionUID = 16L;

    public enum Status {
        NEW, NEW_REVIEW, PENDING, REJECTED, ACTIVE, PAUSED, STOPPED;
    }

    public enum AdOpsStatus { HIGHER_APPROVAL_REQUIRED, MORE_INFO_REQUIRED }
    
    public enum PublicationAttributeKey {
    	SOFT_FLOOR;
    }
    
    public enum PublicationSafetyLevel{
    	UN_CATEGORISED, OTHER, TRUSTED, BRAND_SAFETY;
    }

    @Id @GeneratedValue @Column(name="ID")
    private long id;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="PUBLISHER_ID",nullable=false)
    private Publisher publisher;
    @Column(name="NAME",length=255,nullable=false)
    private String name;

    @OneToMany(fetch=FetchType.LAZY)
    //@OrderColumn(name="PUBLICATION_ORDER",nullable=false,insertable=true,updatable=true)
    @OrderBy("id") 
    @JoinColumn(name="PUBLICATION_ID", nullable=true)
    private List<AdSpace> adSpaces;

    @ManyToOne(fetch=FetchType.EAGER)
    @JoinColumn(name="PUBLICATION_TYPE_ID",nullable=false)
    private PublicationType publicationType;
    @Column(name="URL_STRING",length=255,nullable=true)
    private String urlString; // optional
    @Column(name="STATUS",length=32,nullable=false)
    @Enumerated(EnumType.STRING)
    private Status status;
    @Column(name="AUTO_APPROVAL",nullable=false)
    private boolean autoApproval;

    /** The set of categories for ads that should not be displayed on this publication (opt out). */
    @ManyToMany(fetch=FetchType.LAZY)
    @JoinTable(name="PUBLICATION_EXCLUDED_CATEGORY",joinColumns=@JoinColumn(name="PUBLICATION_ID",referencedColumnName="ID"),inverseJoinColumns=@JoinColumn(name="CATEGORY_ID",referencedColumnName="ID"))
    private Set<Category> excludedCategories;
    @ManyToMany(fetch=FetchType.LAZY)
    @JoinTable(name="PUBLICATION_APPROVED_CREATIVE",joinColumns=@JoinColumn(name="PUBLICATION_ID",referencedColumnName="ID"),inverseJoinColumns=@JoinColumn(name="CREATIVE_ID",referencedColumnName="ID"))
    private Set<Creative> approvedCreatives;
    @ManyToMany(fetch=FetchType.LAZY)
    @JoinTable(name="PUBLICATION_DENIED_CREATIVE",joinColumns=@JoinColumn(name="PUBLICATION_ID",referencedColumnName="ID"),inverseJoinColumns=@JoinColumn(name="CREATIVE_ID",referencedColumnName="ID"))
    private Set<Creative> deniedCreatives;

    @ManyToMany(fetch=FetchType.LAZY)
    @JoinTable(name="PUBLICATION_EXTENDED_CREATIVE_TYPE", joinColumns=@JoinColumn(name="PUBLICATION_ID", referencedColumnName="ID"), inverseJoinColumns=@JoinColumn(name="EXTENDED_CREATIVE_TYPE_ID", referencedColumnName="ID"))
    private Set <ExtendedCreativeType> thirdPartyTagVendorWhitelist;
    
    @Column(name="DESCRIPTION",nullable=true)
    @Lob
    private String description; // e.g. a URL
    @ManyToMany(fetch=FetchType.LAZY)
    @JoinTable(name="PUBLICATION_LANGUAGE",joinColumns=@JoinColumn(name="PUBLICATION_ID",referencedColumnName="ID"),inverseJoinColumns=@JoinColumn(name="LANGUAGE_ID",referencedColumnName="ID"))
    private Set<Language> languages;
    @Column(name="MATCH_USER_LANGUAGE",nullable=false)
    private boolean matchUserLanguage;
    @Column(name="GENDER_MIX",nullable=true)
    private BigDecimal genderMix; // null = don't care, 1.0 = all male
    @Column(name="MIN_AGE",nullable=false)
    private int minAge;
    @Column(name="MAX_AGE",nullable=false)
    private int maxAge;
    @Column(name="EXTERNAL_ID",length=255,nullable=false)
    private String externalID;
    @Column(name="APPROVED_DATE",nullable=true)
    @Temporal(TemporalType.TIMESTAMP)
    private Date approvedDate;
    @Column(name="BACKFILL_ENABLED",nullable=false)
    private boolean backfillEnabled;
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="TRANSPARENT_NETWORK_ID",nullable=true)
    private TransparentNetwork transparentNetwork;
    @Column(name="INSTALL_TRACKING_DISABLED",nullable=false)
    private boolean installTrackingDisabled;
    @Column(name="REFERENCE",length=255,nullable=true)
    private String reference;
    @Column(name="TRACKING_IDENTIFIER_TYPE",nullable=true)
    @Enumerated(EnumType.STRING)
    private TrackingIdentifierType trackingIdentifierType;
    @Column(name="AD_REQUEST_TIMEOUT",nullable=true)
    private Long adRequestTimeout;
    @Column(name="SUMMARY_DISPLAY_DISABLED",nullable=false)
    private boolean summaryDisplayDisabled; // true == owner has marked hidden on tools
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="DEFAULT_INTEGRATION_TYPE_ID",nullable=true)
    private IntegrationType defaultIntegrationType;
    @Column(name="RTB_ID",length=255,nullable=true)
    private String rtbId;
    @OneToMany(fetch=FetchType.LAZY)
    @JoinTable(name="PUBLICATION_RATE_CARD_MAP",joinColumns=@JoinColumn(name="PUBLICATION_ID",referencedColumnName="ID"),inverseJoinColumns=@JoinColumn(name="RATE_CARD_ID",referencedColumnName="ID"))
    @MapKeyColumn(name="BID_TYPE")
    @MapKeyClass(BidType.class)
    @MapKeyEnumerated(EnumType.STRING)
    private Map<BidType,RateCard> rateCardMap;
    @Column(name="INCENTIVIZED",nullable=false)
    private boolean incentivized;

    /** Multiple categories associated with the publication, i.e. for RTB */
    @ManyToMany(fetch=FetchType.LAZY)
    @JoinTable(name="PUBLICATION_STATED_CATEGORY",joinColumns=@JoinColumn(name="PUBLICATION_ID",referencedColumnName="ID"),inverseJoinColumns=@JoinColumn(name="CATEGORY_ID",referencedColumnName="ID"))
    private Set<Category> statedCategories;

    @Column(name="AD_OPS_STATUS",length=32,nullable=true)
    @Enumerated(EnumType.STRING)
    private AdOpsStatus adOpsStatus;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="CATEGORY_ID",nullable=true)
    private Category category;

    // publisher is happy to disclose their identify
    @Column(name="DISCLOSED",nullable=false)
    private boolean disclosed;
    
    @Column(name="CREATION_TIME",nullable=false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date creationTime;
    
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="ECPM_TARGET_RATE_CARD_ID",nullable=true)
    private RateCard ecpmTargetRateCard;

    @Column(name="SUBMISSION_TIME",nullable=true)
    @Temporal(TemporalType.TIMESTAMP)
    private Date submissionTime;
    
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="ASSIGNED_TO_ADFONIC_USER_ID",nullable=true)
    private AdfonicUser assignedTo;

    @OneToMany(mappedBy="publication",fetch=FetchType.LAZY)
    @OrderBy("id")
    private List<PublicationHistory> history;
    
    @ManyToMany(fetch=FetchType.LAZY)
    @JoinTable(name="PUBLICATION_WATCHER",joinColumns=@JoinColumn(name="PUBLICATION_ID",referencedColumnName="ID"),inverseJoinColumns=@JoinColumn(name="ADFONIC_USER_ID",referencedColumnName="ID"))
    private Set<AdfonicUser> watchers;
    
    // publisher is happy to disclose their identify
    @Column(name="FRIENDLY_NAME",length=255,nullable=true)
    private String friendlyName;
    
    @ElementCollection(fetch=FetchType.LAZY,targetClass=BidType.class)
    @CollectionTable(name="PUBLICATION_BLOCKED_BID_TYPE",joinColumns=@JoinColumn(name="PUBLICATION_ID",referencedColumnName="ID"))
    @Column(name="BID_TYPE",nullable=false)
    @Enumerated(EnumType.STRING)
    private Set<BidType> blockedBidTypes;
    
    @ElementCollection(fetch=FetchType.LAZY,targetClass=String.class)
    @CollectionTable(name="PUBLICATION_ATTRIBUTES_MAP",joinColumns=@JoinColumn(name="PUBLICATION_ID",referencedColumnName="ID"))
    @MapKeyColumn(name="NAME",length=64,nullable=false)
    @MapKeyClass(PublicationAttributeKey.class)
    @MapKeyEnumerated(EnumType.STRING)
    @Column(name="VALUE",length=128,nullable=false)
    @Fetch(FetchMode.SELECT)
    private Map<PublicationAttributeKey, String> publicationAttributes;

    @Column(name="SAFETY_LEVEL",nullable=false)
    @Enumerated(EnumType.STRING)
    private PublicationSafetyLevel safetyLevel;
    
    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "PUBLICATION_ID", nullable = true)
    private Set<PublicationProvidedInfo> publicationProvidedInfos;
    
    {
        this.adSpaces = new LinkedList<AdSpace>();
        this.autoApproval = true;
        this.excludedCategories = new HashSet<Category>();
        this.approvedCreatives = new HashSet<Creative>();
        this.deniedCreatives = new HashSet<Creative>();
        this.status = Status.NEW;
        this.languages = new HashSet<Language>();
        this.minAge = AgeRangeTargetingLogic.MIN_AGE;
        this.maxAge = AgeRangeTargetingLogic.MAX_AGE;
        this.genderMix = Segment.DEFAULT_GENDER_MIX;
        this.externalID = UUID.randomUUID().toString();
        this.backfillEnabled = true;
        this.rateCardMap = new HashMap<BidType,RateCard>();
        this.incentivized = false;
        this.statedCategories = new HashSet<Category>();
        this.disclosed = false;
        this.creationTime = new Date();
        this.history = new ArrayList<PublicationHistory>();
        this.watchers = new HashSet<AdfonicUser>();
        this.blockedBidTypes = new HashSet<BidType>();
        this.publicationAttributes = new HashMap<PublicationAttributeKey, String>();
        this.safetyLevel = PublicationSafetyLevel.UN_CATEGORISED;
        this.publicationProvidedInfos = new HashSet<>();
    }

    @Column(table="PUBLICATION_SAMPLINGRATE", name="SAMPLING_RATE")
    private Integer samplingRate;

    Publication() {}

    public Publication(Publisher publisher) {
        this.publisher = publisher;
    }

    public long getId() { return id; };

    public Publisher getPublisher() {
        return publisher;
    }

    public PublicationType getPublicationType() { return publicationType; }
    public void setPublicationType(PublicationType publicationType) {
        this.publicationType = publicationType;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public List<AdSpace> getAdSpaces() {
        return adSpaces;
    }

    public String getReference() { return reference; }
    public void setReference(String reference) { this.reference = reference; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    // Attempts to change status and returns true if it did.
    // This is business logic for the User only -- the system is free
    // to call setStatus() however it would like.
    public boolean transitionStatus(Status newStatus) {
        if (newStatus == status) return true; // no change

        boolean allowed;
        switch (status) {
        case ACTIVE:
            allowed = newStatus == Status.PAUSED || newStatus == Status.STOPPED;
            break;
        case PAUSED:
            allowed = newStatus == Status.ACTIVE || newStatus == Status.STOPPED;
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

    public boolean isAutoApproval() { return autoApproval; }
    public void setAutoApproval(boolean autoApproval) {
        this.autoApproval = autoApproval;
    }

    public Set<Creative> getApprovedCreatives() {
        return approvedCreatives;
    }

    public Set<Creative> getDeniedCreatives() {
        return deniedCreatives;
    }
    
    public Set<ExtendedCreativeType> getThirdPartyTagVendorWhitelist() {
        return thirdPartyTagVendorWhitelist;
    }

    public void approveCreative(Creative creative) {
        if (deniedCreatives.contains(creative)) {
            deniedCreatives.remove(creative);
        }
        approvedCreatives.add(creative);
    }

    public void denyCreative(Creative creative) {
        if (approvedCreatives.contains(creative)) {
            approvedCreatives.remove(creative);
        }
        deniedCreatives.add(creative);
    }

    public Set<Category> getExcludedCategories() {
        return excludedCategories;
    }

    public Set<Category> getStatedCategories() {
        return statedCategories;
    }

    public String getDescription() { return description; }
    public void setDescription(String description) {
        this.description = description;
    }

    public Set<Language> getLanguages() { return languages; }

    public boolean getMatchUserLanguage() { return matchUserLanguage; }
    public void setMatchUserLanguage(boolean matchUserLanguage) {
        this.matchUserLanguage = matchUserLanguage;
    }

    public int getMinAge() { return minAge; }
    public void setMinAge(int minAge) { this.minAge = minAge; }

    public int getMaxAge() { return maxAge; }
    public void setMaxAge(int maxAge) { this.maxAge = maxAge; }

    public BigDecimal getGenderMix() { return genderMix; }
    public void setGenderMix(BigDecimal genderMix) {
        this.genderMix = genderMix;
    }

    public String getExternalID() { return externalID; }

    public AdSpace newAdSpace() {
        AdSpace space = new AdSpace(this);
        adSpaces.add(space);
        return space;
    }

    public String getURLString() { return urlString; }
    public void setURLString(String urlString) {
        this.urlString = urlString;
    }

    public Category getCategory(){
        return category;
    }
    public void setCategory(Category category){
        this.category = category;
    }

    // Status helpers

    // new is reserved in EL
    public boolean isStatusNew() { return (status == Status.NEW); }

    public boolean isApproved() {
        return (status.ordinal() >= Status.ACTIVE.ordinal());
    }

    public boolean isEditable() {
        return status == Status.NEW || status == Status.NEW_REVIEW
            || status == Status.REJECTED;
    }

    public Date getApprovedDate() { return approvedDate; }
    public void setApprovedDate(Date approvedDate) {
        this.approvedDate = approvedDate;
    }

    public boolean isBackfillEnabled() { return backfillEnabled; }
    public void setBackfillEnabled(boolean backfillEnabled) {
        this.backfillEnabled = backfillEnabled;
        for (AdSpace as : adSpaces) {
            as.setBackfillEnabled(backfillEnabled);
        }
    }

    public TransparentNetwork getTransparentNetwork() { return transparentNetwork; }
    public void setTransparentNetwork(TransparentNetwork transparentNetwork) {
        this.transparentNetwork = transparentNetwork;
    }

    public boolean isInstallTrackingDisabled() {
        return installTrackingDisabled;
    }
    public void setInstallTrackingDisabled(boolean installTrackingDisabled) {
        this.installTrackingDisabled = installTrackingDisabled;
    }

    public boolean isSummaryDisplayDisabled() {
        return summaryDisplayDisabled;
    }

    public void setSummaryDisplayDisabled(boolean summaryDisplayDisabled) {
        this.summaryDisplayDisabled = summaryDisplayDisabled;
    }

    public IntegrationType getDefaultIntegrationType() {
        return defaultIntegrationType;
    }
    public void setDefaultIntegrationType(IntegrationType defaultIntegrationType) {
        this.defaultIntegrationType = defaultIntegrationType;
    }

    /**
     * Auditing helpers
     */
    public String getLanguagesAsString() {
        return NamedUtils.namedCollectionToString(languages);
    }
    public String getStatedCategoriesAsString() {
        return NamedUtils.namedCollectionToString(statedCategories);
    }

    public TrackingIdentifierType getTrackingIdentifierType() { return trackingIdentifierType; }
    public void setTrackingIdentifierType(TrackingIdentifierType trackingIdentifierType) {
        this.trackingIdentifierType = trackingIdentifierType;
    }

    public TrackingIdentifierType getEffectiveTrackingIdentifierType() {
        return trackingIdentifierType == null ? publicationType.getDefaultTrackingIdentifierType() : trackingIdentifierType;
    }

    public Long getAdRequestTimeout() { return adRequestTimeout; }
    public void setAdRequestTimeout(Long adRequestTimeout) {
        this.adRequestTimeout = adRequestTimeout;
    }

    public long getEffectiveAdRequestTimeout() {
        return adRequestTimeout == null ? publisher.getDefaultAdRequestTimeout() : adRequestTimeout;
    }

    public String getRtbId() {
        return rtbId;
    }
    public void setRtbId(String rtbId) {
        this.rtbId = rtbId;
    }

    public Map<BidType,RateCard> getRateCardMap() {
        return rateCardMap;
    }

    public Map<BidType,RateCard> getEffectiveRateCardMap() {
        return rateCardMap.isEmpty() ? publisher.getDefaultRateCardMap() : rateCardMap;
    }

    public RateCard getEffectiveRateCard(BidType bidType) {
        RateCard rateCard = rateCardMap.get(bidType);
        // Fall back on the Publisher's default RateCard map if the pub doesn't have it set.
        return rateCard != null ? rateCard : publisher.getDefaultRateCard(bidType);
    }

    public boolean isIncentivized() {
        return incentivized;
    }
    public void setIncentivized(boolean incentivized) {
        this.incentivized = incentivized;
    }

    public AdOpsStatus getAdOpsStatus() {
        return adOpsStatus;
    }
    public void setAdOpsStatus(AdOpsStatus adOpsStatus) {
        this.adOpsStatus = adOpsStatus;
    }
    
    /**
     * Publisher is happy to disclose their identity for this publication.
     */
    public boolean isDisclosed() {
        return disclosed;
    }
    
    /**
     * Set if the publisher is happy to disclose their identity for this 
     * publication.
     */
    public void setDisclosed(boolean disclosed) {
        this.disclosed = disclosed;
    }
    
    public Date getCreationTime() { 
        return creationTime; 
    }
    
    public Date getSubmissionTime() { 
        return submissionTime; 
    }
    
    public void setSubmissionTime(Date submissionTime) { 
        this.submissionTime = submissionTime; 
    }

	public RateCard getEcpmTargetRateCard() {
		return ecpmTargetRateCard;
	}

	public void setEcpmTargetRateCard(RateCard ecpmTargetRateCard) {
		this.ecpmTargetRateCard = ecpmTargetRateCard;
	}

    public AdfonicUser getAssignedTo() {
        return assignedTo;
    }
    public void setAssignedTo(AdfonicUser assignedTo) {
        this.assignedTo = assignedTo;
    }

    public Set<AdfonicUser> getWatchers() {
        return watchers;
    }
    
    public List<PublicationHistory> getHistory() {
        return history;
    }
    
    // Get the friendly name of the publication the user is happy to disclose.
    public String getFriendlyName() { 
        return friendlyName; 
    }
    
    // Set the friendly name of the publication the user is happy to disclose.
    public void setFriendlyName(String friendlyName) {
        this.friendlyName = friendlyName; 
    }
    
    public Set<BidType> getBlockedBidTypes() {
        return this.blockedBidTypes;
    }

	public Map<PublicationAttributeKey, String> getPublicationAttributes() {
		return publicationAttributes;
	}

	public void setPublicationAttributes(
			Map<PublicationAttributeKey, String> publicationAttributes) {
		this.publicationAttributes = publicationAttributes;
	}
	
	public void setPublicationAttribute(PublicationAttributeKey key, Object value) {
		this.publicationAttributes.put(key, value.toString());
	}
    
    public String getPublicationAttribute(PublicationAttributeKey key) {
    	return this.publicationAttributes.get(key);
    }
    
    public Double getDoublePublicationAttribute(PublicationAttributeKey key) {
    	return new Double(this.publicationAttributes.get(key));
    }
    
    public Long getLongPublicationAttribute(PublicationAttributeKey key) {
    	return new Long(this.publicationAttributes.get(key));
    }
    
    public Integer getIntegerPublicationAttribute(PublicationAttributeKey key) {
    	return new Integer(this.publicationAttributes.get(key));
    }
    
    public Boolean getBooleanPublicationAttribute(PublicationAttributeKey key) {
    	return BooleanUtils.toBoolean(this.publicationAttributes.get(key));
    }
    
    public Boolean getSoftFloor() {
    	return getBooleanPublicationAttribute(PublicationAttributeKey.SOFT_FLOOR);
    }
    
    public void setSoftFloor(Boolean value) {
    	setPublicationAttribute(PublicationAttributeKey.SOFT_FLOOR, value);
    }
    
    public PublicationSafetyLevel getSafetyLevel(){
    	return  safetyLevel;
    }
    
    public void setSafetyLevel(PublicationSafetyLevel safetyLevel){
    	this.safetyLevel = safetyLevel;
    }

    public Integer getSamplingRate() {
        return samplingRate;
    }

    public void setSamplingRate(Integer samplingRate) {
        this.samplingRate = samplingRate;
    }

    public Set<PublicationProvidedInfo> getPublicationProvidedInfos() {
        return publicationProvidedInfos;
    }
}
