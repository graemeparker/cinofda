package com.adfonic.adserver.rtb.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PreDestroy;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

import com.adfonic.adserver.SystemName;
import com.adfonic.adserver.rtb.RtbIdService;
import com.adfonic.adserver.rtb.nativ.ByydRequest;
import com.adfonic.adserver.spring.config.AdserverCacheDbSpringConfig;
import com.adfonic.domain.Medium;
import com.adfonic.domain.cache.dto.adserver.FormatDto;
import com.adfonic.domain.cache.dto.adserver.adspace.AdSpaceDto;
import com.adfonic.jms.JmsResource;
import com.adfonic.jms.JmsUtils;

/**
 * Implementation of RtbIdService that provides an L1 cache in memory and
 * uses cachedb via JDBC as an L2 clustered cache.  Handle unrecognized
 * RTB_ID values, ensuring that we don't flood central with JMS publication
 * persistence messages.
 */
@Component
public class RtbIdServiceImpl implements RtbIdService {

    private static final transient Logger LOG = Logger.getLogger(RtbIdServiceImpl.class.getName());

    private final ConcurrentMap<Long, ConcurrentMap<String, Boolean>> level1Cache = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Boolean> bundleCache = new ConcurrentHashMap<String, Boolean>();

    private final BlockingQueue<WorkQueueEntry> workQueue;
    private final List<WorkerThread> workerThreads;

    @Value("${RtbIdServiceImpl.workQueue.offerTimeoutMs:50}")
    private long workQueueOfferTimeoutMs;
    @Value("${RtbIdServiceImpl.workQueue.pollTimeoutMs:1000}")
    private long workQueuePollTimeoutMs;

    @Autowired
    @Qualifier(AdserverCacheDbSpringConfig.CACHEDB_JDBC_TEMPLATE)
    private JdbcTemplate jdbcTemplate;

    @Autowired
    @Qualifier(JmsResource.CENTRAL_JMS_TEMPLATE)
    private JmsTemplate centralJmsTemplate;

    @Autowired
    private JmsUtils jmsUtils;

    @Autowired
    public RtbIdServiceImpl(@Value("${RtbIdServiceImpl.threadPool.size:1}") int threadPoolSize, @Value("${RtbIdServiceImpl.workQueue.capacity:100000}") int workQueueCapacity) {
        LOG.fine("Creating work queue with capacity=" + workQueueCapacity);
        workQueue = new ArrayBlockingQueue<>(workQueueCapacity);

        LOG.fine("Creating worker thread pool with size=" + threadPoolSize);
        workerThreads = new ArrayList<>(threadPoolSize);
        for (int k = 0; k < threadPoolSize; ++k) {
            WorkerThread workerThread = new WorkerThread("RtbIdServiceImpl-" + (k + 1) + "-of-" + threadPoolSize);
            workerThread.setDaemon(true);
            workerThread.start();
            workerThreads.add(workerThread);
        }
    }

    @PreDestroy
    public void shutdown() {
        LOG.fine("Shutting down");
        for (WorkerThread workerThread : workerThreads) {
            workerThread.shutdown();
        }
    }

    /**
     * Queue the addition of a new FormatDto on a given AdSpace
     */
    @Override
    public void addFormatToAdSpace(AdSpaceDto adSpace, FormatDto format) {
        // Send a JMS message requesting persistence of the addition of the AdSpace
        if (LOG.isLoggable(Level.INFO)) {
            LOG.info("Queueing addition of new FormatDto \"" + format.getSystemName() + "\" to AdSpace.id=" + adSpace.getId());
        }
        HashMap<String, Serializable> msg = new HashMap<String, Serializable>();
        msg.put("adSpace.id", adSpace.getId());
        msg.put("format.systemName", format.getSystemName());

        jmsUtils.sendObject(centralJmsTemplate, JmsResource.RTB_ADSPACE_ADD_FORMAT, msg);

        // Add the FormatDto to the AdSpace itself in our in-memory copy.  This won't
        // cause it to start serving ads of the given format or anything, since there
        // won't have been any eligible creatives associated for that format in the
        // domain cache.  But it will at least stop us from queueing JMS messages
        // repeatedly for the same AdSpace/FormatDto combo.  Once the domain reloads,
        // ads for that combo should start serving.
        synchronized (adSpace) {
            adSpace.getFormatIds().add(format.getId());
        }
    }

