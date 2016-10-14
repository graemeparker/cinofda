package com.adfonic.adserver;

import java.util.Collections;
import java.util.Map;

import com.adfonic.domain.cache.dto.adserver.creative.AdvertiserDto;
import com.adfonic.domain.cache.dto.adserver.creative.CampaignDto;
import com.adfonic.domain.cache.dto.adserver.creative.CreativeDto;

@SuppressWarnings("unchecked")
public class StubbedStoppageManager implements StoppageManager {
    
	public Map<Long,Stoppage> getAdvertiserStoppages() {
        return Collections.EMPTY_MAP;
    }
    
    public Map<Long,Stoppage> getCampaignStoppages() {
        return Collections.EMPTY_MAP;
    }
    
    public boolean isCreativeStopped(CreativeDto creative) {
        return false;
    }
    
    public boolean isCampaignStopped(CampaignDto campaign) {
        return false;
    }
    
    public boolean isAdvertiserStopped(AdvertiserDto advertiser) {
        return false;
    }
}
