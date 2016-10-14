package com.adfonic.domainserializer.loader;

import java.util.Objects;

import com.adfonic.domain.cache.listener.DSRejectionListener;
import com.adfonic.domainserializer.CacheBuildStats;
import com.adfonic.domainserializer.DsShard;

public class AdCacheBuildParams {

    private final DsShard shard;

    private Long debugCampaignId;

    private Long debugCreativeId;

    private Long debugAdSpaceId;

    private Long debugPublicationId;

    private final CacheBuildStats stats;

    private DSRejectionListener eligibilityListener;

    public AdCacheBuildParams(DsShard shard, CacheBuildStats stats) {
        Objects.requireNonNull(shard);
        this.shard = shard;
        this.stats = stats;
    }

    public AdCacheBuildParams(DsShard shard) {
        this(shard, new CacheBuildStats());
    }

    public CacheBuildStats getStats() {
        return stats;
    }

    public DSRejectionListener getEligibilityListener() {
        return eligibilityListener;
    }

    public void setEligibilityListener(DSRejectionListener eligibilityListener) {
        this.eligibilityListener = eligibilityListener;
    }

    public DsShard getShard() {
        return shard;
    }

    public Long getDebugCampaignId() {
        return debugCampaignId;
    }

    public void setDebugCampaignId(Long debugCampaignId) {
        this.debugCampaignId = debugCampaignId;
    }

    public Long getDebugCreativeId() {
        return debugCreativeId;
    }

    public void setDebugCreativeId(Long debugCreativeId) {
        this.debugCreativeId = debugCreativeId;
    }

    public Long getDebugAdSpaceId() {
        return debugAdSpaceId;
    }

    public void setDebugAdSpaceId(Long debugAdSpaceId) {
        this.debugAdSpaceId = debugAdSpaceId;
    }

    public Long getDebugPublicationId() {
        return debugPublicationId;
    }

    public void setDebugPublicationId(Long debugPublicationId) {
        this.debugPublicationId = debugPublicationId;
    }

}
