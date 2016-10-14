package com.adfonic.adserver;

import java.util.concurrent.DelayQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PreDestroy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;

import com.adfonic.adserver.bidmanager.DelayedBidEntry;
import com.adfonic.adserver.impl.FrequencyCapper;
import com.adfonic.adserver.logging.LoggingUtils;
import com.adfonic.adserver.rtb.util.AdServerStats;
import com.adfonic.domain.AdAction;
import com.adfonic.domain.cache.dto.adserver.adspace.AdSpaceDto;
import com.adfonic.domain.cache.dto.adserver.creative.CreativeDto;

public abstract class AbstractBidManager<T extends BidDetails> {

    private static final transient Logger LOG = Logger.getLogger(AbstractBidManager.class.getName());

    @Autowired
    private AdEventFactory adEventFactory;
    @Autowired
    private BackupLogger backupLogger;
    @Autowired
    private TargetingContextFactory targetingContextFactory;
    @Autowired
    private FrequencyCapper frequencyCapper;
    @Autowired
    private ImpressionService impressionService;
    @Autowired
    private AdServerStats astats;

    // This queue keep bid details, which periodically
    // get flushed in order to log BID_FAILED events.
    private final BidCacheService<T> bidCacheService;

    private final DelayQueue<DelayedBidEntry<T>> delayedBidQueue = new DelayQueue<DelayedBidEntry<T>>();

    /**
     * Constructor
     *
     * @param bidCacheService      the cache service that deals with BidDetails
     */
    protected AbstractBidManager(BidCacheService<T> bidCacheService) {
        this.bidCacheService = bidCacheService;
    }

    /**
     * Save bid details, adding them to the current bid batch.  This
     * caches the bid for win eligibility, and also queues it for
     * automatic failure logging.
     *
     * @param bidDetails the bid details to save
     */
    public void saveBidDetails(T bidDetails, long duration) {
        if (LOG.isLoggable(Level.FINE)) {
            //LOG.fine("Saving bid details");
            LoggingUtils.log(LOG, Level.FINE, null, null, this.getClass(), "saveBidDetails", "Saving bid details");
        }

        // Put the bid details in cache for now
        // The cache key is the Impression externalID
        bidCacheService.saveBidDetails(bidDetails.getImpression().getExternalID(), bidDetails);

        // Add the bid details to the current running batch so we can
        // eventually track bids that didn't win.
        addToCurrentBidBatch(bidDetails, duration);
    }

    /**
     * Get bid details for handling a win notice.  This removes the bid
     * details from cache so it won't be logged as a failed bid along with
     * its batch.
     *
     * @param impressionExternalID the externalID of the respective Impression
     * @return the bid details if found in cache, otherwise null
     */
    public T removeBidDetails(String impressionExternalID) {
        if (LOG.isLoggable(Level.FINE)) {
            //LOG.fine("Getting bid details for win: " + impressionExternalID);
            LoggingUtils.log(LOG, Level.FINE, null, null, this.getClass(), "removeBidDetails", "Removing bid details for win: " + impressionExternalID);
        }
        return bidCacheService.getAndRemoveBidDetails(impressionExternalID);
    }

    /**
     * Add some bid details to the current batch into the bids Queue that will be
     * flushed at some point.  This provides the ability to check back at flush time,
     * to see which bids won and which ones didn't -- so we can log BID_FAILED
     * for those that didn't.
     */
    void addToCurrentBidBatch(T bidDetails, long duration) {
        delayedBidQueue.add(new DelayedBidEntry<T>(bidDetails, duration));
    }

    /**
     * Periodically go through batches of bids and log any that failed (didn't win)
     * @throws Exception 
     */
    @PreDestroy
    @Scheduled(fixedRate = 10000)
    public void logFailedBids() throws Exception {
        if (LOG.isLoggable(Level.FINE)) {
            //LOG.fine("Logging failed bids");
            LoggingUtils.log(LOG, Level.FINE, null, null, this.getClass(), "logFailedBids", "Logging failed bids");
        }

        int totalBidsProcessed = 0;
        int failedBidCount = 0;

        DelayedBidEntry<T> oneDelayedBidEntry;
        while (true) {
            oneDelayedBidEntry = delayedBidQueue.poll();
            if (oneDelayedBidEntry == null) {
                break;
            }
            T bidDetails = oneDelayedBidEntry.getElement();
            ++totalBidsProcessed;
            // See if the bid was won or not.  We do that by checking for it
            // in cache.  If it was won, then we know the win notice handler
            // would have removed it from cache.  So if it's still in cache
            // now, we know it wasn't won.  Determine if it's still there by
            // doing a remove(), which not only gives us the info we need,
            // but it also helps to keep the cache fairly compact.
            if (bidCacheService.removeBidDetails(bidDetails.getImpression().getExternalID())) {
                // Yup, it was still there, log the failed bid
                logFailedBid(bidDetails, "cache_timeout");
                ++failedBidCount;
            }
        }

        if (LOG.isLoggable(Level.FINE)) {
            //LOG.fine("Done logging, found " + failedBidCount + " failed bid(s) out of " + totalBidsProcessed);
            LoggingUtils
                    .log(LOG, Level.FINE, null, null, this.getClass(), "logFailedBids", "Done logging, found " + failedBidCount + " failed bid(s) out of " + totalBidsProcessed);
        }
    }

