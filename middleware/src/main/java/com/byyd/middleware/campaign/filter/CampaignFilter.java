package com.byyd.middleware.campaign.filter;

import java.util.Collection;
import java.util.Date;

import com.adfonic.domain.Advertiser;
import com.adfonic.domain.Campaign;
import com.adfonic.domain.PublicationList;
import com.adfonic.util.Range;

public class CampaignFilter {

    private Collection<Long> campaignIds;
    private Advertiser advertiser;
    private Collection<Campaign.Status> statuses;
    private Range<Date> dateRangeForActive;
    private Boolean houseAds;
    private String name;
    private String containsName;
    private boolean nameCaseSensitive;
    private Collection<Long> excludedIds;
    private boolean nameWithPreviousSpace;
    private PublicationList publicationList;
    

    public Collection<Long> getCampaignIds() {
        return campaignIds;
    }
    public CampaignFilter setCampaignIds(Collection<Long> campaignIds) {
        this.campaignIds = campaignIds;
        return this;
    }

    public Advertiser getAdvertiser() {
        return advertiser;
    }
    public CampaignFilter setAdvertiser(Advertiser advertiser) {
        this.advertiser = advertiser;
        return this;
    }

    public Collection<Campaign.Status> getStatuses() {
        return statuses;
    }
    public CampaignFilter setStatuses(Collection<Campaign.Status> statuses) {
        this.statuses = statuses;
        return this;
    }
    
    public Range<Date> getDateRangeForActive() {
        return dateRangeForActive;
    }
    public CampaignFilter setDateRangeForActive(Range<Date> dateRangeForActive) {
        this.dateRangeForActive = dateRangeForActive;
        return this;
    }
    
    public Boolean getHouseAds() {
        return houseAds;
    }
    public CampaignFilter setHouseAds(Boolean houseAds) {
        this.houseAds = houseAds;
        return this;
    }

    public String getName() {
        return name;
    }
    public CampaignFilter setName(String name, boolean caseSensitive) {
        this.name = name;
        this.nameCaseSensitive = caseSensitive;
        return this;
    }

    public boolean isNameCaseSensitive() {
        return nameCaseSensitive;
    }
    public CampaignFilter setNameCaseSensitive(boolean nameCaseSensitive) {
        this.nameCaseSensitive = nameCaseSensitive;
        return this;
    }

    public Collection<Long> getExcludedIds() {
        return excludedIds;
    }
    public CampaignFilter setExcludedIds(Collection<Long> excludedIds) {
        this.excludedIds = excludedIds;
        return this;
    }
    public String getContainsName() {
        return containsName;
    }
    public CampaignFilter setContainsName(String containsName) {
        this.containsName = containsName;
           return this;
    }
    public boolean isNameWithPreviousSpace() {
        return nameWithPreviousSpace;
    }
    public CampaignFilter setNameWithPreviousSpace(boolean nameWithPreviousSpace) {
        this.nameWithPreviousSpace = nameWithPreviousSpace;
        return this;
    }
    public PublicationList getPublicationList() {
        return publicationList;
    }
    public CampaignFilter setPublicationList(PublicationList publicationList) {
        this.publicationList = publicationList;
        return this;
    }
    
    
}
