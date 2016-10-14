package com.adfonic.tasks.combined.consumers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adfonic.jms.CreativeApprovalMessage;
import com.adfonic.tasks.xaudit.adx.AdXAuditService;
import com.adfonic.tasks.xaudit.appnxs.AppNexusAuditService;

public class PublisherCreativeHandler {

    private final transient Logger LOG = LoggerFactory.getLogger(getClass().getName());

    private final AppNexusAuditService appNexusService;

    private final AdXAuditService adxService;

    public PublisherCreativeHandler(AppNexusAuditService appNexusService, AdXAuditService adxService) {
        this.appNexusService = appNexusService;
        this.adxService = adxService;
    }

    /**
     * JMS message handler
     */
    public void onCreativeApprovalNotification(CreativeApprovalMessage message) {
        LOG.info("Received : " + message);
        Long publisherId = message.getPublisherId();
        if (publisherId != null) {
            if (appNexusService.getAnxPublishersIds().contains(publisherId)) {
                appNexusService.onCreate(message.getCreativeId(), publisherId);
            } else if (adxService.getPublisherId() == publisherId) {
                adxService.onNewCreative(message.getCreativeId());
            } else {
                LOG.error("No audit service for " + message);
            }
        }
    }

}