    /**
     * Log a failed bid.
     * @throws Exception 
     */
    public void logFailedBid(T bidDetails, String lossReason) {
        Impression impression = bidDetails.getImpression();
        astats.loss(impression, lossReason);

        if (LOG.isLoggable(Level.FINE)) {
            //LOG.fine("Logging BID_FAILED for " + impression.getExternalID());
            LoggingUtils.log(LOG, Level.FINE, impression, null, this.getClass(), "logFailedBid", "Logging BID_FAILED for " + impression.getExternalID());
        }

        // Since our batch methodology holds BidRequest objects locally in memory,
        // we've got the original request-time TargetingContext hanging right off it.
        // That will save us derivation time when logging, since derived attributes
        // will already be sitting right there in the context...boosh!
        TargetingContext context = getTargetingContextFromBidDetails(bidDetails);

        AdSpaceDto adSpace = context.getAdserverDomainCache().getAdSpaceById(impression.getAdSpaceId());
        if (adSpace == null) {
            // It must no longer be active
            LOG.warning("Expiration notice impression references unknown AdSpace id=" + impression.getAdSpaceId());
            //LoggingUtils.log(LOG, Level.WARNING, impression, context, this.getClass(), "logFailedBid", "Expiration notice impression references unknown AdSpace id=" + impression.getAdSpaceId());
            return;
        }

        context.setAdSpace(adSpace);

        CreativeDto creative = context.getAdserverDomainCache().getCreativeById(impression.getCreativeId());
        if (creative == null) {
            if ((creative = context.getAdserverDomainCache().getRecentlyStoppedCreativeById(impression.getCreativeId())) != null) {
                if (LOG.isLoggable(Level.INFO)) {
                    LOG.info("Bid failed with recently stopped Creative id=" + creative.getId());
                    //LoggingUtils.log(LOG, Level.INFO, impression, context, this.getClass(), "logFailedBid", "Bid failed with recently stopped Creative id=" + creative.getId());
                }
            } else {
                LOG.warning("Bid failed impression references unknown Creative id=" + impression.getCreativeId());
                //LoggingUtils.log(LOG, Level.WARNING, impression, context, this.getClass(), "logFailedBid", "Bid failed impression references unknown Creative id=" + impression.getCreativeId());
                return;
            }
        }

        // Log the BID_FAILED event
        AdEvent event = adEventFactory.newInstance(AdAction.BID_FAILED, impression.getCreationTime(), impression.getUserTimeZone()); // Technically this is when the bid failed, not now
        context.populateAdEvent(event, impression, creative);
        // TODO Clean up code for JMS decomission
        //        adEventLogger.logAdEvent(event, context);

        // MAD-958
        frequencyCapper.checkAndDecrement(context, creative);

        try {
            impressionService.removeImpression(impression.getExternalID());
        } catch (Exception e) {
            LOG.warning("Unable to remove Impression.ExternalID = " + impression.getExternalID());
        }
        // Let the subclass do something with this if it wants to.
        // i.e., with RTB we need to invoke the BackupLogger.
        onBidFailed(bidDetails, impression, context, event, lossReason);
    }

    /**
     * Override this method if you need to do anything subclass-specific
     * when a bid has been determined to have failed (didn't win).  This
     * will be called just after the BID_FAILED event has been logged.
     */
    protected void onBidFailed(T bidDetails, Impression impression, TargetingContext context, AdEvent bidFailedEvent, String lossReason) {
        // Nothing to do here...subclasses can override
    }

    /**
     * Reconstruct (at least partially) a TargetingContext from BidDetails.
     * This gets called in two places:
     * 1. When logging a failed bid.  In this case, the original bid-time
     * TargetingContext is still sitting right there on the BidDetails,
     * so we just use that.
     * 2. At win notice time.  In this case, we're loading BidDetails from
     * cache, and we can't assume it's in memory (bid was likely served on a
     * different node).  So we have to create a new TargetingContext and just
     * populate it as best we can with the details available.
     * I say "partially" above, since in case #2 the context won't have its
     * full set of already-derived stuff in it.  And one piece we specifically
     * don't address in this method is the DisplayType.  That gets re-populated
     * in a separate step at win notice time.
     */
    public TargetingContext getTargetingContextFromBidDetails(T bidDetails) {
        TargetingContext context = bidDetails.getBidTimeTargetingContext();

        if (context != null) {
            return context;
        }

        // Create an empty TargetingContext...we'll populate it manually
        context = targetingContextFactory.createTargetingContext();

        // Set the IP address from the bid
        try {
            context.setIpAddress(bidDetails.getIpAddress());
        } catch (InvalidIpAddressException e) {
            // This should never happen since the IP was validated at bid time
            throw new IllegalArgumentException("BidDetails contains an invalid IP address: " + bidDetails.getIpAddress(), e);
        }

        // 
        Impression impression = bidDetails.getImpression();
        context.setAttribute(TargetingContext.DEVICE_IDENTIFIERS, impression.getDeviceIdentifiers());
        context.setSslRequired(impression.getSslRequired());
        context.setAttribute(TargetingContext.VIDEO_PROTOCOL, impression.getVideoProtocol());
        return context;
    }

    protected Integer getQueueSize() {
        return delayedBidQueue.size();
    }

    protected BackupLogger getBackupLogger() {
        return backupLogger;
    }
}
