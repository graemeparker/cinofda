package com.adfonic.domain.cache;

import com.adfonic.domain.cache.dto.datacollector.campaign.CampaignDto;
import com.adfonic.domain.cache.dto.datacollector.publication.PublicationDto;

/** Cached representation of the domain as datacollector needs access to it */
public interface DataCollectorDomainCache extends SerializableCache {

    CampaignDto getCampaignById(long id);

    PublicationDto getPublicationById(long id);
}
