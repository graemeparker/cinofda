package com.adfonic.adserver.controller.dbg.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.adfonic.adserver.impl.LocalBudgetManagerCassandra.AdserverBudget;
import com.adfonic.domain.cache.dto.adserver.creative.CampaignDto;
import com.adfonic.domain.cache.dto.adserver.creative.CreativeDto;

public class DbgCampaignDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private CampaignDto campaign;

    private List<CreativeDto> creatives = new ArrayList<CreativeDto>();

    private AdserverBudget budget;

    private DbgStoppageDto campaignStoppage;

    private DbgStoppageDto advertiserStoppage;

    public CampaignDto getCampaign() {
        return campaign;
    }

    public void setCampaign(CampaignDto campaign) {
        this.campaign = campaign;
    }

    public List<CreativeDto> getCreatives() {
        return creatives;
    }

    public void setCreatives(List<CreativeDto> creatives) {
        this.creatives = creatives;
    }

    public void setBudget(AdserverBudget budget) {
        this.budget = budget;
    }

    public AdserverBudget getBudget() {
        return budget;
    }

    public DbgStoppageDto getCampaignStoppage() {
        return campaignStoppage;
    }

    public void setCampaignStoppage(DbgStoppageDto campaignStoppage) {
        this.campaignStoppage = campaignStoppage;
    }

    public DbgStoppageDto getAdvertiserStoppage() {
        return advertiserStoppage;
    }

    public void setAdvertiserStoppage(DbgStoppageDto advertiserStoppage) {
        this.advertiserStoppage = advertiserStoppage;
    }

}
