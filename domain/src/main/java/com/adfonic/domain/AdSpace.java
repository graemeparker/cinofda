package com.adfonic.domain;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import javax.persistence.*;

/**
 * A publisher's definition of an ad slot.  The external ID will be the one
 * used to reference the space via the APIs.
 */
@Entity
@Table(name="AD_SPACE")
public class AdSpace extends BusinessKey implements Named, HasExternalID {
    private static final long serialVersionUID = 7L;

    public enum ColorScheme {
        blue("#fff"),
        // green, orange, purple, red, yellow,
        black("#fff"),
        grey("#000");

        private String textColor;

        private ColorScheme(String textColor) {
            this.textColor = textColor;
        }

        public String getTextColor() {
            return this.textColor;
        }
    }

    public enum Status {
        UNVERIFIED, VERIFIED, DELETED, DORMANT;
    }

    @Id @GeneratedValue @Column(name="ID")
    private long id;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="PUBLICATION_ID", insertable=true, updatable=false, nullable=true)
    private Publication publication;

    @Column(name="NAME",length=255,nullable=true)
    private String name;
    @Column(name="EXTERNAL_ID",length=255,nullable=false)
    private String externalID;
    @Column(name="CREATION_TIME",nullable=false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date creationTime;
    @Column(name="REACTIVATION_TIME",nullable=true)
    @Temporal(TemporalType.TIMESTAMP)
    private Date reactivationTime;
    @ManyToMany(fetch=FetchType.EAGER)
    @JoinTable(name="AD_SPACE_FORMAT",joinColumns=@JoinColumn(name="AD_SPACE_ID",referencedColumnName="ID"),inverseJoinColumns=@JoinColumn(name="FORMAT_ID",referencedColumnName="ID"))
    private Set<Format> formats;
    @Column(name="STATUS",length=32,nullable=false)
    @Enumerated(EnumType.STRING)
    private Status status;
    @Column(name="UNFILLED_ACTION",length=32,nullable=false)
    @Enumerated(EnumType.STRING)
    private UnfilledAction unfilledAction;
    @Column(name="BACKFILL_ENABLED",nullable=false)
    private boolean backfillEnabled;
    @Column(name="COLOR_SCHEME",length=32,nullable=true)
    @Enumerated(EnumType.STRING)
    private ColorScheme colorScheme;
    @Column(name="USE_AD_SIGNIFIER",nullable=false)
    private boolean useAdSignifier;
    @ElementCollection(fetch=FetchType.LAZY)
    @CollectionTable(name="AD_SPACE_APPROVED_FEATURE",joinColumns=@JoinColumn(name="AD_SPACE_ID",referencedColumnName="ID"))
    @Column(name="FEATURE",nullable=false)
    @Enumerated(EnumType.STRING)
    private Set<Feature> approvedFeatures;
    @ElementCollection(fetch=FetchType.LAZY)
    @CollectionTable(name="AD_SPACE_DENIED_FEATURE",joinColumns=@JoinColumn(name="AD_SPACE_ID",referencedColumnName="ID"))
    @Column(name="FEATURE",nullable=false)
    @Enumerated(EnumType.STRING)
    private Set<Feature> deniedFeatures;

    {
    this.externalID = UUID.randomUUID().toString();
    this.creationTime = new Date();
    this.formats = new HashSet<Format>();
    this.status = Status.UNVERIFIED;
    this.unfilledAction = UnfilledAction.NO_AD;
    this.colorScheme = ColorScheme.grey;
    this.useAdSignifier = true;
    this.approvedFeatures = new HashSet<Feature>();
    this.deniedFeatures = new HashSet<Feature>();
    }

    AdSpace() {}

    AdSpace(Publication publication) {
    this.publication = publication;
    this.backfillEnabled = publication.isBackfillEnabled();
    }

    public long getId() { return id; };

    public Publication getPublication() { return publication; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getExternalID() { return externalID; }

    public Date getCreationTime() { return creationTime; }

    public Set<Format> getFormats() { return formats; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public UnfilledAction getUnfilledAction() { return unfilledAction; }
    public void setUnfilledAction(UnfilledAction unfilledAction) {
    this.unfilledAction = unfilledAction;
    }

    public boolean isBackfillEnabled() { return backfillEnabled; }
    public void setBackfillEnabled(boolean backfillEnabled) {
    this.backfillEnabled = backfillEnabled;
    }

    public ColorScheme getColorScheme() { return colorScheme; }
    public void setColorScheme(ColorScheme colorScheme) {
    this.colorScheme = colorScheme;
    }

    public boolean getUseAdSignifier() { return useAdSignifier; }
    public void setUseAdSignifier(boolean useAdSignifier) {
    this.useAdSignifier = useAdSignifier;
    }

    public Date getReactivationTime() {
        return reactivationTime;
    }
    public void setReactivationTime(Date reactivationTime) {
        this.reactivationTime = reactivationTime;
    }

    /**
     * Auditing helpers
     */
    public String getFormatsAsString() {
    return NamedUtils.namedCollectionToString(formats);
    }

    public Set<Feature> getApprovedFeatures() {
        return approvedFeatures;
    }

    public Set<Feature> getDeniedFeatures() {
        return deniedFeatures;
    }

    public boolean isFeatureApproved(Feature feature) {
        return !deniedFeatures.contains(feature) && (approvedFeatures.isEmpty() || approvedFeatures.contains(feature));
    }
}
