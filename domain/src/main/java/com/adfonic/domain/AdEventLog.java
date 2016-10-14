package com.adfonic.domain;

import java.math.BigDecimal;
import java.util.Date;
import javax.persistence.*;

/**
 * Slightly denormalized object for core reporting purposes.
 * This class internally manages all fields but does not expose the
 * denormalized ones as bean methods.  For example, the campaign field
 * is set whenever the caller calls setCreative().
 */
@Entity
@Table(name="AD_EVENT_LOG")
public class AdEventLog extends BusinessKey {
    private static final long serialVersionUID = 1L;

    @Id @GeneratedValue @Column(name="ID")
    private long id;
    @Column(name="EVENT_TIME",nullable=false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date eventTime;
    @Column(name="PUBLISHER_TIME",nullable=false)
    private Integer publisherTime; // YYYYMMDDHH of eventTime in publisher time zone
    @Column(name="ADVERTISER_TIME",nullable=true)
    private Integer advertiserTime; // YYYYMMDDHH of eventTime in advertiser time zone, nullable
    @Column(name="AD_ACTION",nullable=false)
    @Enumerated(EnumType.STRING)
    private AdAction adAction;
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="CREATIVE_ID",nullable=true)
    private Creative creative; // nullable
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="CAMPAIGN_ID",nullable=true)
    private Campaign campaign; // == creative.campaign, nullable
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="AD_SPACE_ID",nullable=false)
    private AdSpace adSpace;
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="PUBLICATION_ID",nullable=false)
    private Publication publication; // == adSpace.publication
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="MODEL_ID",nullable=true)
    private Model model;
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="COUNTRY_ID",nullable=true)
    private Country country;
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="OPERATOR_ID",nullable=true)
    private Operator operator;
    @Column(name="AGE_LOW",nullable=true)
    private Integer ageLow;
    @Column(name="AGE_HIGH",nullable=true)
    private Integer ageHigh;
    @Column(name="GENDER",nullable=true)
    @Enumerated(EnumType.STRING)
    private Gender gender;
    @Column(name="COST",nullable=true)
    private BigDecimal cost;
    @Column(name="ADVERTISER_VAT",nullable=true)
    private BigDecimal advertiserVAT;
    @Column(name="PAYOUT",nullable=true)
    private BigDecimal payout;
    @Column(name="PUBLISHER_VAT",nullable=true)
    private BigDecimal publisherVAT;
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="USER_AGENT_ID",nullable=true)
    private UserAgent userAgent;
    @Column(name="BACKFILLED",nullable=false)
    private boolean backfilled;
    @Column(name="LATITUDE",nullable=true)
    private Double latitude;
    @Column(name="LONGITUDE",nullable=true)
    private Double longitude;
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="GEOTARGET_ID",nullable=true)
    private Geotarget geotarget;
    @Column(name="IP_ADDRESS",length=16,nullable=false)
    private String ipAddress;
    @Column(name="TRACKING_IDENTIFIER",length=255,nullable=true)
    private String trackingIdentifier;
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="INTEGRATION_TYPE_ID",nullable=true)
    private IntegrationType integrationType;

    public long getId() { return id; };
    
    public Date getEventTime() { return eventTime; }
    
    public AdAction getAdAction() { return adAction; }

    public Creative getCreative() { return creative; }

    public AdSpace getAdSpace() { return adSpace; }

    public Publication getPublication() { return publication; }

    public Model getModel() { return model; }

    public Country getCountry() { return country; }

    public Operator getOperator() { return operator; }

    public Integer getAgeLow() { return ageLow; }

    public Integer getAgeHigh() { return ageHigh; }

    public Gender getGender() { return gender; }

    public Integer getAdvertiserTime() { return advertiserTime; }
    
    public Integer getPublisherTime() { return publisherTime; }
    
    public UserAgent getUserAgent() { return userAgent; }

    public boolean wasBackfilled() { return backfilled; }

    public Double getLatitude() { return latitude; }
    
    public Double getLongitude() { return longitude; }

    public Geotarget getGeotarget() { return geotarget; }

    public String getIpAddress() { return ipAddress; }

    public String getTrackingIdentifier() { return trackingIdentifier; }

    public IntegrationType getIntegrationType() { return integrationType; }
}
