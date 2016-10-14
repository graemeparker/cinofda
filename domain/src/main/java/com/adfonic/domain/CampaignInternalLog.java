package com.adfonic.domain;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@Table(name="campaigns_for_internal_log")
public class CampaignInternalLog extends BusinessKey{
    
    private static final long serialVersionUID = 1L;

    @Id 
    @GeneratedValue 
    @Column(name="campaigns_for_internal_log_id")
    private long id;
    
    @ManyToOne(fetch=FetchType.EAGER)
    @JoinColumn(name="campaign_id",nullable=false)
    private Campaign campaign;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = true)
    private Company company;
    
    @Column(name="log_now_flag", nullable=false)
    private boolean lldEnabled;
    
    @Column(name="created_at",nullable=true)
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;

    @Column(name="updated_at",nullable=true)
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedAt;
    
    public CampaignInternalLog(){
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Campaign getCampaign() {
        return campaign;
    }

    public void setCampaign(Campaign campaign) {
        this.campaign = campaign;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public boolean isLldEnabled() {
        return lldEnabled;
    }

    public void setLldEnabled(boolean lldEnabled) {
        this.lldEnabled = lldEnabled;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }
}
