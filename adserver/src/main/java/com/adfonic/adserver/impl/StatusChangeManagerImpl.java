package com.adfonic.adserver.impl;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.http.annotation.NotThreadSafe;

import com.adfonic.adserver.StatusChangeManager;
import com.adfonic.domain.AdSpace;
import com.adfonic.domain.AdSpace.Status;
import com.adfonic.domain.Campaign;
import com.adfonic.domain.Creative;
import com.adfonic.domain.Publication;
import com.adfonic.domain.cache.dto.adserver.adspace.AdSpaceDto;
import com.adfonic.domain.cache.dto.adserver.adspace.PublicationDto;
import com.adfonic.domain.cache.dto.adserver.creative.CampaignDto;
import com.adfonic.domain.cache.dto.adserver.creative.CreativeDto;
import com.adfonic.jms.StatusChangeMessage;

@NotThreadSafe
public class StatusChangeManagerImpl implements StatusChangeManager {

    private static final transient Logger LOG = Logger.getLogger(StatusChangeManagerImpl.class.getName());

    // Use ConcurrentHashMap for these so gets are non-blocking
    private final Map<Long, AdSpace.Status> adSpaceStatusMap = new ConcurrentHashMap<Long, AdSpace.Status>();
    private final Map<Long, Campaign.Status> campaignStatusMap = new ConcurrentHashMap<Long, Campaign.Status>();
    private final Map<Long, Creative.Status> creativeStatusMap = new ConcurrentHashMap<Long, Creative.Status>();
    private final Map<Long, Publication.Status> publicationStatusMap = new ConcurrentHashMap<Long, Publication.Status>();

    /** @{inheritDoc} */
    @Override
    public AdSpace.Status getStatus(AdSpaceDto adSpace) {
        AdSpace.Status status = adSpaceStatusMap.get(adSpace.getId());
        return status != null ? status : adSpace.getStatus();
    }

    /** @{inheritDoc} */
    @Override
    public Campaign.Status getStatus(CampaignDto campaign) {
        Campaign.Status status = campaignStatusMap.get(campaign.getId());
        return status != null ? status : campaign.getStatus();
    }

    /** @{inheritDoc} */
    @Override
    public Creative.Status getStatus(CreativeDto creative) {
        Creative.Status status = creativeStatusMap.get(creative.getId());
        return status != null ? status : creative.getStatus();
    }

    /** @{inheritDoc} */
    @Override
    public Publication.Status getStatus(PublicationDto publication) {
        Publication.Status status = publicationStatusMap.get(publication.getId());
        return status != null ? status : publication.getStatus();
    }

    /**
     * JMS topic consumer for status change messages
     */
    public void onStatusChange(StatusChangeMessage msg) {
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Handling: " + msg);
        }

        if ("AdSpace".equals(msg.getEntityType())) {
            adSpaceStatusMap.put(msg.getEntityId(), AdSpace.Status.valueOf(msg.getNewStatus()));
        } else if ("Campaign".equals(msg.getEntityType())) {
            campaignStatusMap.put(msg.getEntityId(), Campaign.Status.valueOf(msg.getNewStatus()));
        } else if ("Creative".equals(msg.getEntityType())) {
            creativeStatusMap.put(msg.getEntityId(), Creative.Status.valueOf(msg.getNewStatus()));
        } else if ("Publication".equals(msg.getEntityType())) {
            publicationStatusMap.put(msg.getEntityId(), Publication.Status.valueOf(msg.getNewStatus()));
        } else {
            LOG.warning("Unexpected entity type: " + msg);
        }
    }

    @Override
    public Map<Long, Status> getAdSpaceStatusMap() {
        return adSpaceStatusMap;
    }

    @Override
    public Map<Long, com.adfonic.domain.Campaign.Status> getCampaignStatusMap() {
        return campaignStatusMap;
    }

    @Override
    public Map<Long, com.adfonic.domain.Creative.Status> getCreativeStatusMap() {
        return creativeStatusMap;
    }

    @Override
    public Map<Long, com.adfonic.domain.Publication.Status> getPublicationStatusMap() {
        return publicationStatusMap;
    }
}
