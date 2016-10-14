package com.adfonic.domain;

import java.util.Date;
import javax.persistence.*;

@Entity
@Table(name="ADVERTISER_STOPPAGE")
public class AdvertiserStoppage extends BusinessKey {
    private static final long serialVersionUID = 1L;

    @Id @GeneratedValue @Column(name="ID")
    private long id;
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="ADVERTISER_ID",nullable=false)
    private Advertiser advertiser;
    @Column(name="REASON",length=64,nullable=false)
    @Enumerated(EnumType.STRING)
    private Reason reason;
    @Column(name="TIMESTAMP",nullable=false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date timestamp = new Date();
    @Column(name="REACTIVATE_DATE",nullable=true)
    @Temporal(TemporalType.TIMESTAMP)
    private Date reactivateDate;
    
    public enum Reason {
        DAILY_BUDGET,
        ZERO_BALANCE
    }

    AdvertiserStoppage() {}
    
    public AdvertiserStoppage(Advertiser advertiser, Reason reason, Date reactivateDate) {
        this.advertiser = advertiser;
        this.reason = reason;
        this.reactivateDate = (reactivateDate == null) ? null : new Date(reactivateDate.getTime());
    }

    public long getId() { return id; };
    
    public Advertiser getAdvertiser() { return advertiser; }
    public Reason getReason() { return reason; }
    public Date getTimestamp() { return timestamp; }
    public Date getReactivateDate() { return reactivateDate; }
}
