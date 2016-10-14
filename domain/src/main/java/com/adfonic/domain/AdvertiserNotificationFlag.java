package com.adfonic.domain;

import java.util.Date;
import javax.persistence.*;

/**
 * A notification flag indicates that a given condition has triggered
 * a user notification, so the same condition should not generate further
 * events (at least not until the expiration time comes around).
 */
@Entity
@DiscriminatorValue(value="ADVERTISER")
public class AdvertiserNotificationFlag extends NotificationFlag {
    private static final long serialVersionUID = 1L;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="ADVERTISER_ID",nullable=false)
    private Advertiser advertiser;

    AdvertiserNotificationFlag() {}
    
    public AdvertiserNotificationFlag(Advertiser advertiser, Type type, int ttlSeconds) {
        super(advertiser.getCompany(), type, ttlSeconds);
        this.advertiser = advertiser;
    }

    public AdvertiserNotificationFlag(Advertiser advertiser, Type type, Date expirationDate) {
        super(advertiser.getCompany(), type, expirationDate);
        this.advertiser = advertiser;
    }

    public Advertiser getAdvertiser() {
        return advertiser;
    }
}
