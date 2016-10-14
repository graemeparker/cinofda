package com.adfonic.domain.cache.service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.collections.map.MultiKeyMap;

import com.adfonic.domain.cache.dto.adserver.adspace.AdSpaceDto;

public class AdSpaceServiceImpl implements AdSpaceService {

    private static final long serialVersionUID = 2L;

    public AdSpaceServiceImpl() {

    }

    public AdSpaceServiceImpl(AdSpaceServiceImpl copy) {
        this.adSpaceCache.putAll(copy.adSpaceCache);
        this.adSpaceIdToExternalIdCache.putAll(copy.adSpaceIdToExternalIdCache);
        this.allAdSpaces = Arrays.copyOf(copy.allAdSpaces, copy.allAdSpaces.length);
        this.dormantAdSpaceExternalIds.addAll(copy.dormantAdSpaceExternalIds);
        this.publicationIdsThatMayViewPricing.addAll(copy.publicationIdsThatMayViewPricing);
        this.publisherByExternalId.putAll(copy.publisherByExternalId);
        this.associatePublisherMap.putAll(copy.associatePublisherMap);

    }

    //Holds adspace id as key and adspace as value
    private final Map<Long, AdSpaceDto> adSpaceCache = new HashMap<Long, AdSpaceDto>();

    //Holds adspace externalId as key and adspace Id as value
    private final Map<String, Long> adSpaceIdToExternalIdCache = new HashMap<String, Long>();

    private AdSpaceDto[] allAdSpaces;
    private final Set<String> dormantAdSpaceExternalIds = new HashSet<String>();
    final Set<Long> publicationIdsThatMayViewPricing = new HashSet<Long>();
    final private Map<String, Long> publisherByExternalId = new HashMap<String, Long>();

    private final MultiKeyMap associatePublisherMap = new MultiKeyMap();

    /**
     * This must be called from adserver or anywhere you deserialize cache in memory
     * It will create an array of all values(AdSpaceDto list), which will be returned to
     * everyone who calls  getAllAdSpaces, returning a collection is performance killer as
     * every time it creates a new Iterator, once iterator is created few thing come into picture
     * like synchrozing in case if we ever want to do changes in the map, also GC will have to work more to
     * garbage collect every iterator, which by the way we are not creating explicitly.
     */
    @Override
    public void afterDeserialize() {
        allAdSpaces = adSpaceCache.values().toArray(new AdSpaceDto[adSpaceCache.size()]);
    }

    @Override
    public void beforeSerialization() {
        allAdSpaces = null;
        //Do more memory saving things here

    }

    @Override
    public void addAddSpaceToCache(AdSpaceDto adSpace) {
        adSpaceCache.put(adSpace.getId(), adSpace);
        adSpaceIdToExternalIdCache.put(adSpace.getExternalID(), adSpace.getId());
        publisherByExternalId.put(adSpace.getPublication().getPublisher().getExternalId(), adSpace.getPublication().getPublisher().getId());
    }

    @Override
    public AdSpaceDto getAdSpaceByExternalID(String externalID) {
        Long adSpaceId = adSpaceIdToExternalIdCache.get(externalID);
        return adSpaceCache.get(adSpaceId);
    }

    @Override
    public AdSpaceDto getAdSpaceById(Long adSpaceId) {
        return adSpaceCache.get(adSpaceId);
    }

    @Override
    public void addAssociatePublisher(Long id, String associateReference, Long parentId) {
        associatePublisherMap.put(parentId, associateReference, id);
    }

    @Override
    public Long getAssociatePublisherID(Long parentId, String associateReference) {
        return (Long) associatePublisherMap.get(parentId, associateReference);
    }

    @Override
    public AdSpaceDto[] getAllAdSpaces() {
        if (allAdSpaces == null) {
            //Not thread safe and we dont need it to be.
            //instead u call after desrilaize before u start using this cache/function
            allAdSpaces = adSpaceCache.values().toArray(new AdSpaceDto[adSpaceCache.size()]);
        }
        return allAdSpaces;
    }

    @Override
    public void addDormantAdSpaceExternalId(String adSpaceExternalId) {
        dormantAdSpaceExternalIds.add(adSpaceExternalId);
    }

    @Deprecated
    @Override
    public Set<String> getDormantAdSpaceExternalIds() {
        return dormantAdSpaceExternalIds;
    }

    @Override
    public boolean isDormantAdSpace(String externalId) {
        return dormantAdSpaceExternalIds.contains(externalId);
    }

    @Override
    public void addPublicationMayViewPricing(Long publicationId) {
        publicationIdsThatMayViewPricing.add(publicationId);
    }

    @Override
    public boolean mayPublicationViewPricing(Long publicationId) {
        return publicationIdsThatMayViewPricing.contains(publicationId);
    }

    @Override
    public Long getPublisherIdByExternalID(String externalID) {
        return publisherByExternalId.get(externalID);
    }

    @Override
    public void logCounts(String description, Logger logger, Level level) {
        if (logger.isLoggable(level)) {
            logger.log(level, "Total AdSpaces = " + this.adSpaceCache.size());
            logger.log(level, "Total AdSpaces(ExternalId) = " + this.adSpaceIdToExternalIdCache.size());
            if (this.allAdSpaces != null) {
                logger.log(level, "Total AdSpaces(Array) = " + this.allAdSpaces.length);
            } else {
                logger.log(level, "Total AdSpaces(Array) = 0");
            }

            logger.log(level, "Total Dormant AdSpaces = " + this.dormantAdSpaceExternalIds.size());
            logger.log(level, "Total Publication May View pricing = " + this.publicationIdsThatMayViewPricing.size());
            logger.log(level, "Total Publisher(byExternalId) = " + this.publisherByExternalId.size());
        }
    }

    @Override
    public void addPublisherByExternalId(String publisherExternalId, Long publisherId) {
        publisherByExternalId.put(publisherExternalId, publisherId);
    }

}
