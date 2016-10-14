package com.adfonic.tasks.xaudit.appnxs;

import java.util.Date;
import java.util.EnumSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;

import com.adfonic.domain.Creative;
import com.adfonic.domain.Publisher;
import com.adfonic.domain.PublisherAuditedCreative;
import com.adfonic.domain.PublisherAuditedCreative.Status;
import com.adfonic.tasks.xaudit.ExternalApprovalService;
import com.adfonic.tasks.xaudit.adx.AdXAuditService;
import com.adfonic.tasks.xaudit.appnxs.dat.AppNexusCreativeRecord;
import com.adfonic.tasks.xaudit.appnxs.dat.AppNexusCreativeRecord.AuditStatus;
import com.adfonic.tasks.xaudit.impl.ExternalApprovalSystem;
import com.adfonic.util.KeyedSynchronizer;
import com.byyd.middleware.account.service.PublisherManager;
import com.byyd.middleware.creative.service.CreativeManager;

public class AppNexusAuditService {

    private static final transient Logger LOG = Logger.getLogger(AppNexusAuditService.class.getName());

    private final KeyedSynchronizer<String> keyedSynchronizer = new KeyedSynchronizer<>();

    @Autowired
    private PublisherManager publisherManager;

    @Autowired
    private CreativeManager creativeManager;

    @Autowired
    @Qualifier("default")
    private ExternalApprovalSystem pubsys;

    // Short time before next message, fall-back is 2 hours
    @Value("${appnxs.creative.expirygap.minutes:7200000}")
    private long expiryGapMinutes;

    // Larger time before next message, fall-back is 45 days
    @Value("${appnxs.creative.expirygap.days:3888000000}")
    private long expiryGapDays;

    // Publisher id which we will audit.
    @Value("${appnxs.allow.audit:34381}")
    private String allowAudit;

    // Publisher id of the exchange
    @Value("${appnxs.exchangeid:34381}")
    private int exchangeid;

    // List of the audited creative status to ignore.
    private static final Set<Status> IGNORABLE_STATUSES = EnumSet.of(Status.CREATION_INITIATED, Status.REJECTED, Status.UNAUDITABLE, Status.INTERNALLY_INELIGIBLE,
            Status.MISC_UNMAPPED);

    private static final Set<Status> BYPASS_STATUSES = EnumSet.of(Status.BYPASS_ALLOW_CACHE_ONLY, Status.BYPASS_ALLOW_AUDIT_ONLY, Status.BYPASS_ALLOW_CACHE_AND_AUDIT);

    private final Set<Long> anxPublishersIds;

    public AppNexusAuditService(Set<Long> anxPublishersIds) {
        this.anxPublishersIds = anxPublishersIds;
    }

    public Set<Long> getAnxPublishersIds() {
        return anxPublishersIds;
    }

    @Transactional(noRollbackFor = Exception.class)
    public void onCreate(long creativeId, long publisherId) {
        String creativeKey = key(creativeId);
        if (keyedSynchronizer.tryAcquire(creativeKey)) {
            try {

                // Only allow audit of know publishers.
                if (allowAudit.contains(String.valueOf(publisherId))) {
                    publisherId = exchangeid;
                } else {
                    LOG.severe("Do not allow audit: " + publisherId);
                    return;
                }

                Publisher publisher = publisherManager.getPublisherById(publisherId, AdXAuditService.PUBLISHER_FETCH_STRATEGY);
                if (publisher == null) {
                    LOG.severe("Publisher not found by: " + publisherId);
                    return;
                }

                Creative creative = creativeManager.getCreativeById(creativeId, AdXAuditService.CREATIVE_FETCH_STRATEGY);
                if (creative == null) {
                    LOG.severe("Creative not found by: " + creativeId);
                    return;
                }

                PublisherAuditedCreative existingAuditedCreative = publisherManager.getPublisherAuditedCreativeByPublisherAndCreative(publisher, creative);
                if (existingAuditedCreative == null) {
                    createNew(creative, publisher);
                    return;
                }

                // Don't sync if we don't have invalid data.
                if (IGNORABLE_STATUSES.contains(existingAuditedCreative.getStatus())) {
                    if (LOG.isLoggable(Level.FINE)) {
                        LOG.fine("PublisherAuditedCreative status " + existingAuditedCreative.getStatus() + " to be ignored. Creative id = " + creative);
                    }
                    return;
                }

                // // Don't sync if we don't have invalid data.
                if (existingAuditedCreative.getLatestFetchTime() == null) {
                    if (LOG.isLoggable(Level.FINE)) {
                        LOG.fine("PublisherAuditedCreative no latest fetch time. Creative id = " + creative);
                    }
                    return;
                }

                /* The following now matches DomainSerializer AuditedCreativesFilterImpl */

                // Get the time we last touched/created/updated the creative.
                long lastTouch = System.currentTimeMillis() - existingAuditedCreative.getLatestFetchTime().getTime();

                switch (existingAuditedCreative.getStatus()) {
                case PENDING:
                    if (lastTouch > expiryGapMinutes) {
                        // Check status after a few minutes.
                        sync(existingAuditedCreative);
                    }
                    break;
                case LOCAL_INVALID:
                    if (lastTouch > expiryGapMinutes) {
                        // Creative changed locally so we need to resubmit.
                        resubmit(existingAuditedCreative);
                    }
                    break;
                case ACTIVE:
                    if (lastTouch > expiryGapDays) {
                        touch(existingAuditedCreative);
                    }
                default:
                    break;
                }

                /* Ending matching code */
            } catch (Exception e1) {
                LOG.log(Level.INFO, "syncExternalCreative ");
                e1.printStackTrace();
            } finally {
                keyedSynchronizer.release(creativeKey);
            }
        }

    }

