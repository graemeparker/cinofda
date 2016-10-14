package com.byyd.middleware.audience.filter;

import java.util.Date;

import com.adfonic.domain.CampaignDataFee;

public class AudienceDataFeeFilter {

    private Date time;
    private CampaignDataFee campaignDataFee;
    
    public Date getTime() {
        return time;
    }
    public void setTime(Date time) {
        this.time = (time==null ? null : new Date(time.getTime()));
    }
    public CampaignDataFee getCampaignDataFee() {
        return campaignDataFee;
    }
    public void setCampaignDataFee(CampaignDataFee campaignDataFee) {
        this.campaignDataFee = campaignDataFee;
    }

}
