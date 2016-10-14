package com.adfonic.domain.cache.service;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.adfonic.domain.cache.dto.adserver.CountryDto;
import com.adfonic.domain.cache.dto.adserver.creative.AdspaceWeightedCreative;
import com.adfonic.domain.cache.dto.adserver.creative.CreativeDto;
import com.adfonic.domain.cache.dto.adserver.creative.PluginCreativeInfo;
import com.adfonic.util.Subnet;

public interface CreativeService extends BaseCache {

    void addCreativeToCache(CreativeDto creative);

    CreativeDto getCreativeByExternalID(String externalID);

    CreativeDto getCreativeById(Long id);

    CreativeDto[] getAllCreatives();

    CreativeDto[] getPluginCreatives();

    void addAdSpaceEligibleCreative(Long adSpaceId, Set<AdspaceWeightedCreative> list, List<CountryDto> allCountries);

    AdspaceWeightedCreative[] getEligibleCreatives(Long adSpaceId);

    Set<Long> getEligibleCreativeIdsForCountry(Long countryId);

    PluginCreativeInfo getPluginCreativeInfo(CreativeDto creative);

    PluginCreativeInfo getPluginCreativeInfo(Long creativeId);

    void addRecentlyStoppedCreative(CreativeDto creative);

    CreativeDto getRecentlyStoppedCreativeById(Long id);

    void stopCampaign(Long campaignID);

    void stopAdvertiser(Long advertiserId);

    void addSegmentSubnets(Long segmentId, Set<Subnet> subnets);

    Set<Subnet> getSubnetsBySegmentId(Long segmentId);

    Map<Long, AdspaceWeightedCreative[]> getAllEligibleCreatives();

}