    /**
     * The creative has been updated locally and needs resubmitting.
     * @param existingAuditedCreative
     */
    private void resubmit(PublisherAuditedCreative existingAuditedCreative) {
        LOG.info("Resubmitting creative " + existingAuditedCreative.getId() + " for publisher " + existingAuditedCreative.getPublisher().getId() + " with AppNexus");
        ExternalApprovalService approvalService = getApprovalService(existingAuditedCreative.getPublisher());
        String blockingRemarks = approvalService.checkForAnyCreativeIncompatibility(existingAuditedCreative.getCreative());
        if (blockingRemarks != null) {
            LOG.info("Ineligible remark on: " + existingAuditedCreative.getCreative().getId() + ". " + blockingRemarks);
            existingAuditedCreative.setStatus(Status.INTERNALLY_INELIGIBLE);
            existingAuditedCreative.setLastAuditRemarks("Internal Remark : " + blockingRemarks);
            updateCreative(existingAuditedCreative);
            return;
        }

        final String externalRef = existingAuditedCreative.getExternalReference();
        approvalService.updateCreative(externalRef, existingAuditedCreative.getCreative(), existingAuditedCreative.getPublisher());

        existingAuditedCreative.setLatestFetchTime(new Date());
        AppNexusCreativeRecord appNexusCreativeRecord = approvalService.getAppNexusCreative(externalRef);

        LOG.info("Status : " + appNexusCreativeRecord.getAudit_status());
        existingAuditedCreative.setStatus(mapFromAppNexusStatus(appNexusCreativeRecord.getAudit_status()));

        // Possible rejection reason
        String feedback = appNexusCreativeRecord.getAudit_feedback();
        if (feedback != null && feedback.length() > 0) {
            LOG.info("Audit : " + feedback);
            existingAuditedCreative.setLastAuditRemarks(feedback);
        }

        updateCreative(existingAuditedCreative);
    }

    /**
     * Status Mapping
     * @param appNxsStatus
     * @return
     */
    private Status mapFromAppNexusStatus(AuditStatus appNxsStatus) {
        switch (appNxsStatus) {
        case no_audit:
            return Status.MISC_UNMAPPED;
        case audited:
            return Status.ACTIVE;
        case pending:
            return Status.PENDING;
        case rejected:
            return Status.REJECTED;
        case unauditable:
            return Status.UNAUDITABLE;
        }
        return Status.MISC_UNMAPPED;
    }

    /**
     * Checking the status by using their external creative id. 
     * @param existingAuditedCreative
     */
    private void sync(PublisherAuditedCreative existingAuditedCreative) {
        ExternalApprovalService approvalService = getApprovalService(existingAuditedCreative.getPublisher());

        // If is no longer a fall back service.
        if (approvalService == null) {
            LOG.severe("Cannot sync approval service for publisher id");
            return;
        }

        final String externalRef = existingAuditedCreative.getExternalReference();
        existingAuditedCreative.setLatestFetchTime(new Date());

        AppNexusCreativeRecord appNexusCreativeRecord = approvalService.getAppNexusCreative(externalRef);

        LOG.info("Status : " + appNexusCreativeRecord.getAudit_status());
        existingAuditedCreative.setStatus(mapFromAppNexusStatus(appNexusCreativeRecord.getAudit_status()));

        // Possible rejection reason
        String feedback = appNexusCreativeRecord.getAudit_feedback();
        if (feedback != null && feedback.length() > 0) {
            LOG.info("Audit : " + feedback);
            existingAuditedCreative.setLastAuditRemarks(feedback);
        }
        updateCreative(existingAuditedCreative);
    }

