package com.adfonic.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.persistence.*;

import com.adfonic.util.AgeRangeTargetingLogic;
import com.adfonic.util.Range;

/**
 * A transparent network is a set of one or more sites and apps
 * (publications) that have been packaged together in order to
 * appeal to advertisers.  Any given site or app is either in the
 * performance network (no transparent) or belongs to a single TransparentNetwork.
 */
@Entity
@Table(name="TRANSPARENT_NETWORK")
public class TransparentNetwork extends BusinessKey implements Named {
    private static final long serialVersionUID = 3L;

    // This is the well-known reserved name of our performance network.
    // This constant should be used in preference to hard-coding a string
    // literal in lots of places in app code.  This needs to match the
    // TRANSPARENT_NETWORK.NAME field in the db.  See Sprint 24 incrementals.
    public static final String PERFORMANCE_NETWORK_NAME = "__RESERVED_ADFONIC_PERFORMANCE_NETWORK";

    @Id @GeneratedValue @Column(name="ID")
    private long id;
    
    /** The publications that comprise the network; often just one. */
    @OneToMany(mappedBy="transparentNetwork",fetch=FetchType.LAZY)
    private Set<Publication> publications;

    /**
     * A closed, or private, network, is one that is only visible by a VIP
     * list of specific advertisers.
     */
    @Column(name="CLOSED",nullable=false)
    private boolean closed;

    /** Set of advertisers who can view this network if it is closed */
    @ManyToMany(fetch=FetchType.LAZY)
    @JoinTable(name="COMPANY_TRANSPARENT_NETWORK",joinColumns=@JoinColumn(name="TRANSPARENT_NETWORK_ID",referencedColumnName="ID"),inverseJoinColumns=@JoinColumn(name="COMPANY_ID",referencedColumnName="ID"))
    private Set<Company> advertisers;

    /** 
     * Pricing information for the network.  If there is no entry for a
     * BidType, the network does not accept bids for that type (for example,
     * could be CPM only).
     *
     * A transparent network where rateCardMap.isEmpty() uses the system
     * default rate card.  This will be the case with most transparent
     * (non-premium) networks.  See #isDefaultRateCard().
     */
    @OneToMany(fetch=FetchType.LAZY)
    @JoinTable(name="TRANSPARENT_NETWORK_RATE_CARD_MAP",joinColumns=@JoinColumn(name="TRANSPARENT_NETWORK_ID",referencedColumnName="ID"),inverseJoinColumns=@JoinColumn(name="RATE_CARD_ID",referencedColumnName="ID"))
    @MapKeyColumn(name="BID_TYPE")
    @MapKeyClass(BidType.class)
    private Map<BidType,RateCard> rateCardMap;

