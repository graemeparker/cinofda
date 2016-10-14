package com.byyd.middleware.campaign.filter;

import java.util.Date;

import com.adfonic.domain.Campaign;

public class CampaignDataFeeFilter {

    private Campaign campaign;
    private Date time; 
    
    public Campaign getCampaign() {
        return campaign;
    }
    public void setCampaign(Campaign campaign) {
        this.campaign = campaign;
    }
    public Date getTime() {
        return time;
    }
    public void setTime(Date time) {
        this.time = (time == null ? null : new Date(time.getTime()));
    }
    
}
