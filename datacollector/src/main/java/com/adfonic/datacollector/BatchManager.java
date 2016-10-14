package com.adfonic.datacollector;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PreDestroy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.adfonic.adserver.AdEvent;
import com.adfonic.datacollector.dao.AccountingDao;
import com.adfonic.datacollector.dao.ClusterDao;
import com.adfonic.datacollector.dao.ToolsDao;
import com.adfonic.domain.AdAction;
import com.adfonic.domain.AdvertiserStoppage;
import com.adfonic.domain.CampaignStoppage;
import com.adfonic.domain.StopAction;
import com.adfonic.domain.cache.dto.datacollector.campaign.CampaignDto;
import com.adfonic.domain.cache.dto.datacollector.publication.PublisherDto;
import com.adfonic.util.DaemonThreadFactory;
import com.adfonic.util.DateUtils;
import com.adfonic.util.stats.CounterManager;

@Component
public class BatchManager {

    private static final transient Logger LOG = Logger.getLogger(BatchManager.class.getName());

    private static final int NUM_BATCH_FLUSH_PASSES = 2;

    private final Map<Long, Batch> batchesById = new HashMap<Long, Batch>();
    @Autowired
    private ToolsDao toolsDao;
    @Autowired
    private ClusterDao clusterDao;
    @Autowired
    private AccountingDao accountingDao;
    @Autowired
    private StoppageManager stoppageManager;
    @Autowired
    private CounterManager counterManager;

    private final long batchDurationMs;
    private final Executor threadPool;

    // AF-1535
    /*package*/enum Counter {
        BATCHES_FLUSHED, EVENTS_FLUSHED,
    }

    @Autowired
    public BatchManager(@Value("${BatchManager.batchDurationMs}") long batchDurationMs, @Value("${BatchManager.threadPool.size}") int threadPoolSize) {
        LOG.info("Thread pool size: " + threadPoolSize);
        this.batchDurationMs = batchDurationMs;
        this.threadPool = Executors.newFixedThreadPool(threadPoolSize, DaemonThreadFactory.getInstance());
    }

    /**
     * Add an AdEvent to the current batch
     * @param accounting the AdEventAccounting wrapped around the given AdEvent
     * @param userAgentId the established respective USER_AGENT id
     */
    public void addToCurrentBatch(AdEventAccounting accounting, Long userAgentId) {
        // Get or create the current batch
        long batchId = getCurrentBatchId();
        Batch batch = batchesById.get(batchId);
        if (batch == null) {
            synchronized (batchesById) {
                batch = batchesById.get(batchId);
                if (batch == null) {
                    if (LOG.isLoggable(Level.FINER)) {
                        LOG.finer("Creating batch " + batchId);
                    }
                    batch = new Batch(batchId);
                    batchesById.put(batchId, batch);
                }
            }
        }

        batch.add(accounting, userAgentId);
    }

    /**
     * Get the numeric id of the current batch
     */
    long getCurrentBatchId() {
        return System.currentTimeMillis() / batchDurationMs;
    }

    /**
     * Flush batches to the database.  This method is intended to be called
     * by the scheduler on a regular basis.
     */
    @PreDestroy
    public synchronized void flushBatches() {
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Flushing batches");
        }

        // We *could* probably get away with just taking the *last* batch id and
        // flushing it.  But that's not bulletproof...i.e. it's conceivable that
        // this method may not get invoked until there have already been 2 batches,
        // i.e. if context startup lags, or it may get delayed in its invocation
        // if load spikes.  So to be safe, we need to go through and pull out
        // *all* batches that need to be flushed.  99% of the time there will only
        // be one, and this synchronized block will be not measurably more expensive
        // than just doing one synchronized get on the last batch id.
        long currentBatchId = getCurrentBatchId();
        List<Batch> batchesToFlush = new ArrayList<Batch>();
        synchronized (batchesById) {
            for (Iterator<Map.Entry<Long, Batch>> iter = batchesById.entrySet().iterator(); iter.hasNext();) {
                Map.Entry<Long, Batch> entry = iter.next();
                if (entry.getKey() < currentBatchId) {
                    iter.remove(); // remove it from batchesById
                    batchesToFlush.add(entry.getValue());
                }
            }
        }

