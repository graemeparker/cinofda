package com.byyd.middleware.audience.filter;

import com.adfonic.domain.Audience;
import com.adfonic.domain.Campaign;

public class CampaignAudienceFilter {

    private Audience audience;
    private Boolean include;
    private Boolean deleted;
    private Campaign campaign;
    
    public Audience getAudience() {
        return audience;
    }
    public CampaignAudienceFilter setAudience(Audience audience) {
        this.audience = audience;
        return this;
    }
    public Boolean getInclude() {
        return include;
    }
    public CampaignAudienceFilter setInclude(Boolean include) {
        this.include = include;
        return this;
    }
    public Boolean getDeleted() {
        return deleted;
    }
    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }
    public Campaign getCampaign() {
        return campaign;
    }
    public void setCampaign(Campaign campaign) {
        this.campaign = campaign;
    }
    
}
