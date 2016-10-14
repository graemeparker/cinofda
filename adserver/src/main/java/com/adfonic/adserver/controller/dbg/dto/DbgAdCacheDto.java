package com.adfonic.adserver.controller.dbg.dto;

import java.util.Set;

/**
 * 
 * @author mvanek
 *
 */
public class DbgAdCacheDto {

    private DbgCacheMetaDto metaData;

    /**
     * Total count of Advertisers
     */
    private Integer adSpacesTotal;
    /**
     * Total count of all Publishers (even without any AdSpace)
     */
    private Integer publishersTotal;
    /**
     * Total count of Creatives
     */
    private Integer creativesTotal;
    /**
     * Total count of Advertisers
     */
    private Integer advertisersTotal;
    /**
     * AdSpaces having some eligible Creative
     */
    private Integer eligibilitiesTotal;

    private Set<Long> creativeIds;

    private Set<Long> campaignIds;

    /**
     * Publishers with at least one AdSpace
     */
    private Set<Long> publisherIds;

    private Set<Long> advertiserIds;

    public DbgCacheMetaDto getMetaData() {
        return metaData;
    }

    public void setMetaData(DbgCacheMetaDto meta) {
        this.metaData = meta;
    }

    public Set<Long> getCreativeIds() {
        return creativeIds;
    }

    public void setCreativeIds(Set<Long> creatives) {
        this.creativeIds = creatives;
    }

    public Set<Long> getCampaignIds() {
        return campaignIds;
    }

    public void setCampaignIds(Set<Long> campaignIds) {
        this.campaignIds = campaignIds;
    }

    public Set<Long> getPublisherIds() {
        return publisherIds;
    }

    public void setPublisherIds(Set<Long> publisherIds) {
        this.publisherIds = publisherIds;
    }

    public Set<Long> getAdvertiserIds() {
        return advertiserIds;
    }

    public void setAdvertiserIds(Set<Long> advertiserIds) {
        this.advertiserIds = advertiserIds;
    }

    public Integer getAdSpacesTotal() {
        return adSpacesTotal;
    }

    public void setAdSpacesTotal(Integer adSpacesTotal) {
        this.adSpacesTotal = adSpacesTotal;
    }

    public Integer getCreativesTotal() {
        return creativesTotal;
    }

    public void setCreativesTotal(Integer creativesTotal) {
        this.creativesTotal = creativesTotal;
    }

    public Integer getPublishersTotal() {
        return publishersTotal;
    }

    public void setPublishersTotal(Integer publishersTotal) {
        this.publishersTotal = publishersTotal;
    }

    public Integer getAdvertisersTotal() {
        return advertisersTotal;
    }

    public void setAdvertisersTotal(Integer advertisersTotal) {
        this.advertisersTotal = advertisersTotal;
    }

    public Integer getEligibilitiesTotal() {
        return eligibilitiesTotal;
    }

    public void setEligibilitiesTotal(Integer eligibilitiesTotal) {
        this.eligibilitiesTotal = eligibilitiesTotal;
    }
}
