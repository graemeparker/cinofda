package com.adfonic.adserver.rtb;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.adfonic.adserver.AbstractBidManager;
import com.adfonic.adserver.AdEvent;
import com.adfonic.adserver.Impression;
import com.adfonic.adserver.LocalBudgetManager;
import com.adfonic.adserver.TargetingContext;
import com.adfonic.domain.cache.dto.adserver.PlatformDto;

@Component
public class RtbBidManager extends AbstractBidManager<RtbBidDetails> {

    private static final transient Logger LOG = Logger.getLogger(RtbBidManager.class.getName());

    @Autowired
    private LocalBudgetManager localBudgetManager;

    @Autowired
    public RtbBidManager(RtbCacheService rtbCacheService) {
        super(rtbCacheService);
    }

    @Override
    public TargetingContext getTargetingContextFromBidDetails(RtbBidDetails bidDetails) {
        TargetingContext context = super.getTargetingContextFromBidDetails(bidDetails);

        // Restore the PlatformDto derived at bid time...this is required by the
        // MarkupGenerator, and we don't want to leave it to try to derive the
        // PlatformDto from the win notice itself.  That was failing.
        if (bidDetails.getPlatformId() != null) {
            PlatformDto platform = context.getDomainCache().getPlatformById(bidDetails.getPlatformId());
            if (platform != null) {
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.fine("Restored bid time derived PlatformDto id=" + platform.getId());
                }
                context.setAttribute(TargetingContext.PLATFORM, platform);
            } else {
                LOG.warning("PlatformDto not found by id: " + bidDetails.getPlatformId());
            }
        }
        return context;
    }

    @Override
    protected void onBidFailed(RtbBidDetails bidDetails, Impression impression, TargetingContext context, AdEvent bidFailedEvent, String lossReason) {
        getBackupLogger().logRtbLoss(impression, bidFailedEvent.getEventTime(), context, lossReason);
    }

}
