package com.adfonic.adserver.controller.dbg.dto;

import java.util.Date;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 
 * @author mvanek
 *
 */
public class DbgApplicationDto {

    @JsonProperty("bidding")
    private Boolean bidding;

    @JsonProperty("startedAt")
    private Date startedAt;

    @JsonProperty("snapshotAt")
    private Date snapshotAt;

    @JsonProperty("adserverCache")
    private DbgCacheMetaDto adserverCache;

    @JsonProperty("domainCache")
    private DbgCacheMetaDto domainCache;

    @JsonProperty("buildProperties")
    private Map<String, String> buildProperties;

    @JsonProperty("adserverProperties")
    private Map<String, String> adserverProperties;

    @JsonProperty("dataCacheProperties")
    private Map<String, String> dataCacheProperties;

    public Map<String, String> getBuildProperties() {
        return buildProperties;
    }

    public Boolean getBidding() {
        return bidding;
    }

    public void setBidding(Boolean bid) {
        this.bidding = bid;
    }

    public Date getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(Date started) {
        this.startedAt = started;
    }

    public void setBuildProperties(Map<String, String> build) {
        this.buildProperties = build;
    }

    public Date getSnapshotAt() {
        return snapshotAt;
    }

    public void setSnapshotAt(Date timestamp) {
        this.snapshotAt = timestamp;
    }

    public DbgCacheMetaDto getAdserverCache() {
        return adserverCache;
    }

    public void setAdserverCache(DbgCacheMetaDto adserverCache) {
        this.adserverCache = adserverCache;
    }

    public DbgCacheMetaDto getDomainCache() {
        return domainCache;
    }

    public void setDomainCache(DbgCacheMetaDto domainCache) {
        this.domainCache = domainCache;
    }

    public Map<String, String> getAdserverProperties() {
        return adserverProperties;
    }

    public void setAdserverProperties(Map<String, String> adserverProperties) {
        this.adserverProperties = adserverProperties;
    }

    public Map<String, String> getDataCacheProperties() {
        return dataCacheProperties;
    }

    public void setDataCacheProperties(Map<String, String> dbCacheProperties) {
        this.dataCacheProperties = dbCacheProperties;
    }
}
