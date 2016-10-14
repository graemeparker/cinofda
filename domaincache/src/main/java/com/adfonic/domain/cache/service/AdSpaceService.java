package com.adfonic.domain.cache.service;

import java.util.Set;

import com.adfonic.domain.cache.dto.adserver.adspace.AdSpaceDto;

public interface AdSpaceService extends BaseCache {

    /**
     * This function will add an AdSpace to cache, using two keys, id and externalId
     */
    void addAddSpaceToCache(AdSpaceDto adSpace);

    /**
     * get AdSpace by ExternalId
     */
    AdSpaceDto getAdSpaceByExternalID(String externalID);

    /**
     * get AdSpace by id(primary key)
     */
    AdSpaceDto getAdSpaceById(Long id);

    /**
     * get all the active adspaces
     */
    AdSpaceDto[] getAllAdSpaces();

    /**
     * add dormant adspace external id to cache
     */
    void addDormantAdSpaceExternalId(String adSpaceExternalId);

    @Deprecated
    /**
     * Most probably you want to check if given adspace external id is dormant or not.
     * If thats the case use isDormantAdSpace.
     * And if you really want all dormant adspaces then only use it.
     * 
     * @return
     */
    Set<String> getDormantAdSpaceExternalIds();

    /**
     * Check if given external id belongs to dormant adspace 
     */
    boolean isDormantAdSpace(String externalId);

    void addPublicationMayViewPricing(Long publicationId);

    boolean mayPublicationViewPricing(Long publicationId);

    Long getPublisherIdByExternalID(String externalID);

    void addPublisherByExternalId(String publisherExternalId, Long publisherId);

    void addAssociatePublisher(Long id, String associateReference, Long parentId);

    Long getAssociatePublisherID(Long parentId, String associateReference);

}
