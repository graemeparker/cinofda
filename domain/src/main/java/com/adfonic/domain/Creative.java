package com.adfonic.domain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
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
import javax.persistence.MapKeyJoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

/**
 * Gives an identifier to the assets associated with a creative.
 * The individual assets will match the names in the contentSpecs
 * of the referenced Format.
 */
@Entity
@Table(name="CREATIVE")
public class Creative extends BusinessKey implements Named, HasExternalID {
    private static final long serialVersionUID = 17L;

    public enum Status {
        NEW, PENDING, PENDING_PAUSED, REJECTED, ACTIVE, PAUSED, STOPPED;
    }

    @Id @GeneratedValue @Column(name="ID")
    private long id;
    @Column(name="NAME",length=255,nullable=false)
    private String name;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="CAMPAIGN_ID", insertable=true, updatable=false, nullable=true)
    private Campaign campaign;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="SEGMENT_ID",nullable=true)
    private Segment segment;
    @ManyToOne(fetch=FetchType.EAGER)
    @JoinColumn(name="FORMAT_ID",nullable=false)
    private Format format;
    @OneToMany(mappedBy="creative",fetch=FetchType.LAZY)
    @MapKeyJoinColumn(name="DISPLAY_TYPE_ID",referencedColumnName="ID")
    private Map<DisplayType,AssetBundle> assetBundleMap;
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="DESTINATION_ID",nullable=true)
    private Destination destination;

    @NotCopied("Every new (or copied) creative has its own status flow")
    @Column(name="STATUS",length=32,nullable=false)
    @Enumerated(EnumType.STRING)
    private Status status;
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="LANGUAGE_ID",nullable=false)
    private Language language;
    @Column(name="ENGLISH_TRANSLATION",length=255,nullable=true)
    private String englishTranslation;

    @NotCopied("Auto-generated when created (or copied)")
    @Column(name="EXTERNAL_ID",length=255,nullable=false)
    private String externalID;
    @Column(name="LAST_UPDATED",nullable=true)
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastUpdated;

    @NotCopied("This gets set when the creative is ultimately approved")
    @Column(name="APPROVED_DATE",nullable=true)
    @Temporal(TemporalType.TIMESTAMP)
    private Date approvedDate;
    /**
     * Deleted for the Premium Performance Network rework
     *
    * Indicates whether this ad can be served on publications that
    * are part of a premium TransparentNetwork (if the price and targeting
    * make it eligible).

    @NotCopied("TODO: verify this")
    @Column(name="PREMIUM_ACCEPTABLE",nullable=false)
    private boolean premiumAcceptable;
    */

    @Column(name="PLUGIN_BASED",nullable=false)
    private boolean pluginBased;
    @Column(name="PRIORITY",nullable=false)
    private int priority;
    @Column(name="END_DATE",nullable=true)
    @Temporal(TemporalType.TIMESTAMP)
    private Date endDate;
    @OneToMany(fetch=FetchType.LAZY)
    @JoinTable(name="CREATIVE_REMOVED_PUBLICATION_MAP",joinColumns=@JoinColumn(name="CREATIVE_ID",referencedColumnName="ID"),inverseJoinColumns=@JoinColumn(name="REMOVAL_INFO_ID",referencedColumnName="ID"))
    @MapKeyJoinColumn(name="PUBLICATION_ID",referencedColumnName="ID")
    private Map<Publication,RemovalInfo> removedPublications;
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="EXTENDED_CREATIVE_TYPE_ID",nullable=true)
    private ExtendedCreativeType extendedCreativeType;
    // JPA:
    @ElementCollection(fetch=FetchType.EAGER,targetClass=String.class)
    @CollectionTable(name="EXTENDED_CREATIVE_DATA",joinColumns=@JoinColumn(name="CREATIVE_ID",referencedColumnName="ID"))
    @MapKeyColumn(name="NAME",length=255,nullable=false)
    @MapKeyClass(String.class)
    @Column(name="VALUE",length=1024,nullable=false)
    private Map<String,String> extendedData;

    @NotCopied("Every new (or copied) creative has its own creation time")
    @Column(name="CREATION_TIME",nullable=false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date creationTime;

    @NotCopied("Every new (or copied) creative must be submitted separately")
    @Column(name="SUBMISSION_TIME",nullable=true)
    @Temporal(TemporalType.TIMESTAMP)
    private Date submissionTime;

    @NotCopied("Every new (or copied) creative will be assigned separately")
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="ASSIGNED_TO_ADFONIC_USER_ID",nullable=true)
    private AdfonicUser assignedTo;
    
    @NotCopied("Every new (or copied) creative will be assigned separately")
    @Column(name="CLOSED_MODE",nullable=false)
    private boolean closedMode;

    @NotCopied("Every new (or copied) creative will be assigned separately")
    @Column(name="ALLOW_EXTERNAL_AUDIT",nullable=false)
    private boolean allowExternalAudit;
    
    @NotCopied("Every new (or copied) creative will have its own history")
    @OneToMany(mappedBy="creative",fetch=FetchType.LAZY)
    @OrderBy("id")
    private List<CreativeHistory> history;

    @OneToMany(mappedBy="creative",fetch=FetchType.LAZY)
    private Set<ExtendedCreativeTemplate> extendedCreativeTemplates;

    @NotCopied("See ticket, no specification provided even though requested")
    @ManyToMany(fetch=FetchType.EAGER)
    @JoinTable(name="CREATIVE_CREATIVE_ATTRIBUTE",joinColumns=@JoinColumn(name="CREATIVE_ID",referencedColumnName="ID"),inverseJoinColumns=@JoinColumn(name="CREATIVE_ATTRIBUTE_ID",referencedColumnName="ID"))
    @Fetch(FetchMode.SELECT)
    private Set<CreativeAttribute> creativeAttributes;
    
    @NotCopied("Every new (or copied) creative will have its own history")
    @OneToMany(mappedBy="creative",fetch=FetchType.LAZY)
    private Collection<PublisherAuditedCreative> publishersAuditedCreative;
    
    @Column(name = "SSL_COMPLIANT", nullable = false)
    private boolean sslCompliant; // MAD-1738
    
    @Column(name = "SSL_OVERRIDE", nullable = false)
    private boolean sslOverride; // MAD-3395
    
    //========================================================================
    // ***** WARNING *****
    // WHENEVER YOU ADD NEWS FIELDS, YOU NEED TO CONSIDER WHETHER OR NOT THEY
    // ARE INCLUDED IN THE FIELDS THAT GET COPIED WHEN copyFrom IS INVOKED.
    // ***** YOU ALSO NEED TO INCREMENT THE serialVersionUID. *****
    //========================================================================

    {
        this.creationTime = new Date();
        this.assetBundleMap = new HashMap<DisplayType,AssetBundle>();
        this.status = Status.NEW;
        this.externalID = UUID.randomUUID().toString();
        this.removedPublications = new HashMap<Publication,RemovalInfo>();
        this.extendedData = new HashMap<String,String>();
        this.history = new ArrayList<>();
        this.extendedCreativeTemplates = new HashSet<ExtendedCreativeTemplate>();
        this.creativeAttributes = new HashSet<CreativeAttribute>();
        this.publishersAuditedCreative = new ArrayList<>();
        this.sslOverride = false;
    }

    Creative() {}

    /**
    * Instantiates a new Creative and generates AssetBundles for
    * each DisplayType identified by the Format.
    */
    Creative(Segment segment, Format format) {
        this.segment = segment;
        this.format = format;
    }

    Creative(Campaign campaign, Segment segment, Format format) {
        this.segment = segment;
        this.format = format;
        this.campaign = campaign;
    }

    public long getId() { return id; };

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Campaign getCampaign() { return campaign; }

    /** Called by campaign.addCreative() */
    void initializeCampaign(Campaign campaign) {
        if (this.campaign != null && this.campaign != campaign) {
            throw new IllegalArgumentException("Can't change the campaign on an existing creative");
        }
        this.campaign = campaign;
    }

    public Segment getSegment() { return segment; }

    public Format getFormat() { return format; }
    public void setFormat(Format format) {
        if (format != this.format) {
            assetBundleMap.clear();
        }
        this.format = format;
    }

    public AssetBundle getAssetBundle(DisplayType displayType) {
        return assetBundleMap.get(displayType);
    }

    public Map<DisplayType,AssetBundle> getAssetBundleMap() {
        return assetBundleMap;
    }

    public boolean hasAssetBundle(DisplayType displayType) {
        return assetBundleMap.containsKey(displayType);
    }

    public Destination getDestination() { return destination; }
    public void setDestination(Destination destination) {
        this.destination = destination;

        pluginBased = false;
        if (destination != null) {
            final String data = destination.getData();
            if ((data != null) && data.startsWith("plugin://")) {
                pluginBased = true;
            }
        }
    }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    /** Makes a new asset, but doesn't add it to the map. */
    public Asset newAsset(ContentType contentType) {
        return new Asset(this, contentType);
    }

    public Language getLanguage() { return language; }
    public void setLanguage(Language language) {
        this.language = language;
    }

    public String getExternalID() { return externalID; }

    public AssetBundle newAssetBundle(DisplayType displayType) {
        AssetBundle bundle = new AssetBundle(this, displayType);
        assetBundleMap.put(displayType, bundle);
        return bundle;
    }

    public Date getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Date lastUpdated) {
        this.lastUpdated = lastUpdated;
    }


    public String getEnglishTranslation() { return englishTranslation; }
    public void setEnglishTranslation(String englishTranslation) {
        this.englishTranslation = englishTranslation;
    }

    public boolean isEditable() { return status == Status.NEW || status == Status.REJECTED; }

    public Date getApprovedDate() { return approvedDate; }
    public void setApprovedDate(Date approvedDate) {
        this.approvedDate = approvedDate;
    }

    /**
     *
     * Deleted for the premium performance network rework


    public boolean isPremiumAcceptable() { return premiumAcceptable; }
    public void setPremiumAcceptable(boolean premiumAcceptable) {
        this.premiumAcceptable = premiumAcceptable;
    }


    */

    public boolean isPluginBased() { return pluginBased; }
    public void setPluginBased(boolean pluginBased) {
        this.pluginBased = pluginBased;
    }

    public int getPriority() { return priority; }
    public void setPriority(int priority) {
        this.priority = priority;
    }

    public Date getEndDate() {
        return endDate;
    }
    public void setEndDate(Date endDate) {
        this.endDate = endDate;
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

    public ExtendedCreativeType getExtendedCreativeType() {
        return extendedCreativeType;
    }
    public void setExtendedCreativeType(ExtendedCreativeType extendedCreativeType) {
        this.extendedCreativeType = extendedCreativeType;
    }

    public Map<String,String> getExtendedData() {
        return extendedData;
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

    public AdfonicUser getAssignedTo() {
        return assignedTo;
    }
    public void setAssignedTo(AdfonicUser assignedTo) {
        this.assignedTo = assignedTo;
    }

    public List<CreativeHistory> getHistory() {
        return history;
    }

	public Set<ExtendedCreativeTemplate> getExtendedCreativeTemplates() {
		return extendedCreativeTemplates;
	}

	public void setExtendedCreativeTemplates(
			Set<ExtendedCreativeTemplate> extendedCreativeTemplates) {
		this.extendedCreativeTemplates = extendedCreativeTemplates;
	}
    
    public boolean isClosedMode() { return closedMode; }
    public void setClosedMode(boolean closedMode) {
        this.closedMode = closedMode;
    }
    
    public boolean isAllowExternalAudit() { return allowExternalAudit; }
    public void setAllowExternalAudit(boolean allowExternalAudit) {
        this.allowExternalAudit = allowExternalAudit;
    }

    public Set<CreativeAttribute> getCreativeAttributes() {
        return creativeAttributes;
    }

    public void setCreativeAttributes(Set<CreativeAttribute> creativeAttributes) {
        this.creativeAttributes = creativeAttributes;
    }

    public Collection<PublisherAuditedCreative> getPublishersAuditedCreative() {
        return publishersAuditedCreative;
    }

    public void setPublishersAuditedCreative(Collection<PublisherAuditedCreative> publishersAuditedCreative) {
        this.publishersAuditedCreative = publishersAuditedCreative;
    }

    public boolean isSslCompliant() {
        return sslCompliant;
    }

    public void setSslCompliant(boolean httpsReady) {
        this.sslCompliant = httpsReady;
    }

	public boolean isSslOverride() {
		return sslOverride;
	}

	public void setSslOverride(boolean sslOverride) {
		this.sslOverride = sslOverride;
	}
    
 }
