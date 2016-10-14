package com.adfonic.domain.cache.service;

import java.util.Map;

import com.adfonic.domain.cache.dto.adserver.adspace.AdSpaceDto;

public interface RtbCacheService extends BaseCache {

    void addRtbPublicationAdSpace(AdSpaceDto adspace);

    Map<String, AdSpaceDto> getPublisherRtbAdSpacesMap(Long publisherId);

    AdSpaceDto getAdSpaceByPublicationRtbId(Long publisherId, String publicationRtbId);

    boolean isRtbEnabled();
}
