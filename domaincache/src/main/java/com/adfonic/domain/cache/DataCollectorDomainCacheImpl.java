package com.adfonic.domain.cache;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.adfonic.domain.cache.dto.datacollector.campaign.CampaignDto;
import com.adfonic.domain.cache.dto.datacollector.publication.PublicationDto;

/** Cached representation of the domain as datacollector needs access to it */
public class DataCollectorDomainCacheImpl implements DataCollectorDomainCache {

    private static final long serialVersionUID = 7L;

    // These fields are package access so that the DataCollectorDomainSerializer can
    // populate them most easily.

    final Map<Long, CampaignDto> campaignsById = new HashMap<Long, CampaignDto>();
    final Map<Long, PublicationDto> publicationsById = new HashMap<Long, PublicationDto>();

    @Override
    public CampaignDto getCampaignById(long id) {
        return campaignsById.get(id);
    }

    @Override
    public PublicationDto getPublicationById(long id) {
        return publicationsById.get(id);
    }

    @Override
    public void logCounts(String description, Logger logger, Level level) {
        if (logger.isLoggable(level)) {
            logger.log(level, "Cache counts: Campaigns=" + campaignsById.size() + ", Publications=" + publicationsById.size());
        }
    }
}
