package com.adfonic.domain;

import java.util.Date;
import javax.persistence.*;

/**
 * A notification flag indicates that a given condition has triggered
 * a user notification, so the same condition should not generate further
 * events (at least not until the expiration time comes around).
 */
@Entity
@DiscriminatorValue(value="CAMPAIGN")
public class CampaignNotificationFlag extends NotificationFlag {
    private static final long serialVersionUID = 1L;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="ADVERTISER_ID",nullable=false)
    private Advertiser advertiser;
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="CAMPAIGN_ID",nullable=false)
    private Campaign campaign;

    CampaignNotificationFlag() {}
    
    public CampaignNotificationFlag(Campaign campaign, Type type, int ttlSeconds) {
        super(campaign.getAdvertiser().getCompany(), type, ttlSeconds);
        this.advertiser = campaign.getAdvertiser();
        this.campaign = campaign;
    }

    public CampaignNotificationFlag(Campaign campaign, Type type, Date expirationDate) {
        super(campaign.getAdvertiser().getCompany(), type, expirationDate);
        this.advertiser = campaign.getAdvertiser();
        this.campaign = campaign;
    }

    public Advertiser getAdvertiser() {
        return advertiser;
    }

    public Campaign getCampaign() {
        return campaign;
    }
}