    /**
     * We need to check the creative status from time to time even once ACTIVE.
     * @param existingAuditedCreative
     */
    private void touch(PublisherAuditedCreative existingAuditedCreative) {
        sync(existingAuditedCreative);
    }

    /**
     * We are about to update an existing creative so update the message counts
     * and save data.
     * @param existingAuditedCreative
     */
    private void updateCreative(PublisherAuditedCreative existingAuditedCreative) {
        incrementMessageCount(existingAuditedCreative);
        publisherManager.update(existingAuditedCreative);
    }

    /**
     * This is where we create the first and only the first creative entry. 
     * Any changes after these are "updates" so keep on eye on message count.
     * @param creative
     * @param publisher
     */
    private void createNew(Creative creative, Publisher publisher) {
        PublisherAuditedCreative auditedCreative = new PublisherAuditedCreative(publisher, creative);
        ExternalApprovalService approvalService = getApprovalService(publisher);

        // If is no longer a fall back service.
        if (approvalService == null) {
            LOG.severe("Cannot create approval service for publisher id : " + publisher.getId());
            return;
        }

        String blockingRemarks = approvalService.checkForAnyCreativeIncompatibility(creative);
        if (blockingRemarks != null) {
            auditedCreative.setStatus(Status.INTERNALLY_INELIGIBLE);
            auditedCreative.setLastAuditRemarks("Internal Remark: " + blockingRemarks);
            publisherManager.create(auditedCreative);
            return;
        }

        // Adding logging as we're not creating records.
        LOG.info("Need to create new audited creative now! creativeId=" + auditedCreative.getCreative().getId() + " publisherId= " + auditedCreative.getPublisher().getId());

        LOG.info("Before create!");
        auditedCreative.setCreationTime(new Date());
        try {
            PublisherAuditedCreative publisherAuditedCreative = publisherManager.create(auditedCreative);
            LOG.info("After create! " + publisherAuditedCreative.toString() + " with status " + publisherAuditedCreative.getStatus().name());
        } catch (RuntimeException re) {
            LOG.warning("Unable to create " + auditedCreative + re.getMessage());
        }

        // Posting to appnexus!
        LOG.info("Before post!");
        String creativeRef = approvalService.newCreative(creative, publisher);
        LOG.info("After post!");

        if (creativeRef == null || creativeRef.length() <= 0) {
            LOG.warning("ExternalReference is null");
        } else {
            LOG.info("ExternalReference " + creativeRef);
        }

        auditedCreative.setExternalReference(creativeRef);
        auditedCreative.setLatestFetchTime(new Date());

        AppNexusCreativeRecord appNexusCreativeRecord = approvalService.getAppNexusCreative(creativeRef);

        LOG.info("Status : " + appNexusCreativeRecord.getAudit_status());
        auditedCreative.setStatus(mapFromAppNexusStatus(appNexusCreativeRecord.getAudit_status()));

        // Possible rejection reason
        String feedback = appNexusCreativeRecord.getAudit_feedback();
        if (feedback != null && feedback.length() > 0) {
            LOG.info("Audit : " + feedback);
            auditedCreative.setLastAuditRemarks(feedback);
        }
        updateCreative(auditedCreative);

        LOG.info("Finished creating and updating creative " + creativeRef);
    }

    /**
     * There is a fallback if the publisher is not found. Watch out for 
     * external ids with DummyExtIdFor.
     * @param publisher
     * @return
     */
    private ExternalApprovalService getApprovalService(Publisher publisher) {
        return pubsys.getService(publisher.getId());
    }

    /**
     * Used for keeping a local key in memory for creative approval.
     * @param creativeId
     * @return
     */
    private String key(long creativeId) {
        return "creappr:" + creativeId;
    }

    /**
     * Keep a counter of everytime we touch this creative. It's a good 
     * indication something is wrong if we keep playing with it.
     * @param existingAuditedCreative
     */
    private void incrementMessageCount(PublisherAuditedCreative existingAuditedCreative) {
        existingAuditedCreative.setMessageCount(existingAuditedCreative.getMessageCount() + 1);
    }
}
