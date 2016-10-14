package com.adfonic.domain;

import java.util.Date;
import javax.persistence.*;

@Entity
@Table(name="CAMPAIGN_STOPPAGE")
public class CampaignStoppage extends BusinessKey {
    private static final long serialVersionUID = 1L;

    @Id @GeneratedValue @Column(name="ID")
    private long id;
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="CAMPAIGN_ID",nullable=false)
    private Campaign campaign;
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
    	HOURLY_BUDGET,
        DAILY_BUDGET,
        OVERALL_BUDGET
    }

    CampaignStoppage() {}
    
    public CampaignStoppage(Campaign campaign,
                            Reason reason,
                            Date reactivateDate)
    {
	this.campaign = campaign;
        this.reason = reason;
        this.reactivateDate = (reactivateDate == null) ? null : new Date(reactivateDate.getTime());
    }

    public long getId() { return id; };
    
    public Campaign getCampaign() { return campaign; }
    public Reason getReason() { return reason; }
    public Date getTimestamp() { return timestamp; }
    public Date getReactivateDate() { return reactivateDate; }
}
