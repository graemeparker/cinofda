package com.adfonic.domain;

import java.math.BigDecimal;
import javax.persistence.*;

/** This object is automatically inserted by a db trigger when a Campaign
    gets created.  It's updated by the UPDATE_BUDGETS stored proc.  In other
    words, don't go creating these, and don't go updating these.  They're
    really just read-only hanging off a Campaign.
*/
@Entity
@Table(name="CAMPAIGN_OVERALL_SPEND")
public class CampaignOverallSpend extends BusinessKey {
    private static final long serialVersionUID = 1L;

    @Id @GeneratedValue @Column(name="ID")
    private long id;
    // Careful here. Even tho the relationship is marked as LAZY, OneToOne lazies are not supported by Hibernate.
    // The object will always be hydrated.
    @OneToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="CAMPAIGN_ID",nullable=false)
    private Campaign campaign;
    @Column(name="BUDGET",nullable=true)
    private BigDecimal budget;
    @Column(name="AMOUNT",nullable=false)
    private BigDecimal amount;

    public long getId() { return id; };

    public Campaign getCampaign() { return campaign; }
    public void setCampaign(Campaign campaign) {
        this.campaign = campaign;
    }

    public BigDecimal getBudget() { return budget; }
    public void setBudget(BigDecimal budget) {
        this.budget = budget;
    }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}
