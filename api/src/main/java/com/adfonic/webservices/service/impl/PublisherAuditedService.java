package com.adfonic.webservices.service.impl;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.adfonic.adserver.Impression;
import com.adfonic.adserver.MacroTractor;
import com.adfonic.domain.Creative;
import com.adfonic.domain.Creative.Status;
import com.adfonic.domain.Destination;
import com.adfonic.domain.Publisher;
import com.adfonic.domain.PublisherAuditedCreative;
import com.adfonic.webservices.ErrorCode;
import com.adfonic.webservices.exception.ServiceException;
import com.adfonic.webservices.service.IPublisherAuditService;
import com.byyd.middleware.account.service.PublisherManager;
import com.byyd.middleware.creative.service.CreativeManager;

@Service
public class PublisherAuditedService implements IPublisherAuditService {
    private static final transient Logger LOG = Logger.getLogger(PublisherAuditedService.class.getName());

    @Autowired
    private PublisherManager publisherManager;

    @Autowired
    private CreativeManager creativeManager;

    @Override
    public PublisherAuditedCreative getAuditedCreativeAndPublisher(String creativeExternalId, String publisherExternalId) {
        Publisher publisher = publisherManager.getPublisherByExternalId(publisherExternalId);

        if (publisher == null) {
            throw new ServiceException(ErrorCode.ENTITY_NOT_FOUND, "Publisher not found");
        }

        LOG.warning("Found publisher by id: " + publisher.getId());
        Creative creative = creativeManager.getCreativeByExternalId(creativeExternalId);

        if (creative == null) {
            throw new ServiceException(ErrorCode.ENTITY_NOT_FOUND, "Creative not found");
        }

        LOG.warning("Found creative by id: " + creative.getId());
        return publisherManager.getPublisherAuditedCreativeByPublisherAndCreative(publisher, creative);
    }

    @Override
    public void recordAuditorImpression(PublisherAuditedCreative auditedCreative) {
        auditedCreative.setImpressionCount(1 + auditedCreative.getImpressionCount());
        auditedCreative.setLatestImpressionTime(new Date());
    }

    @Override
    public void recordAuditorClick(PublisherAuditedCreative auditedCreative) {
        auditedCreative.setClickCount(1 + auditedCreative.getClickCount());
        auditedCreative.setLatestClickTime(new Date());
    }

    private String warnRedirectorPfx;

    @Override
    public String getRedirectUrl(PublisherAuditedCreative auditedCreative) {
        //TODO - handle proxied destination

        Creative creative = auditedCreative.getCreative();
        if (isDestinationRedirectUnAcceptable(creative)) {
            throw new ServiceException(ErrorCode.INVALID_STATE, "Audit not relevant in current state!");
        }

        Destination destination = creative.getDestination();
        if (destination == null || !destination.isDataIsFinalDestination() && StringUtils.isEmpty(destination.getFinalDestination()) || destination.getData() == null
                || StringUtils.isEmpty(destination.getData())) {

            throw new ServiceException(ErrorCode.INVALID_STATE, "Unknown state!");
        }

        String redirectUrl = MacroTractor.resolveMacros(destination.getData(), "auditpublisherextid", "auditadspaceextid", creative.getExternalID(), creative.getCampaign()
                .getExternalID(), creative.getCampaign().getAdvertiser().getExternalID(), "auditpublicationextid", DUMMY_IMPRESSION, DUMMY_DEVTYPEMAP, Collections.emptyMap(),
                false, null, true, true);

        if (auditedCreative.getStatus() == com.adfonic.domain.PublisherAuditedCreative.Status.LOCAL_INVALID) {
            try {
                redirectUrl = warnRedirectorPfx + URLEncoder.encode(redirectUrl, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                throw new ServiceException(ErrorCode.PROCESSING_ERROR, e.getMessage());
            }
        }

        return redirectUrl;
    }

    private static final Impression DUMMY_IMPRESSION = new Impression();
    @SuppressWarnings("serial")
    private static final Map<String, Long> DUMMY_DEVTYPEMAP = new HashMap<String, Long>() {

        @Override
        public Long get(Object key) {
            return 0L;
        }

        @Override
        public boolean isEmpty() {
            return false;
        }
    };
    static {
        Map<Long, String> fakeDeviceIdentifiers = new HashMap<>();
        fakeDeviceIdentifiers.put(0L, "stuff");
        DUMMY_IMPRESSION.setDeviceIdentifiers(fakeDeviceIdentifiers);
    }

    private boolean isDestinationRedirectUnAcceptable(Creative creative) {
        return creative == null || creative.getStatus() == Status.STOPPED;

    }

}
