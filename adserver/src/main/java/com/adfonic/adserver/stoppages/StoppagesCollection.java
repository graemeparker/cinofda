package com.adfonic.adserver.stoppages;

import java.util.Map;

import com.adfonic.adserver.Stoppage;
import com.google.common.collect.ImmutableMap;

public class StoppagesCollection {
    private final Map<Long, Stoppage> advertiserStoppages;

    private final Map<Long, Stoppage> campaignStoppages;

    public StoppagesCollection(Map<Long, Stoppage> advertiserStoppages, Map<Long, Stoppage> campaignStoppages) {
        this.advertiserStoppages = ImmutableMap.copyOf( advertiserStoppages);
        this.campaignStoppages = ImmutableMap.copyOf(campaignStoppages);
    }

    public Map<Long, Stoppage> getCampaignStoppages() {
        return campaignStoppages;
    }

    public Map<Long, Stoppage> getAdvertiserStoppages() {
        return advertiserStoppages;
    }
}
