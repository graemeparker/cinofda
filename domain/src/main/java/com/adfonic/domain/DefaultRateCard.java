package com.adfonic.domain;

import javax.persistence.*;

/**
 * There is only one DefaultRateCard entry per bid type.
 */
@Entity
@Table(name="DEFAULT_RATE_CARD")
public class DefaultRateCard extends BusinessKey {
    private static final long serialVersionUID = 1L;

    @Id @GeneratedValue @Column(name="ID")
    private long id;
    @Column(name="BID_TYPE",length=16,nullable=false)
    @Enumerated(EnumType.STRING)
    private BidType bidType;
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="RATE_CARD_ID",nullable=false)
    private RateCard rateCard;

    public long getId() { return id; };
    
    public BidType getBidType() { return bidType; }
    public RateCard getRateCard() { return rateCard; }
}