    /** @{inheritDoc} */
    @Override
    public void handleUnrecognizedRtbId(ByydRequest bidRequest, long publisherId, String rtbId) {
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Handling " + publisherId + "/" + rtbId);
        }

        // Grab the L1 cache for the given publisher (RTB_ID is only unique per publisher),
        // creating it on the fly if need be.
        ConcurrentMap<String, Boolean> publisherL1Cache = getOrCreatePublisherL1Cache(publisherId);

        // Don't even bother with a containsKey check here, keep it simple/fast.
        // BTW, we're using a Map here even though we really just need a Set,
        // since ConcurrentHashMap is non-blocking.  The value doesn't matter,
        // it's just presence of the key that matters.
        if (publisherL1Cache.putIfAbsent(rtbId, Boolean.TRUE) != null) {
            // It's already in the L1 cache, so there's nothing more to do
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("Already in L1 cache: publisherId=" + publisherId + ", rtbId=" + rtbId);
            }
            return;
        }

        // It's ours to handle...queue it for L2 check
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Added " + publisherId + "/" + rtbId + " to the L1 cache, queueing for L2 processing");
        }
        try {
            // Offer it to the queue, but don't wait forever.  We need to timeout
            // if the queue (which is bounded) is at capacity, since we don't want
            // the calling thread to have to wait too long.
            if (!workQueue.offer(new WorkQueueEntry(WorkQueueEntry.WorkType.CREATE_PUBLICATION, bidRequest, publisherId, rtbId), workQueueOfferTimeoutMs, TimeUnit.MILLISECONDS)) {
                // Heads-up...if the queue hits capacity then this warning may spew like crazy!
                // I'm not bothering to rate limit the warning or anything, keeping it simple.
                LOG.warning("Failed to queue entry for publisherId=" + publisherId + ", rtbId=" + rtbId + " (increase capacity?)");

                // TODO: handle this more smartlyish.  As of right now, we basically
                // just quietly (or not so quietly) ignore the failure.  Since the
                // RTB_ID is in the L1 cache now, we won't repeatedly queue the
                // same one for L2 processing at least...but that's not necessarily
                // a good thing.  L1 says we handled it, but we never did.  Anyway,
                // this is the lesser of two evils -- the other evil being the
                // calling thread having to wait indefinitely for room to free up.
            }
        } catch (InterruptedException e) {
            LOG.warning("Interrupted while offering queue entry for publisherId=" + publisherId + ", rtbId=" + rtbId);
        }
    }

    public void flush() {
        level1Cache.clear();

    }

    /** @{inheritDoc} */
    @Override
    public void handleBundleAssociation(ByydRequest bidRequest, Long publicationId) {
        String bundleName = bidRequest.getBundleName();
        if (StringUtils.isNotBlank(bundleName)) {
            if (bundleCache.putIfAbsent((bundleName + publicationId), Boolean.TRUE) != null) {
                LOG.log(Level.FINE, "Bundle and publication relation have been processed previously (Bundle={0} ; PublicationId={1})", new Object[] { bundleName, publicationId });
            } else {
                try {
                    WorkQueueEntry workQueueEntry = new WorkQueueEntry(com.adfonic.adserver.rtb.impl.RtbIdServiceImpl.WorkQueueEntry.WorkType.BUNDLE_ASSOCIATION, bidRequest,
                            publicationId);
                    if (!workQueue.offer(workQueueEntry, workQueueOfferTimeoutMs, TimeUnit.MILLISECONDS)) {
                        LOG.log(Level.WARNING, "Failed to queue entry for linking bundle to publication (Bundle={0} ; PublicationId={1}) (increase capacity?)", new Object[] {
                                bundleName, publicationId });
                    }
                } catch (InterruptedException e) {
                    LOG.log(Level.WARNING, "Interrupted while offering queue entry for linking bundle to publication (Bundle={0} ; PublicationId={1})", new Object[] { bundleName,
                            publicationId });
                }
            }
        }
    }

    /**
     * Check the L2 cache to see if we already know about the given RTB_ID.
     * If it's already known, we don't need to do anything further.
     * If it's not already in L2, then we queue (JMS) a persistence request.
     */
    private void doLevel2Check(ByydRequest bidRequest, long publisherId, String rtbId) {
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Doing Level 2 check for publisherId=" + publisherId + ", rtbId=" + rtbId);
        }

        // Check the L2 cache (database) to see if it's already there
        Boolean found = Boolean.FALSE;
        try {
            found = jdbcTemplate.queryForObject("SELECT TRUE FROM PUBLICATION_RTB FORCE INDEX (rtb_idx) WHERE PUBLISHER_ID=? AND RTB_ID=?", Boolean.class, publisherId, rtbId);
        } catch (EmptyResultDataAccessException e) {
            // This just means there wasn't a row
        } catch (Exception e) {
            // This is something else bad (i.e. db down) and we should log it
            LOG.log(Level.WARNING, "Failed to query PUBLICATION_RTB for publisherId=" + publisherId + ", rtbId=" + rtbId, e);
        }
        if (BooleanUtils.isTrue(found)) {
            // Somebody else already added it to L2, so we have nothing else to do
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("Already present in L2 cache: publisherId=" + publisherId + ", rtbId=" + rtbId);
            }
            return;
        } else {
            // It's not in L2 yet, so this is a brand new publication.  Queue it
            // for persistence.
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("Not found in L2 cache: publisherId=" + publisherId + ", rtbId=" + rtbId);
            }
            sendJms(bidRequest, publisherId, rtbId);
        }
    }

    /**
     * Send a message to the JMS queue to indicate that a new RTB publication
     * needs to be persisted (saved in tools db).
     */
    private void sendJms(ByydRequest bidRequest, long publisherId, String rtbId) {
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Queueing for persistence: publisherId=" + publisherId + ", rtbId=" + rtbId);
        }

        // Construct the value of Publication.name
        String publicationName = generatePublicationName(bidRequest);
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Generated Publication.name=" + publicationName);
        }

        // Determine the PublicationTypeDto based on the bid request, since this
        // needs to be passed along with the queued persistence request.
        String publicationTypeSystemName;
        String bundleName = null;
        if (Medium.APPLICATION.equals(bidRequest.getMedium())) {
            // TODO: maybe detect the device type to further narrow this down
            // to IPHONE_APP or IPAD_APP or ANDROID_APP.
            publicationTypeSystemName = SystemName.OTHER_APP;
            bundleName = bidRequest.getBundleName();
        } else {
            // I don't think we can do much to further clarify this.  I can't think
            // of a reliable way to distinguish MOBILE_SITE from IPHONE_SITE.
            publicationTypeSystemName = SystemName.MOBILE_SITE;
        }
        Integer sellerNetworkId = bidRequest.getSellerNetworkId();

        // Send a JMS message requesting persistence of the Publication and/or AdSpace
        HashMap<String, Serializable> msg = new HashMap<String, Serializable>();
        msg.put("publisher.id", publisherId);
        msg.put("publication.rtbId", rtbId);
        msg.put("publication.name", publicationName);
        msg.put("publication.publicationType.systemName", publicationTypeSystemName);
        msg.put("publicationProvidedInfo.sellerNetworkId", sellerNetworkId);
        msg.put("publication.bundle", bundleName);

        // Establish either the "site" or "app" -- which mostly have
        // the same attributes, but some are specific to the type.
        msg.put("publication.urlString", bidRequest.getPublicationUrlString());

        List<String> iabIds = bidRequest.getIabIds();
        if (iabIds != null) {
            msg.put("publication.iabIds", (Serializable) iabIds);
        }

        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Sending JMS message: " + msg);
        }
        jmsUtils.sendObject(centralJmsTemplate, JmsResource.RTB_PUBLICATION_PERSISTENCE, msg);
    }

    /**
     * Send a message to the JMS queue to indicate a new bundle -publication association
     * needs to be persisted (saved in tools db).
     */
    private void queueForBundleAssociation(ByydRequest bidRequest, Long publicationId) {
        String bundleName = bidRequest.getBundleName();
        LOG.log(Level.INFO, "Queueing for bundle association (Bundle={0} ; PublicationId={1})", new Object[] { bundleName, publicationId });

        // Send a JMS message requesting persistence of the Publication and/or AdSpace
        HashMap<String, Serializable> msg = new HashMap<String, Serializable>();
        msg.put("publication.id", publicationId);
        msg.put("publication.bundle", bundleName);

        LOG.log(Level.FINE, "Sending JMS message (bundle association): {0}", msg);
        jmsUtils.sendObject(centralJmsTemplate, JmsResource.RTB_APP_BUNDLE_PERSISTENCE, msg);
    }

    /**
     * Convenience method for getting (or creating on the fly) the L1 cache
     * for a particular publisher id.
     */
    private ConcurrentMap<String, Boolean> getOrCreatePublisherL1Cache(long publisherId) {
        ConcurrentMap<String, Boolean> publisherL1Cache = level1Cache.get(publisherId);
        if (publisherL1Cache == null) {
            publisherL1Cache = new ConcurrentHashMap<>();
            ConcurrentMap<String, Boolean> alreadyThere = level1Cache.putIfAbsent(publisherId, publisherL1Cache);
            if (alreadyThere != null) {
                publisherL1Cache = alreadyThere;
            }
        }
        return publisherL1Cache;
    }

    // This was originally in RtbLogicImpl
    private static String generatePublicationName(ByydRequest bidRequest) {
        String siteOrAppName = bidRequest.getPublicationName();
        String pubName = bidRequest.getPub();
        boolean hasPubName = StringUtils.isNotBlank(pubName);
        boolean hasSiteOrAppName = StringUtils.isNotBlank(siteOrAppName);

        StringBuilder bld = new StringBuilder();
        if (hasPubName) {
            bld.append(pubName).append(" - ");
        } else if (!hasSiteOrAppName) {
            bld.append(bidRequest.getPublicationRtbId());
        }

        if (hasSiteOrAppName) {
            bld.append(siteOrAppName);
        }

        bld.append(Medium.SITE.equals(bidRequest.getMedium()) ? " -site" : " -app");

        return bld.toString();
    }

    private class WorkerThread extends Thread {
        private volatile boolean active = true;

        public WorkerThread(String name) {
            super(name);
        }

        private void shutdown() {
            active = false;
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("WorkerThread " + getId() + " deactivated");
            }
        }

        @Override
        public void run() {
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("WorkerThread " + getId() + " is running");
            }
            while (active) {
                try {
                    // Poll, but don't poll indefinitely.  Use the configured timeout,
                    // which forces each thread to re-check its active flag every once
                    // in a while.  Even though these are daemon threads, we want to
                    // provide for a smooth and graceful shutdown.
                    WorkQueueEntry entry = workQueue.poll(workQueuePollTimeoutMs, TimeUnit.MILLISECONDS);
                    if (entry != null) {
                        switch (entry.getWorkType()) {
                        case CREATE_PUBLICATION:
                            try {
                                // Call our L2 cache check logic and onward
                                doLevel2Check(entry.getBidRequest(), entry.getPublisherId(), entry.getRtbId());
                            } catch (Exception e) {
                                LOG.log(Level.SEVERE, "Failed to do level 2 check", e);
                            }
                            break;

                        case BUNDLE_ASSOCIATION:
                            try {
                                queueForBundleAssociation(entry.getBidRequest(), entry.getPublicationId());
                            } catch (Exception e) {
                                LOG.log(Level.SEVERE, "Failed sending to do bundle association", e);
                            }
                        default:
                            break;
                        }

                    }
                } catch (InterruptedException e) {
                    LOG.warning("WorkerThread " + getId() + " was interrupted");
                }
            }
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("WorkerThread " + getId() + " is done");
            }
        }
    }

    /**
     * This is just a wrapper around the stuff we need to shove in the
     * work queue for an L2 check.
     */
    protected static final class WorkQueueEntry {
        private static enum WorkType {
            CREATE_PUBLICATION, BUNDLE_ASSOCIATION
        }

        private final WorkType workType;
        private final ByydRequest bidRequest;
        private final Long publisherId;
        private final String rtbId;
        private final Long publicationId;

        public WorkQueueEntry(WorkType workType, ByydRequest bidRequest, Long publisherId, String rtbId) {
            this.bidRequest = bidRequest;
            this.workType = workType;
            this.publisherId = publisherId;
            this.rtbId = rtbId;
            publicationId = null;
        }

        public WorkQueueEntry(WorkType workType, ByydRequest bidRequest, Long publicationId) {
            this.bidRequest = bidRequest;
            this.workType = workType;
            this.publicationId = publicationId;
            this.publisherId = null;
            this.rtbId = null;
        }

        public WorkType getWorkType() {
            return workType;
        }

        public ByydRequest getBidRequest() {
            return bidRequest;
        }

        public long getPublisherId() {
            return publisherId;
        }

        public String getRtbId() {
            return rtbId;
        }

        public Long getPublicationId() {
            return publicationId;
        }
    }
}