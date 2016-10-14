package com.adfonic.adserver;

import java.util.Map;

import com.adfonic.domain.cache.dto.adserver.creative.AdvertiserDto;
import com.adfonic.domain.cache.dto.adserver.creative.CampaignDto;
import com.adfonic.domain.cache.dto.adserver.creative.CreativeDto;

public interface StoppageManager {
    /**
     * Get all Advertiser stoppages
     * @return map of Stoppage objects by Advertiser id
     */
    Map<Long,Stoppage> getAdvertiserStoppages();

    /**
     * Get all Campaign stoppages
     * @return map of Stoppage objects by Campaign id
     */
    Map<Long,Stoppage> getCampaignStoppages();

    /**
     * Is a given Creative stopped?
     * @return true if the Creative is stopped
     */
    boolean isCreativeStopped(CreativeDto creative);

    /**
     * Is a given Campaign stopped?
     * @return true if the Campaign is stopped
     */
    boolean isCampaignStopped(CampaignDto campaign);

    /**
     * Is a given Advertiser stopped?
     * @return true if the Advertiser is stopped
     */
    boolean isAdvertiserStopped(AdvertiserDto advertiser);
}
