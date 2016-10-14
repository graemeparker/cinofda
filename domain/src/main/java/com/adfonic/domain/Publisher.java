package com.adfonic.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.MapKeyClass;
import javax.persistence.MapKeyColumn;
import javax.persistence.MapKeyEnumerated;
import javax.persistence.MapKeyJoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderBy;
import javax.persistence.Table;

/**
 * Represents the "publisher" identity of a given company.
 */
@Entity
@Table(name="PUBLISHER")
public class Publisher extends BusinessKey implements Named, HasExternalID {
    private static final long serialVersionUID = 5L;

    public static final BigDecimal DEFAULT_REV_SHARE = new BigDecimal("0.60");

    @Id @GeneratedValue @Column(name="ID")
    private long id;
    @Column(name="EXTERNAL_ID",length=255,nullable=false)
    private String externalID;
    // Careful here. Even tho the relationship is marked as LAZY, OneToOne lazies are not supported by Hibernate.
    // The object will always be hydrated.
    @OneToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="COMPANY_ID",nullable=false)
    private Company company;
    @Column(name="NAME",length=255,nullable=false)
    private String name;
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="ACCOUNT_ID",nullable=false)
    private Account account;
    @OneToMany(mappedBy="publisher",fetch=FetchType.LAZY)
    private Set<Publication> publications;
    @ManyToMany(fetch=FetchType.LAZY)
    @JoinTable(name="PUBLISHER_APPROVED_CREATIVE",joinColumns=@JoinColumn(name="PUBLISHER_ID",referencedColumnName="ID"),inverseJoinColumns=@JoinColumn(name="CREATIVE_ID",referencedColumnName="ID"))
    private Set<Creative> approvedCreatives;
    @Column(name="PENDING_AD_TYPE",length=32,nullable=false)
    @Enumerated(EnumType.STRING)
    private PendingAdType pendingAdType;
    @Column(name="DEFAULT_AD_REQUEST_TIMEOUT",nullable=false)
    private long defaultAdRequestTimeout;
    @Column(name="DEFAULT_REV_SHARE",nullable=false)
    private BigDecimal defaultRevShare;
    @OneToMany(fetch=FetchType.LAZY)
    @JoinTable(name="PUBLISHER_DEFAULT_RATE_CARD_MAP",joinColumns=@JoinColumn(name="PUBLISHER_ID",referencedColumnName="ID"),inverseJoinColumns=@JoinColumn(name="RATE_CARD_ID",referencedColumnName="ID"))
    @MapKeyColumn(name="BID_TYPE")
    @MapKeyClass(BidType.class)
    @MapKeyEnumerated(EnumType.STRING)
    private Map<BidType,RateCard> defaultRateCardMap;

    // adops support fields
    @Column(name="IS_KEY",nullable=false)
    private boolean key;
    @ManyToOne(fetch=FetchType.EAGER)
    @JoinColumn(name="AD_OPS_OWNER_ID",nullable=true)
    private AdfonicUser adOpsOwner;
    @ManyToOne(fetch=FetchType.EAGER)
    @JoinColumn(name="SALES_OWNER_ID",nullable=true)
    private AdfonicUser salesOwner;

    @OneToMany(fetch=FetchType.LAZY)
    @JoinTable(name="PUBLISHER_DEFAULT_INTEGRATION_TYPE_MAP",joinColumns=@JoinColumn(name="PUBLISHER_ID",referencedColumnName="ID"),inverseJoinColumns=@JoinColumn(name="INTEGRATION_TYPE_ID",referencedColumnName="ID"))
    @MapKeyJoinColumn(name="PUBLICATION_TYPE_ID",referencedColumnName="ID")
    private Map<PublicationType,IntegrationType> defaultIntegrationTypeMap;

    @ManyToOne(fetch=FetchType.EAGER)
    @JoinColumn(name="CURRENT_REV_SHARE_ID",nullable=true)
    private PublisherRevShare currentRevShare;

    @OneToMany(fetch=FetchType.LAZY)
    //@OrderColumn(name="PUBLISHER_ORDER",nullable=false,insertable=true,updatable=true)
    @OrderBy("id")
    @JoinColumn(name="PUBLISHER_ID", nullable=true)
    private List<PublisherRevShare> revShareHistory; // audit trail of revshare changes

    @OneToOne(fetch=FetchType.EAGER)
    @JoinColumn(name = "RTB_CONFIG_ID")
    private RtbConfig rtbConfig;

    @ManyToMany(fetch=FetchType.LAZY)
    @JoinTable(name="PUBLISHER_EXCLUDED_CATEGORY",joinColumns=@JoinColumn(name="PUBLISHER_ID",referencedColumnName="ID"),inverseJoinColumns=@JoinColumn(name="CATEGORY_ID",referencedColumnName="ID"))
    private Set<Category> excludedCategories;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="ECPM_TARGET_RATE_CARD_ID",nullable=true)
    private RateCard ecpmTargetRateCard;

    @Column(name="BUYER_PREMIUM",nullable=false)
    private Double buyerPremium;

    // publisher is happy to disclose their identify
    @Column(name="DISCLOSED",nullable=false)
    private boolean disclosed;

    @ElementCollection(fetch=FetchType.LAZY,targetClass=BidType.class)
    @CollectionTable(name="PUBLISHER_BLOCKED_BID_TYPE",joinColumns=@JoinColumn(name="PUBLISHER_ID",referencedColumnName="ID"))
    @Column(name="BID_TYPE",nullable=false)
    @Enumerated(EnumType.STRING)
    private Set<BidType> blockedBidTypes;
    
    @Column(name="REQUIRES_REAL_DESTINATION",nullable=false)
    private boolean requiresRealDestination;

    // MAD-787
    @ManyToMany(fetch=FetchType.LAZY)
    @JoinTable(name="PUBLISHER_EXTENDED_CREATIVE_TYPE", joinColumns=@JoinColumn(name="PUBLISHER_ID", referencedColumnName="ID"), inverseJoinColumns=@JoinColumn(name="EXTENDED_CREATIVE_TYPE_ID", referencedColumnName="ID"))
    private Set<ExtendedCreativeType> thirdPartyTagVendorWhitelist;
    
    {
        this.externalID = UUID.randomUUID().toString();
        this.account = new Account(AccountType.PUBLISHER);
        this.publications = new HashSet<Publication>();
        this.approvedCreatives = new HashSet<Creative>();
        this.pendingAdType = PendingAdType.HOLDING_AD;
        this.defaultAdRequestTimeout = 2000;
        this.defaultRevShare = DEFAULT_REV_SHARE;
        this.defaultRateCardMap = new HashMap<BidType,RateCard>();
        this.key = false;
        this.defaultIntegrationTypeMap = new HashMap<PublicationType,IntegrationType>();
        this.revShareHistory = new LinkedList<PublisherRevShare>();
        this.excludedCategories = new HashSet<Category>();
        this.disclosed = false;
        this.buyerPremium = 0.0;
        this.blockedBidTypes = new HashSet<BidType>();
        this.requiresRealDestination = false;
        this.thirdPartyTagVendorWhitelist = new HashSet<ExtendedCreativeType>();
    }

    Publisher() {}

    public Publisher(Company company, String name) {
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

    public Account getAccount() {
        return account;
    }

    public Set<Publication> getPublications() {
        return publications;
    }

    public Set<Creative> getApprovedCreatives() {
        return approvedCreatives;
    }

    public PendingAdType getPendingAdType() {
        return pendingAdType;
    }
    public void setPendingAdType(PendingAdType pendingAdType) {
        this.pendingAdType = pendingAdType;
    }

    public long getDefaultAdRequestTimeout() {
        return defaultAdRequestTimeout;
    }
    public void setDefaultAdRequestTimeout(long defaultAdRequestTimeout) {
        this.defaultAdRequestTimeout = defaultAdRequestTimeout;
    }

    public BigDecimal getDefaultRevShare() {
        return defaultRevShare;
    }
    public void setDefaultRevShare(BigDecimal defaultRevShare) {
        this.defaultRevShare = defaultRevShare;
    }

    public Map<BidType,RateCard> getDefaultRateCardMap() {
        return defaultRateCardMap;
    }

    public RateCard getDefaultRateCard(BidType bidType) {
        return defaultRateCardMap.get(bidType);
    }

    public boolean isKey() {
        return key;
    }
    public void setKey(boolean key) {
        this.key = key;
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

    public Map<PublicationType,IntegrationType> getDefaultIntegrationTypeMap() {
        return defaultIntegrationTypeMap;
    }

    public IntegrationType getDefaultIntegrationType(PublicationType publicationType) {
        return defaultIntegrationTypeMap.get(publicationType);
    }
    public void setDefaultIntegrationType(PublicationType publicationType, IntegrationType integrationType) {
        defaultIntegrationTypeMap.put(publicationType, integrationType);
    }

    public List<PublisherRevShare> getRevShareHistory() {
        return revShareHistory;
    }

    public BigDecimal getCurrentRevShare() {
        return currentRevShare.getRevShare();
    }

    public PublisherRevShare getCurrentPublisherRevShare() {
        return currentRevShare;
    }

    public void setCurrentRevShare(PublisherRevShare revShare) {
        this.currentRevShare = revShare;
    }

    public PublisherRevShare newPublisherRevShare(BigDecimal revShare, Date startDate) {
        return new PublisherRevShare(this, revShare, startDate);
    }

    public BigDecimal getRevShareForDate(Date date) {
        for (PublisherRevShare rs : revShareHistory) {
            if (!date.before(rs.getStartDate())) {
                Date rsEnd = rs.getEndDate();
                if ((rsEnd == null) || date.before(rsEnd)) {
                    return rs.getRevShare();
                }
            }
        }
        return null; // no revshare for that time
    }

    /**
     * Calculate "payout" for this publisher and a given CampaignBid.
     * We use the formula:
     *   payout = bidAmount * (1 - advertiserDiscount) * publisherRevShare
     * This version uses the publisher's current rev share.
     */
    public BigDecimal getPayout(CampaignBid campaignBid) {
        return getPayout(campaignBid.getCampaign(), campaignBid);
    }

    /**
     * Calculate "payout" for this publisher and a given CampaignBid.
     * We use the formula:
     *   payout = bidAmount * (1 - advertiserDiscount) * publisherRevShare
     * This version uses the publisher's current rev share.
     * It also takes Advertiser directly in case you're dealing with a detached
     * CampaignBid object that doesn't have its Campaign field detached.
     */
    public BigDecimal getPayout(Campaign campaign, CampaignBid campaignBid) {
        return getPayout(campaign, campaignBid, null);
    }

    /**
     * Calculate "payout" for this Publisher and a given CampaignBid.
     * We use the formula:
     *   payout = bidAmount * (1 - advertiser.discount) * publisherRevShare
     * This version determines the publisher's rev share as of the given event time.
     */
    public BigDecimal getPayout(Campaign campaign, CampaignBid campaignBid, Date eventTime) {
        BigDecimal revShare = eventTime == null ? currentRevShare.getRevShare() : getRevShareForDate(eventTime);
        return getPayout(revShare, campaign.getCurrentAgencyDiscountAmount(), campaignBid.getAmount(), campaign.getAdvertiser().getCompany().getCurrentMarginShareDSPValue());
    }

    /**
     * Static helper method to do the actual calculation of payout
     * We use the formula:
     *   payout = bidAmount * (1 - advertiser.discount) * publisherRevShare
     */
    public static BigDecimal getPayout(BigDecimal revShare, BigDecimal agencyDiscount, BigDecimal campaignBidAmount, BigDecimal marginShareDSP) {
    	//MAD-529
    	// N = COST * (1 - campaign.agencydiscount), i.e. net revenue
    	// m = Company.currentMarginShareDSP
    	// r = Publisher.currentRevShare
    	// PAYOUT = N * r * m / (1 + r * m - r)
    	BigDecimal N = campaignBidAmount.multiply(BigDecimal.ONE.subtract(agencyDiscount)); // N = COST * (1 - campaign.agencyDiscount)
    	BigDecimal r = revShare;
    	BigDecimal m = marginShareDSP;
    	BigDecimal num = (N.multiply(r).multiply(m)); // N * r * m 
    	BigDecimal denom = BigDecimal.ONE.add(r.multiply(m).subtract(r)); // (1 + r * m - r)
    	return num.divide(denom, RoundingMode.HALF_UP); // N * r * m / (1 + r * m - r)
        //return campaignBidAmount.multiply(BigDecimal.ONE.subtract(advertiserDiscount)).multiply(revShare);
    }

    public RtbConfig getRtbConfig() {
        return rtbConfig;
    }

    public void setRtbConfig(RtbConfig rtbConfig) {
        this.rtbConfig = rtbConfig;
    }

    /**
     * Convenience method for determining if a Publisher is RTB-enabled
     */
    public boolean isRtbEnabled() {
        return rtbConfig != null;
    }

    public Set<Category> getExcludedCategories() {
        return excludedCategories;
    }

    /**
     * Publisher is happy to disclose their identity for all publications.
     */
    public boolean isDisclosed() {
        return disclosed;
    }

    /**
     * Set if the publisher is happy to disclose their identity for all
     * publications.
     */
    public void setDisclosed(boolean disclosed) {
        this.disclosed = disclosed;
    }

    public RateCard getEcpmTargetRateCard() {
        return ecpmTargetRateCard;
    }

    public void setEcpmTargetRateCard(RateCard ecpmTargetRateCard) {
        this.ecpmTargetRateCard = ecpmTargetRateCard;
    }

    public Double getBuyerPremium() {
        return buyerPremium;
    }

    public void setBuyerPremium(Double buyerPremium) {
        this.buyerPremium = buyerPremium;
    }
    
    public Set<BidType> getBlockedBidTypes() {
        return this.blockedBidTypes;
    }

	public boolean isRequiresRealDestination() {
		return requiresRealDestination;
	}

	public void setRequiresRealDestination(boolean requiresRealDestination) {
		this.requiresRealDestination = requiresRealDestination;
	}

	public Set<ExtendedCreativeType> getThirdPartyTagVendorWhitelist() {
		return thirdPartyTagVendorWhitelist;
	}
}