        // We take a two-pass approach here.  I've only seen this happen once, but
        // that was enough for me to modify the approach.  It's conceivable that
        // while an addToCurrentBatch call is being made, the batch changes.  And
        // it's conceivable that this flush method was called immediately after
        // the batch change -- but *before* the addToCurrentBatch call finishes.
        // So not only do we need to synchronize on the batch list (since that other
        // thread could still be adding to it while we're flushing), but we also
        // take this two-pass approach, which should pick up any latecomers.
        int numEvents = 0;
        for (int k = 0; k < NUM_BATCH_FLUSH_PASSES; ++k) {
            for (Batch batch : batchesToFlush) {
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.fine("Pass " + (k + 1) + " of " + NUM_BATCH_FLUSH_PASSES + ", flushing batch: " + batch.getId());
                }
                numEvents += batch.flushAsynchronously();
                counterManager.incrementCounter(getClass(), Counter.BATCHES_FLUSHED);
                counterManager.incrementCounter(getClass(), Counter.EVENTS_FLUSHED, numEvents);
            }
        }

        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Queued flushing of " + numEvents + " events");
        }
    }

    private void flushEvents(PublisherDto publisher, CampaignDto campaign, int advertiserDateId, List<Event> events) throws java.sql.SQLException {
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Flushing Publisher id=" + publisher.getId() + ", Campaign id=" + campaign.getId() + ", advertiserDateId=" + advertiserDateId + ", # of events="
                    + events.size());
        }

        Map<StopAction, Date> stopActionMap = new HashMap<StopAction, Date>();
        boolean verifyConversionTracking = false;
        boolean verifyInstallTracking = false;

        List<Event> type2b3bEventsToLog = new LinkedList<Event>();
        BigDecimal advertiserSpendTally = null;
        BigDecimal currentPublisherCreditMultiplier = null;

        List<Event> type2cEventsToLog = new LinkedList<Event>();
        BigDecimal publisherBalanceTally = null;

        for (Event event : events) {
            AdEventAccounting accounting = event.getAdEventAccounting();
            AdEvent adEvent = accounting.getAdEvent();

            if (adEvent.getAdAction() == AdAction.CONVERSION) {
                verifyConversionTracking = true;
            }
            if (adEvent.getAdAction() == AdAction.INSTALL) {
                verifyInstallTracking = true;
            }

            // https://sites.google.com/a/adfonic.com/devteam/account-balance-update-batching
            BigDecimal publisherCreditMultiplier;
            if (adEvent.isRtb()) {
                // Type 3b
                publisherCreditMultiplier = BigDecimal.ZERO;
            } else if (accounting.getAdvertiser().getCompany().isBackfill()) {
                // Type 2c - update publisher balance only
                type2cEventsToLog.add(event);
                BigDecimal payout = accounting.getPayout();
                if (payout != null) {
                    if (publisherBalanceTally == null) {
                        publisherBalanceTally = payout;
                    } else {
                        publisherBalanceTally = publisherBalanceTally.add(payout);
                    }
                    // Add publisher VAT if applicable
                    if (accounting.getPublisherVat() != null) {
                        publisherBalanceTally = publisherBalanceTally.add(accounting.getPublisherVat());
                    }
                }
                continue;
            } else {
                // Type 2b
                publisherCreditMultiplier = accounting.getPublisherCreditMultiplier(campaign.getAgencyDiscount());
            }

            // Type 2b or 3b
            if (currentPublisherCreditMultiplier == null) {
                // First one...just set it
                currentPublisherCreditMultiplier = publisherCreditMultiplier;
            } else if (!currentPublisherCreditMultiplier.equals(publisherCreditMultiplier)) {
                // The publisherCreditMultiplier is changing, so flush and log
                // everything we've got tallied up so far
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.fine("Publisher credit multiplier changing from " + currentPublisherCreditMultiplier + " to " + publisherCreditMultiplier + ", flushing");
                }
                updateBudgetsAndLogAdEvents(campaign, publisher, type2b3bEventsToLog, advertiserSpendTally, currentPublisherCreditMultiplier, advertiserDateId, stopActionMap);
                // Start fresh
                type2b3bEventsToLog.clear();
                advertiserSpendTally = null;
                // Update the current publisher credit multiplier
                currentPublisherCreditMultiplier = publisherCreditMultiplier;
            }

            // Queue this event for logging
            type2b3bEventsToLog.add(event);

            // Keep a running tally of advertiser spend
            if (advertiserSpendTally == null) {
                advertiserSpendTally = accounting.getAdvertiserSpend();
            } else {
                advertiserSpendTally = advertiserSpendTally.add(accounting.getAdvertiserSpend());
            }
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("advertiserSpendTally + " + accounting.getAdvertiserSpend() + " = " + advertiserSpendTally);
            }
        }

        // Do a final flush of any Type 2b or 3b events we've got tallied
        if (!type2b3bEventsToLog.isEmpty()) {
            updateBudgetsAndLogAdEvents(campaign, publisher, type2b3bEventsToLog, advertiserSpendTally, currentPublisherCreditMultiplier, advertiserDateId, stopActionMap);
        }

        // Log any Type 2c events
        if (!type2cEventsToLog.isEmpty()) {
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("Logging " + type2cEventsToLog.size() + " Type 2c events");
            }
            for (Event event : type2cEventsToLog) {
                clusterDao.createAdEventLog(event.getAdEventAccounting(), event.getUserAgentId(), event.getAdvertiserTimeId(), event.getPublisherTimeId());
            }

            // And flush the publisher-balance-only increment
            if (publisherBalanceTally != null) {
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.fine("Incrementing Publisher id=" + publisher.getId() + " balance by total tally: " + publisherBalanceTally);
                }
                accountingDao.incrementPublisherBalance(publisher, publisherBalanceTally);
            }
        }

        // Deal with the StopActions, if there were any
        if (!stopActionMap.isEmpty()) {
            for (Map.Entry<StopAction, Date> entry : stopActionMap.entrySet()) {
                StopAction stopAction = entry.getKey();
                Date eventTime = entry.getValue();
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.fine("Handling StopAction: " + stopAction);
                }
                switch (stopAction) {
                case STOP_CAMPAIGN_FOR_HOUR:
                    stoppageManager.stopCampaign(eventTime, campaign, CampaignStoppage.Reason.HOURLY_BUDGET);
                    break;
                case STOP_CAMPAIGN_FOR_TODAY:
                    stoppageManager.stopCampaign(eventTime, campaign, CampaignStoppage.Reason.DAILY_BUDGET);
                    break;
                case STOP_CAMPAIGN_FOREVER:
                    stoppageManager.stopCampaign(eventTime, campaign, CampaignStoppage.Reason.OVERALL_BUDGET);
                    break;
                case STOP_ADVERTISER_FOR_TODAY:
                    stoppageManager.stopAdvertiser(eventTime, campaign.getAdvertiser(), AdvertiserStoppage.Reason.DAILY_BUDGET);
                    break;
                case STOP_ADVERTISER_ZERO_BALANCE:
                    stoppageManager.stopAdvertiser(eventTime, campaign.getAdvertiser(), AdvertiserStoppage.Reason.ZERO_BALANCE);
                    break;
                default:
                    LOG.severe("Unhandled StopAction: " + stopAction);
                    break;
                }
            }
        }

        if (verifyConversionTracking) {
            // If campaign.conversionTrackingVerified has not been set yet, set it.
            toolsDao.markCampaignConversionTrackingVerified(campaign.getId());
        }

        if (verifyInstallTracking) {
            // If campaign.installTrackingVerified has not been set yet, set it.
            toolsDao.markCampaignInstallTrackingVerified(campaign.getId());
        }
    }

    void updateBudgetsAndLogAdEvents(CampaignDto campaign, PublisherDto publisher, List<Event> events, BigDecimal advertiserSpendTally, BigDecimal publisherCreditMultiplier,
            int advertiserDateId, Map<StopAction, Date> stopActionMap) throws java.sql.SQLException {
        int impressionsCount = 0;
        int conversionsCount = 0;
        int clicksCount = 0;

        for (Event event : events) {
            if (event.accounting.getAdEvent().getAdAction().equals(AdAction.AD_SERVED))
                impressionsCount++;
            if (event.accounting.getAdEvent().getAdAction().equals(AdAction.CLICK))
                clicksCount++;
            if (event.accounting.getAdEvent().getAdAction().equals(AdAction.CONVERSION) || event.accounting.getAdEvent().getAdAction().equals(AdAction.INSTALL))
                conversionsCount++;
        }

        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Updating budgets for " + events.size() + " batched event(s) for Campaign id=" + campaign.getId() + ", Publisher id=" + publisher.getId()
                    + ", advertiserSpendTally=" + advertiserSpendTally + ", publisherCreditMultiplier=" + publisherCreditMultiplier + ", impressionsCount=" + impressionsCount
                    + ", clicksCount=" + clicksCount + ", conversionsCount=" + conversionsCount);
        }

        AccountingDao.UpdateBudgetsResult result = accountingDao.updateBudgets(campaign, publisher, advertiserSpendTally, publisherCreditMultiplier, advertiserDateId,
                impressionsCount, clicksCount, conversionsCount);

        BigDecimal adjustedAdvertiserSpend = result.getAdjustedAdvertiserSpend();

        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Adjusting and logging " + events.size() + " AdEvent(s)");
        }
        // Iterate through the events, and adjust spend as needed
        for (Event event : events) {
            AdEventAccounting accounting = event.getAdEventAccounting();
            BigDecimal spendForThisEvent = accounting.getAdvertiserSpend();
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("spendForThisEvent=" + spendForThisEvent + ", adjustedAdvertiserSpend(remaining)=" + adjustedAdvertiserSpend);
            }
            if (spendForThisEvent.compareTo(adjustedAdvertiserSpend) > 0) {
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.fine("Adjusting this event's spend down from " + spendForThisEvent + " to " + adjustedAdvertiserSpend + ", now zero remaining");
                }
                // We have to adjust the spend for this event
                accounting.setAdjustedAdvertiserSpend(adjustedAdvertiserSpend, campaign.getAgencyDiscount());
                // Nothing left
                adjustedAdvertiserSpend = BigDecimal.ZERO;
            } else {
                // Subtract this event's spend
                adjustedAdvertiserSpend = adjustedAdvertiserSpend.subtract(spendForThisEvent);
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.fine("adjustedAdvertiserSpend(remaining) is now " + adjustedAdvertiserSpend);
                }
            }

            // Log the AdEvent
            clusterDao.createAdEventLog(accounting, event.getUserAgentId(), event.getAdvertiserTimeId(), event.getPublisherTimeId());
        }

        // "Merge" the stop actions, if there were any
        if (result.getStopActions() != null) {
            // Use the event time of the very last event in the batch as the
            // time at which the stoppage takes effect.
            Date latestEventTime = events.get(events.size() - 1).getAdEventAccounting().getAdEvent().getEventTime();
            for (StopAction stopAction : result.getStopActions()) {
                // Only update the stoppage effective time if ours was earlier than
                // the previous time, if there was one
                Date previousEventTime = stopActionMap.get(stopAction);
                if (previousEventTime == null || latestEventTime.before(previousEventTime)) {
                    stopActionMap.put(stopAction, latestEventTime);
                }
            }
        }
    }

    private static final class Event {
        private final AdEventAccounting accounting;
        private final Long userAgentId;
        private final int advertiserTimeId;
        private final int publisherTimeId;

        private Event(AdEventAccounting accounting, Long userAgentId, int advertiserTimeId, int publisherTimeId) {
            this.accounting = accounting;
            this.userAgentId = userAgentId;
            this.advertiserTimeId = advertiserTimeId;
            this.publisherTimeId = publisherTimeId;
        }

        public AdEventAccounting getAdEventAccounting() {
            return accounting;
        }

        public Long getUserAgentId() {
            return userAgentId;
        }

        public int getAdvertiserTimeId() {
            return advertiserTimeId;
        }

        public int getPublisherTimeId() {
            return publisherTimeId;
        }
    }

    private final class Batch {
        private final Object modificationMutex = new Object();
        private final long id;
        // Publisher -> Campaign -> advertiserDateId(yyyyMMddHH) -> List<Event[AdEventAccounting,userAgentId]>
        private final Map<PublisherDto, Map<CampaignDto, Map<Integer, List<Event>>>> data = new HashMap<PublisherDto, Map<CampaignDto, Map<Integer, List<Event>>>>();

        private Batch(long id) {
            this.id = id;
        }

        public long getId() {
            return id;
        }

        public Map<PublisherDto, Map<CampaignDto, Map<Integer, List<Event>>>> getData() {
            return data;
        }

        public void add(AdEventAccounting accounting, Long userAgentId) {
            synchronized (modificationMutex) {
                Map<CampaignDto, Map<Integer, List<Event>>> byCampaign = data.get(accounting.getPublisher());
                if (byCampaign == null) {
                    byCampaign = new HashMap<CampaignDto, Map<Integer, List<Event>>>();
                    data.put(accounting.getPublisher(), byCampaign);
                }

                Map<Integer, List<Event>> byAdvertiserDateId = byCampaign.get(accounting.getCampaign());
                if (byAdvertiserDateId == null) {
                    byAdvertiserDateId = new HashMap<Integer, List<Event>>();
                    byCampaign.put(accounting.getCampaign(), byAdvertiserDateId);
                }

                int advertiserTimeId = DateUtils.getTimeID(accounting.getAdEvent().getEventTime(), accounting.getAdvertiser().getCompany().getDefaultTimeZone());
                int advertiserDateId = advertiserTimeId / 100;
                List<Event> events = byAdvertiserDateId.get(advertiserDateId);
                if (events == null) {
                    events = new ArrayList<Event>();
                    byAdvertiserDateId.put(advertiserDateId, events);
                }

                int publisherTimeId = DateUtils.getTimeID(accounting.getAdEvent().getEventTime(), accounting.getPublisher().getCompany().getDefaultTimeZone());

                if (LOG.isLoggable(Level.FINE)) {
                    LOG.fine("Batch id=" + id + " is adding " + accounting.getAdEvent().getAdAction() + " event for Publisher id=" + accounting.getPublisher().getId()
                            + ", Campaign id=" + accounting.getCampaign().getId());
                }

                events.add(new Event(accounting, userAgentId, advertiserTimeId, publisherTimeId));
            }
        }

        /**
         * Flush the events contained in this batch asynchronously.
         */
        public int flushAsynchronously() {
            int numEvents = 0;
            synchronized (modificationMutex) {
                for (Map.Entry<PublisherDto, Map<CampaignDto, Map<Integer, List<Event>>>> publisherEntry : data.entrySet()) {
                    PublisherDto publisher = publisherEntry.getKey();
                    Map<CampaignDto, Map<Integer, List<Event>>> byCampaign = publisherEntry.getValue();
                    for (Map.Entry<CampaignDto, Map<Integer, List<Event>>> campaignEntry : byCampaign.entrySet()) {
                        CampaignDto campaign = campaignEntry.getKey();
                        Map<Integer, List<Event>> byAdvertiserDateId = campaignEntry.getValue();
                        for (Map.Entry<Integer, List<Event>> advertiserDateIdEntry : byAdvertiserDateId.entrySet()) {
                            int advertiserDateId = advertiserDateIdEntry.getKey();
                            List<Event> events = advertiserDateIdEntry.getValue();
                            if (!events.isEmpty()) {
                                numEvents += events.size();
                                // We need to make a copy of the events list, since we'll be flushing them
                                // asynchronously, and the original events list itself may get released and
                                // checked again on a subsequent pass.
                                List<Event> eventsCopy = new ArrayList<Event>(events.size());
                                eventsCopy.addAll(events);
                                events.clear(); // clear it out to prepare for the next pass
                                // Pass the events off to the thread pool for asynchronous processing
                                threadPool.execute(new FlushEventsRunner(publisher, campaign, advertiserDateId, eventsCopy));
                            }
                        }
                    }
                }
            }
            return numEvents;
        }
    }

    // This is formalized as an inner class instead of anonymous class in order
    // to improve performance (slightly, but every little bit counts).
    private final class FlushEventsRunner implements Runnable {
        private final PublisherDto publisher;
        private final CampaignDto campaign;
        private final int advertiserDateId;
        private final List<Event> events;

        private FlushEventsRunner(PublisherDto publisher, CampaignDto campaign, int advertiserDateId, List<Event> events) {
            this.publisher = publisher;
            this.campaign = campaign;
            this.advertiserDateId = advertiserDateId;
            this.events = events;
        }

        @Override
        public void run() {
            try {
                flushEvents(publisher, campaign, advertiserDateId, events);
            } catch (Exception e) {
                LOG.log(Level.SEVERE, "Failed to flush " + events.size() + " events for Publisher id=" + publisher.getId() + ", Campaign id=" + campaign.getId()
                        + ", advertiserDateId=" + advertiserDateId, e);
            }
        }
    }
}
