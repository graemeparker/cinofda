package com.adfonic.domainserializer.xaudit;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.adfonic.domain.PublisherAuditedCreative.Status;
import com.adfonic.domain.cache.dto.adserver.adspace.AdSpaceDto;
import com.adfonic.domain.cache.dto.adserver.creative.CreativeDto;
import com.adfonic.domainserializer.loader.AdCacheBuildContext;
import com.adfonic.domainserializer.loader.AdCacheBuildContext.PublisherAuditedCreativeValue;

/**
 * This class is to filter the creatives that require external auditing.
 * 
 * @author graemeparker
 *
 */
public class AuditEligibilityCheckImpl implements AuditEligibilityCheck {

    // List of publishers that require their creative to be audited. For AppNexus see ASSOCIATED_PUBLISHERS table.
    private final Set<Long> auditingPublishers;

    // List of publishers that are allowed to audit creatives with AppNexus (creative allow audit flag still relevant)
    private final Set<Long> appNexusAuditPublishers;

    // Service to creative/sync/update the creative via JMS messages to tasks.
    private final AuditCheckJmsSender auditNotifier;

    /*
        // Short time before next message, fall-back is 2 hours
        @Value("${appnxs.creative.expirygap.minutes:7200000}")
        private long expiryGapMinutes;

        // Larger time before next message, fall-back is 45 days
        @Value("${appnxs.creative.expirygap.days:3888000000}")
        private long expiryGapDays;

        // Should get a response within 20mins
        @Value("${adx.creative.expirygap.minutes:28800000}")
        private long adXPendingGap;

        // Hack: For adx if we can't creative (keep running out of quota)
        // Move to config file.
        private final long adxPublisherId;

        // Next possible run for a single creative in case combined can't consume 
        // quick enough..
        private int dsRunLengthMins = 45;

        // List of the audited creative status to ignore.
        private static final Set<Status> IGNORABLE_STATUSES = EnumSet.of(//Status.CREATION_INITIATED, 
                Status.REJECTED, //
                Status.UNAUDITABLE,// 
                Status.INTERNALLY_INELIGIBLE,// 
                Status.MISC_UNMAPPED);

        private static final Set<Status> BYPASS_STATUSES = EnumSet.of(//
                Status.BYPASS_ALLOW_CACHE_ONLY,//
                Status.BYPASS_ALLOW_AUDIT_ONLY,//
                Status.BYPASS_ALLOW_CACHE_AND_AUDIT);
    */
    public AuditEligibilityCheckImpl(Set<Long> auditingPublishers, Set<Long> appNexusAllowAuditPublishers, AuditCheckJmsSender notifier) {
        this.auditingPublishers = auditingPublishers;
        this.appNexusAuditPublishers = appNexusAllowAuditPublishers;
        this.auditNotifier = notifier;
    }

    /**
     * Called from eligibility check for every adspace & creative combination, which means that it will be called many times for same creative & publisher.
     * But book-keeping PublisherAuditedCreativeValue will prevent from duplicate processing
     */
    @Override
    public boolean isEligible(CreativeDto creative, AdSpaceDto adSpace, AdCacheBuildContext td) {

        long creativeId = creative.getId();
        long publisherId = adSpace.getPublication().getPublisher().getOperatingPublisherId();

        if (!auditingPublishers.contains(publisherId)) {
            return true; // publisher does not require audit
        }

        // Only for Appnexus - MAX-216: AppNexus - Stop Submission Per Creative
        boolean isForAppNexus = appNexusAuditPublishers.contains(publisherId);
        if (!creative.isAllowExternalAudit() && isForAppNexus) { // MAD-1643 bypass 'allow audit flag' for AdX
            return false;
        }

        Map<Long, PublisherAuditedCreativeValue> publisher2auditedCreative = td.creative2PublisherAudited.get(creativeId);
        if (publisher2auditedCreative == null) { // creative is new
            td.creative2PublisherAudited.put(creativeId, publisher2auditedCreative = new HashMap<>());
        }

        boolean eligible;
        PublisherAuditedCreativeValue auditedCreative = publisher2auditedCreative.get(publisherId);
        if (auditedCreative == null) { // creative not audited by this publisher 
            auditedCreative = new PublisherAuditedCreativeValue(Status.CREATION_INITIATED, null, null);
            publisher2auditedCreative.put(publisherId, auditedCreative);
            // new creative - send it to tasks for audit submission
            auditNotifier.syncExternalCreative(creativeId, publisherId);
            eligible = false;
        } else if (Status.ACTIVE.equals(auditedCreative.getStatus())) {
            eligible = true; // yes yes yes
        } else {
            switch (auditedCreative.getStatus()) {
            case BYPASS_ALLOW_CACHE_ONLY:
                eligible = true;
                break;
            case BYPASS_ALLOW_AUDIT_ONLY:
                auditNotifier.syncExternalCreative(creativeId, publisherId);
                eligible = false;
                break;
            case BYPASS_ALLOW_CACHE_AND_AUDIT:
                auditNotifier.syncExternalCreative(creativeId, publisherId);
                eligible = true;
                break;
            default:
                eligible = false;
                break;
            }
        }
        // Not necessary but helps debugging on adserver why creative is not bidded on some exchage
        creative.getExtendedData().put("xaudit-" + publisherId, auditedCreative.getStatus().toString());
        return eligible;

    }

}