    // Publisher-provided info for display
    @Column(name="NAME",length=255,nullable=false)
    private String name;
    @Column(name="DESCRIPTION",length=255,nullable=true)
    private String description;
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="ICON_CONTENT_ID",nullable=true)
    private UploadedContent icon;
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="SCREEN_SHOT_CONTENT_ID",nullable=true)
    private UploadedContent screenShot;

    // Statistics
    @Column(name="REQUESTS",nullable=true)
    private Integer requests;
    @Column(name="UNIQUE_USERS",nullable=true)
    private Integer uniqueUsers;
    @Column(name="CLICK_THROUGH_RATE",nullable=true)
    private BigDecimal clickThroughRate;
    /** 
     * All countries with more than the required daily requests, 
     * associated with a percentage figure (all floats less than or
     * equal to 1.0).
     */
    @ElementCollection(fetch=FetchType.LAZY,targetClass=Float.class)
    @CollectionTable(name="TRANSPARENT_NETWORK_COUNTRY_MAP",joinColumns=@JoinColumn(name="TRANSPARENT_NETWORK_ID",referencedColumnName="ID"))
    @MapKeyJoinColumn(name="COUNTRY_ID",referencedColumnName="ID")
    @Column(name="PERCENT",nullable=false)
    private Map<Country,Float> topCountries;

    {
	this.advertisers = new HashSet<Company>();
	this.publications = new HashSet<Publication>();
	this.rateCardMap = new HashMap<BidType,RateCard>();
	this.topCountries = new HashMap<Country,Float>();
    }
    
    TransparentNetwork() {}

    public TransparentNetwork(String name) {
	this.name = name;
    }

    public long getId() { return id; };

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    /** Closed networks are private, i.e. only visible to invited advertisers. */
    public boolean isClosed() { return closed; }
    public void setClosed(boolean closed) { this.closed = closed; }

    public Set<Company> getAdvertisers() { return advertisers; }

    public Set<Publication> getPublications() { return publications; }

    public boolean isDefaultRateCard() {
	return rateCardMap.isEmpty();
    }

    /** If a bid type is not supported, there will be no entry in the map. */
    public Map<BidType,RateCard> getRateCardMap() {
	return rateCardMap;
    }

    public RateCard getRateCard(BidType bidType) {
	return rateCardMap.get(bidType);
    }

    public String getDescription() { return description; }
    public void setDescription(String description) {
	this.description = description;
    }

    public UploadedContent getIcon() { return icon; }
    public void setIcon(UploadedContent icon) {
	this.icon = icon;
    }

    public UploadedContent getScreenShot() { return screenShot; }
    public void setScreenShot(UploadedContent screenShot) {
	this.screenShot = screenShot;
    }

    // Statistics
    public Integer getRequests() { return requests; }
    public void setRequests(Integer requests) { this.requests = requests; }

    public Integer getUniqueUsers() { return uniqueUsers; }
    public void setUniqueUsers(Integer uniqueUsers) { 
	this.uniqueUsers = uniqueUsers; 
    }

    public BigDecimal getClickThroughRate() { return clickThroughRate; }
    public void setClickThroughRate(BigDecimal clickThroughRate) {
	this.clickThroughRate = clickThroughRate;
    }

    public Map<Country,Float> getTopCountries() {
	return topCountries;
    }

    // Utility methods
    public Set<Format> getFormats() {
	Set<Format> formats = new HashSet<Format>();
	for (Publication p : publications) {
	    if (p.getStatus() == Publication.Status.ACTIVE) {
		for (AdSpace as : p.getAdSpaces()) {
		    formats.addAll(as.getFormats());
		}
	    }
	}
	return formats;
    }

    public Set<Category> getCategories() {
        Set<Category> categories = new HashSet<Category>();
        for (Publication p : publications) {
            if (p.getStatus() == Publication.Status.ACTIVE) {
                categories.add(p.getCategory());
            }
        }
        return categories;
    }

    public Range<Integer> getAgeRange() {
	int count = 0;
	int min = AgeRangeTargetingLogic.MAX_AGE;
	int max = AgeRangeTargetingLogic.MIN_AGE; // yes these are intentionally reversed

	for (Publication p : publications) {
	    if (p.getStatus() == Publication.Status.ACTIVE) {
		min = Math.min(min, p.getMinAge());
		max = Math.max(max, p.getMaxAge());
		++count;
	    }
	}

	// Be nice in the case where there are no publications
	if (count == 0) return new Range<Integer>(AgeRangeTargetingLogic.MIN_AGE, AgeRangeTargetingLogic.MAX_AGE);

	return new Range<Integer>(min, max);
    }

    // Note: this method ignores weighting of the publications for now
    public BigDecimal getGenderMix() {
	int count = 0;
	BigDecimal sum = BigDecimal.ZERO;

	for (Publication p : publications) {
	    if (p.getStatus() == Publication.Status.ACTIVE) {
		sum = sum.add(p.getGenderMix());
		++count;
	    }
	}
	
	// Be nice in the case where there are no publications
	if (count == 0) return new BigDecimal("0.5");

	return sum.divide(new BigDecimal(count), 2, RoundingMode.HALF_UP);
    }

    /** Get the set of publication types that make up this network. */
    public Set<PublicationType> getPublicationTypes() {
	Set<PublicationType> pubTypes = new HashSet<PublicationType>();
	for (Publication p : publications) {
	    if (p.getStatus() == Publication.Status.ACTIVE) {
		pubTypes.add(p.getPublicationType());
	    }
	}
	return pubTypes;
    }

    /** Get all possible platforms that represent traffic on this network. */
    public Set<Platform> getPlatforms() {
	Set<Platform> platforms = new HashSet<Platform>();
	for (PublicationType pt : getPublicationTypes()) {
	    platforms.addAll(pt.getPlatforms());
	}
	return platforms;
    }

    /** A network is paused only if all of its publications are paused. */
    public boolean isPaused() {
	for (Publication p : publications) {
	    if (p.getStatus() == Publication.Status.ACTIVE) {
		return false;
	    }
	}
	return true;
    }

    /**
     * Transparent networks should only be shown when their traffic is
     * relevant to a campaign.  This is the case when:
     * 1) The country targeting is global and the network is valid for
     *    at least one country; or
     *    The country targeting is specific and at least one country
     *    overlaps with the network.
     * 2) The platform targeting is unrestricted; or
     *    The platform targeting is specific and at least one platform
     *    overlaps with the relevant platforms based on
     *    Publication.publicationType.platforms for at least one
     *    of the publications in the network.
     */
    public boolean isVisibleFor(Segment segment) {
	if (topCountries.isEmpty()) return false;

	// Check country targeting if required
	if (segment.isGeographyTargeted()) {
	    boolean valid = false;
	    for (Country c : segment.getCountries()) {
		if (topCountries.containsKey(c)) {
		    valid = true;
		    break;
		}
	    }
	    if (!valid) return false;
	}

	// Check platform applicability
        Set<Platform> segPlatforms = segment.getPlatforms();
        if (segPlatforms.isEmpty()) {
            return true; // No restriction
        } else {
	    Set<Platform> platforms = getPlatforms();
	    for (Platform p : platforms) {
                if (segPlatforms.contains(p)) {
                    return true; // At least one platform overlaps, good enough
                }
	    }
            return false;
	}
    }
}
