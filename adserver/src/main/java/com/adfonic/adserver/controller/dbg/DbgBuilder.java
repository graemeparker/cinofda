package com.adfonic.adserver.controller.dbg;

import java.io.File;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Logger;

import com.adfonic.adserver.controller.dbg.dto.DbgAdCacheMetaDto;
import com.adfonic.adserver.controller.dbg.dto.DbgCacheMetaDto;
import com.adfonic.domain.cache.AdserverDomainCacheManager;
import com.adfonic.domain.cache.DomainCache;
import com.adfonic.domain.cache.DomainCacheManager;
import com.adfonic.domain.cache.dto.adserver.adspace.AdSpaceDto;
import com.adfonic.domain.cache.dto.adserver.adspace.PublisherDto;
import com.adfonic.domain.cache.dto.adserver.creative.AdspaceWeightedCreative;
import com.adfonic.domain.cache.ext.AdserverDomainCache;
import com.adfonic.domain.cache.ext.AdserverDomainCacheImpl;
import com.adfonic.domain.cache.service.AdSpaceServiceImpl;

/**
 * 
 * @author mvanek
 *
 */
public class DbgBuilder {

    private static final Logger log = Logger.getLogger(DbgBuilder.class.getName());

    public static DbgCacheMetaDto getCacheMetaData(DomainCacheManager cacheManager) {
        File adsCacheFile = cacheManager.getLoadedCacheFile();
        //Date adsFileDate = getCacheSerializationDate(adsCacheFile);
        DomainCache cache = cacheManager.getCache();
        Date populationStartedAt = cache.getPopulationStartedAt();
        Date serializationStartedAt = cache.getSerializationStartedAt();
        Date deserializationStartedAt = cache.getDeserializationStartedAt();
        Date deserializationFinishedAt = cache.getDeserializationFinishedAt();
        return new DbgCacheMetaDto(cacheManager.getLastCheckAt(), adsCacheFile, populationStartedAt, serializationStartedAt, deserializationStartedAt, deserializationFinishedAt);
    }

    public static DbgCacheMetaDto getCacheMetaData(AdserverDomainCacheManager cacheManager) {
        File adsCacheFile = cacheManager.getLoadedCacheFile();
        //Date adsFileDate = getCacheSerializationDate(adsCacheFile);
        AdserverDomainCache cache = cacheManager.getCache();
        Date populationStartedAt = cache.getPopulationStartedAt();
        Date elegibilityStartedAt = cache.getElegibilityStartedAt();
        Date preprocessingStartedAt = cache.getPreprocessingStartedAt();
        Date preprocessingFinishedAt = cache.getPreprocessingFinishedAt();
        Date deserializationStartedAt = cache.getDeserializationStartedAt();
        Date postprocessingStartedAt = cache.getPostprocessingStartedAt();
        Date postprocessingFinishedAt = cache.getPostprocessingFinishedAt();
        DbgAdCacheMetaDto adCacheMeta = new DbgAdCacheMetaDto(cacheManager.getLastCheckAt(), adsCacheFile, populationStartedAt, preprocessingFinishedAt, deserializationStartedAt,
                postprocessingFinishedAt);
        adCacheMeta.setElegibilityStartedAt(elegibilityStartedAt);
        adCacheMeta.setPreprocessingStartedAt(preprocessingStartedAt);
        adCacheMeta.setPostprocessingStartedAt(postprocessingStartedAt);
        return adCacheMeta;
    }

    public static void getEligibleAdSpaces(long creativeId, AdserverDomainCache adCache) {
        Set<Long> adSpaceIds = new HashSet<Long>();

        Map<Long, AdspaceWeightedCreative[]> allEligibleCreatives = adCache.getAllEligibleCreatives();

        //Map<String, AdSpaceDto> publisherRtbAdSpacesMap = adCache.getPublisherRtbAdSpacesMap(id);
        Map<Long, Integer> perPublisherCounts = new HashMap<Long, Integer>();

        for (Entry<Long, AdspaceWeightedCreative[]> entry : allEligibleCreatives.entrySet()) {
            Long adSpaceId = entry.getKey();
            AdspaceWeightedCreative[] weightedCreatives = entry.getValue();
            for (AdspaceWeightedCreative weightedCreative : weightedCreatives) {
                Long[] creativeIds = weightedCreative.getCreativeIds();
                for (Long itemId : creativeIds) {
                    if (itemId.longValue() == creativeId) {
                        adSpaceIds.add(adSpaceId);
                        AdSpaceDto adSpace = adCache.getAdSpaceById(adSpaceId);
                        PublisherDto publisher = adSpace.getPublication().getPublisher();
                        //I'm not sure why are non RTB publishers there, so exclude them...
                        if (publisher.isRtbEnabled()) {
                            Long publisherId = publisher.getId();
                            Integer count = perPublisherCounts.get(publisherId);
                            if (count == null) {
                                count = 1;
                            } else {
                                count = count + 1;
                            }
                            perPublisherCounts.put(publisherId, count);
                        }
                    }
                }
            }
        }
    }

    public static Set<PublisherDto> getAllPublishers(AdserverDomainCache adCache) {
        try {
            Field fAdSpaceService = AdserverDomainCacheImpl.class.getDeclaredField("adSpaceService");
            fAdSpaceService.setAccessible(true);
            Object adSpaceService = fAdSpaceService.get(adCache);

            Field fPublisherByExternalId = AdSpaceServiceImpl.class.getDeclaredField("publisherByExternalId");
            fPublisherByExternalId.setAccessible(true);
            Map<String, Long> publishersByExternalId = (Map<String, Long>) fPublisherByExternalId.get(adSpaceService);

            Set<PublisherDto> publishers = new HashSet<PublisherDto>();
            for (Long publisherId : publishersByExternalId.values()) {
                Map<String, AdSpaceDto> adSpacesMap = adCache.getPublisherRtbAdSpacesMap(publisherId);
                if (adSpacesMap != null && !adSpacesMap.isEmpty()) {
                    AdSpaceDto adSpace = adSpacesMap.values().iterator().next();
                    PublisherDto publisher = adSpace.getPublication().getPublisher();
                    publishers.add(publisher);
                }
            }
            return publishers;
        } catch (Exception x) {
            x.printStackTrace();
            return Collections.emptySet();
        }

    }

    /**
     * @return Publishers how have at least on AdSpace inf AdCache
     
    public static Set<PublisherDto> getServingPublishers(AdserverDomainCache adCache) {
        AdSpaceDto[] allAdSpaces = adCache.getAllAdSpaces();
        //Only publishers who have at least one AdSpace in cache
        Set<PublisherDto> exchanges = new HashSet<PublisherDto>();
        for (AdSpaceDto adSpace : allAdSpaces) {
            exchanges.add(adSpace.getPublication().getPublisher());
        }
        return exchanges;
    }
    */

}
