package com.adfonic.datacollector;

import java.io.File;
import java.sql.SQLException;
import java.util.Date;
import java.util.EnumSet;
import java.util.TimeZone;
import java.util.logging.Level;

import javax.annotation.PreDestroy;
import javax.annotation.Resource;

import net.byyd.archive.model.v1.V1DomainModelMapper;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;

import org.apache.commons.collections.MapIterator;
import org.apache.commons.collections.keyvalue.MultiKey;
import org.apache.commons.collections.map.LRUMap;
import org.apache.commons.collections.map.MultiKeyMap;
import org.apache.commons.lang.time.FastDateFormat;
import org.apache.log4j.SimpleLayout;
import org.apache.log4j.rolling.RollingFileAppender;
import org.apache.log4j.rolling.TimeBasedRollingPolicy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.adfonic.adserver.AdEvent;
import com.adfonic.adserver.AdEventFactory;
import com.adfonic.adserver.AdserverUtils;
import com.adfonic.adserver.Click;
import com.adfonic.adserver.Impression;
import com.adfonic.adserver.KryoManager;
import com.adfonic.datacollector.dao.ClusterDao;
import com.adfonic.datacollector.kafka.AdEventConversionUtils;
import com.adfonic.datacollector.kafka.KafkaProducer;
import com.adfonic.domain.AdAction;
import com.adfonic.domain.cache.DataCollectorDomainCache;
import com.adfonic.domain.cache.DataCollectorDomainCacheManager;
import com.adfonic.domain.cache.dto.datacollector.campaign.CampaignDto;
import com.adfonic.domain.cache.dto.datacollector.publication.PublicationDto;
import com.adfonic.jms.JmsUtils;
import com.adfonic.jms.UserAgentUpdatedMessage;
import com.adfonic.tracker.ClickService;
import com.adfonic.util.DateUtils;
import com.adfonic.util.stats.CounterManager;

/** Consume and handle AdEvents from the queue */
@Component
public class AdEventDataCollector {

    private static final transient java.util.logging.Logger LOG = java.util.logging.Logger.getLogger(AdEventDataCollector.class.getName());

    // Max allowable length of the User-Agent header we store in the db
    static final int MAX_USER_AGENT_LENGTH = 512;

    // This is the USER_AGENT.ID cache by User-Agent and Model.id,
    // which is a MultiKeyMap decorator around an LRUMap.
    private final MultiKeyMap userAgentMap;

    private final File failedLogFile;
    private final int clickDefaultTtlSeconds;
    private final int installTrackingTtlSeconds;
    private final int conversionTrackingTtlSeconds;
    private final RollingFileAppender failedLogFileAppender;
    private final org.apache.log4j.Logger failedLogger;
    
    private V1DomainModelMapper mapper = new V1DomainModelMapper();

    @Autowired
    private ClusterDao clusterDao;
    @Autowired
    private BatchManager batchManager;
    @Autowired
    private DataCollectorDomainCacheManager dataCollectorDomainCacheManager;
    @Autowired
    private AdEventFactory adEventFactory;
    @Autowired
    private KryoManager kryoManager;
    @Autowired
    private ClickService clickService;
    @Autowired
    private CounterManager counterManager;
    @Autowired
    private JmsUtils jmsUtils;
    @Resource(name = "missingCampaignsCache")
    private Ehcache missingCampaignsCache;
    @Value("${kafka.topic.failed}")
    private String failedTopic;
    @Autowired
    private AdEventConversionUtils adEventConversionUtils;
    @Autowired
    private KafkaProducer kafkaProducer;

    // AF-1535
    /*package*/enum Counter {
        // onAdEventBatch
        ON_AD_EVENT_BATCH, ON_AD_EVENT_BATCH_EVENT_FAILED,
        // onAdEvent(byte[])
        ON_AD_EVENT_SERIALIZED_FORM, ON_AD_EVENT_SERIALIZED_FORM_DESERIALIZATION_FAILURE,
        // onJSONAdEvent(byte[])
        ON_AD_EVENT_JSON_FORM, ON_AD_EVENT_JSON_FORM_DESERIALIZATION_FAILURE,
        // onFailedAdEvent
        ON_FAILED_AD_EVENT, ON_FAILED_AD_EVENT_DESERIALIZATION_FAILURE,
        // onFailedJSONAdEvent
        ON_FAILED_JSON_AD_EVENT, ON_FAILED_JSON_AD_EVENT_DESERIALIZATION_FAILURE,
        // onAdEvent(AdEvent...)
        ON_AD_EVENT, ON_AD_EVENT_TEST_MODE, ON_AD_EVENT_AD_SERVED_AND_IMPRESSION, ON_AD_EVENT_FAILED_TWICE, ON_AD_EVENT_FAILED_ONCE_REQUEUED, ON_JSON_AD_EVENT_FAILED_TWICE, ON_JSON_AD_EVENT_FAILED_ONCE_REQUEUED,
        // handleAdEvent
        HANDLE_AD_EVENT_PUBLICATION_NOT_FOUND, HANDLE_AD_EVENT_CAMPAIGN_NOT_FOUND, HANDLE_AD_EVENT_BATCHED, HANDLE_AD_EVENT_NOT_BATCHED,
        // establishUserAgentId
        USER_AGENT_TRUNCATED, USER_AGENT_CACHE_MISS, USER_AGENT_FAILURE,
        // onClickMessage
        ON_CLICK_MESSAGE, ON_CLICK_MESSAGE_DESERIALIZATION_FAILURE, ON_CLICK_MESSAGE_CAMPAIGN_NOT_FOUND, ON_CLICK_MESSAGE_DUPLICATE, ON_CLICK_MESSAGE_TRACKED,
        //CampaignNotFoundInCache
        CAMPAIGN_NOT_FOUND_IN_CACHE
    }

    // Custom log4j layout that spits out only the message itself
    private static final class FailedLoggerLayout extends SimpleLayout {
        @Override
        public String format(org.apache.log4j.spi.LoggingEvent event) {
            return (String) event.getMessage() + LINE_SEP;
        }
    }

    @Autowired
    public AdEventDataCollector(@Value("${AdEventDataCollector.failedLogFile}") File failedLogFile, @Value("${click.default.ttlSeconds}") int clickDefaultTtlSeconds,
            @Value("${click.installTracking.ttlSeconds}") int installTrackingTtlSeconds, @Value("${click.conversionTracking.ttlSeconds}") int conversionTrackingTtlSeconds,
            @Value("${AdEventDataCollector.userAgentIdCacheMaxSize}") int userAgentIdCacheMaxSize) throws java.io.IOException {
        this.failedLogFile = failedLogFile;
        this.clickDefaultTtlSeconds = clickDefaultTtlSeconds;
        this.installTrackingTtlSeconds = installTrackingTtlSeconds;
        this.conversionTrackingTtlSeconds = conversionTrackingTtlSeconds;

        // Set up our LRU cache for the USER_AGENT.ID mappings
        if (LOG.isLoggable(Level.INFO)) {
            LOG.info("USER_AGENT.ID LRU cache max size: " + userAgentIdCacheMaxSize);
        }
        userAgentMap = MultiKeyMap.decorate(new LRUMap(userAgentIdCacheMaxSize));

        // Set up the failed event logger
        if (LOG.isLoggable(Level.INFO)) {
            LOG.info("Logging failures to file: " + failedLogFile.getCanonicalPath());
        }

        // Set up the log4j time-based rolling log appender
        TimeBasedRollingPolicy rollingPolicy = new TimeBasedRollingPolicy();
        rollingPolicy.setFileNamePattern(failedLogFile.getCanonicalPath());

        failedLogFileAppender = new RollingFileAppender();
        failedLogFileAppender.setRollingPolicy(rollingPolicy);
        // Use our simple one-line message-only layout
        failedLogFileAppender.setLayout(new FailedLoggerLayout());
        failedLogFileAppender.activateOptions();

        failedLogger = org.apache.log4j.Logger.getLogger("FailedAdEventLogger");
        // Usually this has no appenders to start with, but just in case...
        failedLogger.removeAllAppenders();
        // Add our custom appender
        failedLogger.addAppender(failedLogFileAppender);
        // Disable additivity so our logging doesn't cascade up to the
        // root logger and end up in catalina.out and what not.
        failedLogger.setAdditivity(false);
        // We're only going to be using INFO
        failedLogger.setLevel(org.apache.log4j.Level.INFO);
    }

    @PreDestroy
    public void destroy() {
        if (LOG.isLoggable(Level.INFO)) {
            LOG.info("Flushing and closing the failed file LOG...");
        }
        failedLogFileAppender.close();
    }
    
    // Consumer from kafka for ad events
    public void onJSONAdEvent(net.byyd.archive.model.v1.AdEvent adEvent, byte[] objectBytes) {
        counterManager.incrementCounter(getClass(), Counter.ON_AD_EVENT_JSON_FORM);
        String message = new String(objectBytes);
        AdEvent ae = null;
        try {
            AdAction adAction = null;
            if(adEvent!=null){
                adAction = adEventConversionUtils.convertAdAction(adEvent.getAdAction());
            }
            
            //If we have an identified Ad Action, then proceed with the adserver AdEvent
            if(adAction!=null){
                ae = adEventConversionUtils.convertJsonAdEvent(adEvent, adAction);
                    
                //    Clicks reviewed for de-duplicate and discard failures
                if(adAction.equals(AdAction.CLICK)){
                    if(!onClickJSONMessage(adEvent)){
                        ae = null;
                    }
                }
            }
        } catch (Exception e) {
            // Deserialization/Conversion failed.  If we can't deserialize it, maybe another
            // instance of datacollector can.  Let's requeue it in the failed
            // AdEvent queue...about the best we can do here for now.
            LOG.warning("Requeueing message due to deserialization failure: " + e.getMessage());
            sendFailedJSONMessage(message);
            return;
        } 

        if(ae!=null){
            onAdEvent(ae, false, objectBytes);
        }
    }

    // Consumer reading from Kafka
    public void onJSONFailedAdEvent(net.byyd.archive.model.v1.AdEvent adEvent, byte[] objectBytes) {
        counterManager.incrementCounter(getClass(), Counter.ON_FAILED_JSON_AD_EVENT);
        String message = new String(objectBytes);
        AdEvent ae = null;
        try {
            AdAction adAction = adEventConversionUtils.convertAdAction(adEvent.getAdAction());
            //Consider when RTB_FAILED is UNFILLED_REQUEST
            if(adAction==null && adEvent.getAdAction().equals(net.byyd.archive.model.v1.AdAction.RTB_FAILED) && adEvent.getDetailReason()!= null && 
                                adEvent.getDetailReason().toLowerCase().contains("no") && adEvent.getDetailReason().toLowerCase().contains("creative")){
                adAction = com.adfonic.domain.AdAction.UNFILLED_REQUEST;
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.fine("Rtb Failed event is considered Unfilled Request");
                }
            }
            
            //If we have an identified Ad Action, then proceed with the adserver AdEvent
            if(adAction!=null){
                ae = adEventConversionUtils.convertJsonAdEvent(adEvent, adAction);
            }
        } catch (Exception e) {
            // Deserialization failed...since this is technically our second
            // attempt, there's not much else we can do.  Ordinarily, we log
            // twice-failed stuff to the CSV file-based failure log file.
            // But since we can't log CSV since we can't even deserialize the
            // AdEvent, let's just log the serialized byte array to the regular
            // log file...and worst case we can grep for this after the fact
            // and recover the data that way.  Log it as a hex string.
            LOG.log(Level.SEVERE, "Failed to deserialize: " + message, e);
            counterManager.incrementCounter(getClass(), Counter.ON_FAILED_JSON_AD_EVENT_DESERIALIZATION_FAILURE);
            return;
        }
        onAdEvent(ae, true, objectBytes);
    }
    
    public void sendFailedJSONMessage(String message){
        // Requeue it in the secondary failed message queue
        kafkaProducer.sendMessage(message, failedTopic);
        counterManager.incrementCounter(getClass(), Counter.ON_AD_EVENT_JSON_FORM_DESERIALIZATION_FAILURE);
    }
    
    public void onAdEvent(AdEvent event, boolean failedPreviously){
        onAdEvent(event, failedPreviously, null);
    }

    public void onAdEvent(AdEvent event, boolean failedPreviously, byte[]oldJSONMessage) {
        if (LOG.isLoggable(Level.FINER)) {
            LOG.finer("Handling " + event);
        } else if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Handling a new " + event.getAdAction() + " AdEvent");
        }

        counterManager.incrementCounter(getClass(), Counter.ON_AD_EVENT);

        if (event.isTestMode()) {
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("Discarding test mode " + event.getAdAction() + " event");
            }
            counterManager.incrementCounter(getClass(), Counter.ON_AD_EVENT_TEST_MODE);
            return;
        }

        try {
            handleAdEvent(event);
        } catch (CampaignNotFoundInCacheException cnfice) {
            // MAX-664 Don't log warnings for specific events on campaigns that aren't in cache...(they will retry anyway)
            if (!EnumSet.of(AdAction.BID_FAILED, AdAction.UNFILLED_REQUEST).contains(event.getAdAction())) {
                // MAX-165
                // campaign is not visible to datacollector cache hence temporarily we need to loop it through failed event queue
                // this AD_EVENT will loop through the failedAdEventQueue indefinitely until the campaign is found.
                // Hence, even if this has failed (we are putting this event on failed queue) we are not setting failedPreviously as we want it to loop indefinitely (for now).
                LOG.warning("Requeueing message due to Campaign id=" + event.getCampaignId() + " not found in cache for " + event);
            }
            // Requeue it in the secondary failed message queue
            kafkaProducer.sendMessage(new String(oldJSONMessage), failedTopic);
            counterManager.incrementCounter(getClass(), Counter.CAMPAIGN_NOT_FOUND_IN_CACHE);
        } catch (Exception e) {
            if (failedPreviously) {
                // This was our 2nd try...just bail.
                LOG.log(Level.SEVERE, "Failed to process already-failed: " + event, e);

                // But before we give up completely, let's write the event
                // to a custom "failed twice" CSV file-based log
                failedLogger.info(event.toCsv());
                LOG.warning("Logged message to " + failedLogFile.getAbsolutePath());
                counterManager.incrementCounter(getClass(), Counter.ON_AD_EVENT_FAILED_TWICE);
            } else {
                // This is the first time it has failed
                LOG.warning("Requeueing message due to: " + e.getMessage());

                // Requeue it in the secondary failed message queue
                kafkaProducer.sendMessage(new String(oldJSONMessage), failedTopic);
                counterManager.incrementCounter(getClass(), Counter.ON_AD_EVENT_FAILED_ONCE_REQUEUED);
            }
        }
    }

    void handleAdEvent(AdEvent event) throws java.sql.SQLException {
        Long userAgentId = establishUserAgentId(event);

        // Grab the current version of the domain cache
        DataCollectorDomainCache dataCollectorDomainCache = dataCollectorDomainCacheManager.getCache();

        // Load the Publication from the domain cache...this is required
        PublicationDto publication = dataCollectorDomainCache.getPublicationById(event.getPublicationId());
        if (publication == null) {
            LOG.warning("Publication id=" + event.getPublicationId() + " not found in cache for " + event);
            counterManager.incrementCounter(getClass(), Counter.HANDLE_AD_EVENT_PUBLICATION_NOT_FOUND);
            return;
        }

        AdEventAccounting accounting = null;

        // Load the Campaign from the domain cache if the event specifies a campaignId
        CampaignDto campaign = null;
        if (event.getCampaignId() != null) {
            campaign = dataCollectorDomainCache.getCampaignById(event.getCampaignId());
            if (campaign == null) {
                // campaign is not available in DataCollector Domaincache just yet, check in our local missingCampaignsCache (ehcache)
                Element element = missingCampaignsCache.get(event.getCampaignId());
                if (element != null) {
                    // campaign was found, great!
                    campaign = (CampaignDto) element.getObjectValue();
                } else {
                    // MAX-664 Don't log warnings for specific events on campaigns not in cache...
                    if (!EnumSet.of(AdAction.BID_FAILED, AdAction.UNFILLED_REQUEST).contains(event.getAdAction())) {
                        //campaign was not found in missingCampaignsCache either, log a failed event.
                        LOG.warning("Campaign id=" + event.getCampaignId() + " not found in cache for " + event);
                    }
                    counterManager.incrementCounter(getClass(), Counter.HANDLE_AD_EVENT_CAMPAIGN_NOT_FOUND);
                    throw new CampaignNotFoundInCacheException();
                }
            } else {
                // There's a campaign, so create the accounting wrapper
                accounting = new AdEventAccounting(event, campaign, publication.getPublisher());
            }
        }

        // https://sites.google.com/a/adfonic.com/devteam/account-balance-update-batching
        // Whether or not we'll batch this event depends on whether there's
        // any advertiser spend.
        if (accounting != null && accounting.getAdvertiserSpend() != null) {
            // Batch
            batchManager.addToCurrentBatch(accounting, userAgentId);
            counterManager.incrementCounter(getClass(), Counter.HANDLE_AD_EVENT_BATCHED);
        } else {
            // Don't bother batching
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("Not batching");
            }

            counterManager.incrementCounter(getClass(), Counter.HANDLE_AD_EVENT_NOT_BATCHED);

            // Just log the event, that's all we need to do
            int publisherTimeId = DateUtils.getTimeID(event.getEventTime(), publication.getPublisher().getCompany().getDefaultTimeZone());
            Integer advertiserTimeId = null;
            if (campaign != null) {
                advertiserTimeId = DateUtils.getTimeID(event.getEventTime(), campaign.getAdvertiser().getCompany().getDefaultTimeZone());
            }

            // https://tickets.adfonic.com/browse/BZ-2161
            // We used to pass null for the AdEventAccounting argument.  That caused
            // a bug for RTB, where PAYOUT wasn't getting set at AD_SERVED time for
            // CPC campaigns.  Even though we're not doing any batching or "accounting"
            // (budget updates) in this branch, we still need to pass the accounting
            // object here.  It may be null, sure, but it also may not be...in which
            // case, even though spend is null, payout may not be.
            clusterDao.createAdEventLog(event, accounting, userAgentId, advertiserTimeId, publisherTimeId);
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("Created AdEventLog entry");
            }
        }

        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Done");
        }
    }

    public Long establishUserAgentId(AdEvent event) {
        if (event.getModelId() == null) {
            return null;
        }

        String userAgentHeader = event.getUserAgentHeader();
        if (userAgentHeader == null) {
            return null;
        }

        if (userAgentHeader.length() > MAX_USER_AGENT_LENGTH) {
            LOG.warning("Need to truncate long User-Agent (length=" + userAgentHeader.length() + ") for " + event);
            userAgentHeader = userAgentHeader.substring(0, MAX_USER_AGENT_LENGTH);
            counterManager.incrementCounter(getClass(), Counter.USER_AGENT_TRUNCATED);
        }

        // Resolve the UserAgent by "User-Agent" header + Model
        // First check in our cache
        UserAgent userAgent;
        int currentDate = Integer.valueOf(FastDateFormat.getInstance("yyyyMMdd").format(DateUtils.getStartOfDay(new Date(), TimeZone.getDefault()).getTime()));
        synchronized (userAgentMap) {
            userAgent = (UserAgent) userAgentMap.get(userAgentHeader, event.getModelId());
        }

        if (userAgent == null) {
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("UserAgent not found in cache, querying for it");
            }
            counterManager.incrementCounter(getClass(), Counter.USER_AGENT_CACHE_MISS);
            try {
                userAgent = clusterDao.getOrCreateUserAgent(userAgentHeader, event.getModelId(), currentDate);
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.fine("Determined UserAgent id=" + userAgent.getUserAgentId());
                }
                // Cache it for future use
                // the map just contained userAgentId. Since we need to store the last seen date as well
                // changed it to a UserAgent object.
                cacheUserAgent(userAgentHeader, event.getModelId(), userAgent);
                return userAgent.getUserAgentId();
            } catch (java.sql.SQLException e) {
                LOG.warning("Failed to getOrCreateUserAgent due to: " + e.getMessage() + ", proceeding with " + event);
                counterManager.incrementCounter(getClass(), Counter.USER_AGENT_FAILURE);
            }
        } else {
            // UserAgent was found in cache and 
            if (userAgent.getLastSeen() < currentDate) {
                // the DATE_LAST_SEEN is before the current date
                // do a database update for last seen date if and only if the date in UserAgent is before today's date.
                // so that we only update the database once everyday for any particular useragent
                try {
                    // passing null for connection here as it will be created within updateUserAgent method 
                    // if the connection is null
                    clusterDao.updateUserAgent(null, userAgentHeader, event.getModelId(), currentDate);
                } catch (SQLException e) {
                    LOG.warning("Failed to update UserAgent due to: " + e.getMessage() + ", proceeding with " + event);
                    counterManager.incrementCounter(getClass(), Counter.USER_AGENT_FAILURE);
                }
                // update the DATE_LAST_SEEN to current date
                userAgent.setLastSeen(currentDate);
                // Need to update the the userAgent object in the cache
                // cache the new value with the current date
                cacheUserAgent(userAgentHeader, event.getModelId(), userAgent);
            }
            return userAgent.getUserAgentId();
        }
        return null;
    }

    private void cacheUserAgent(String userAgentHeader, long modelId, UserAgent userAgent) {
        synchronized (userAgentMap) {
            // Need to update the the userAgent object in the cache
            // cache the new value with the current date
            userAgentMap.put(userAgentHeader, modelId, userAgent);
        }
    }

    /**
     * This JMS topic message gets published by PlatformMapper
     */
    public void onUserAgentUpdated(UserAgentUpdatedMessage message) {
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Handling: " + message.toString());
        }
        // Clear any entries in the userAgentMap corresponding to the given User-Agent header
        synchronized (userAgentMap) {
            for (MapIterator iter = userAgentMap.mapIterator(); iter.hasNext();) {
                MultiKey multiKey = (MultiKey) iter.next();
                // this needs to change as we are no longer storing Longs but UserAgent object
                //long userAgentId = (Long)iter.getValue();
                UserAgent userAgent = (UserAgent) iter.getValue();
                if (userAgent.getUserAgentId() == message.getUserAgentId()) {
                    switch (message.getChangeType()) {
                    case DELETE:
                        // The UserAgent was deleted, so just un-cache it
                        if (LOG.isLoggable(Level.FINE)) {
                            LOG.fine("Uncaching: " + multiKey + " => " + userAgent.getUserAgentId());
                        }
                        iter.remove();
                        break;
                    case UPDATE:
                    default:
                        // The UserAgent was updated, so let's make sure we
                        // have correct mappings...and if not, un-cached it.
                        //String uaHeader = (String)multiKey.getKeys()[0];
                        long modelId = (Long) multiKey.getKeys()[1];
                        if (modelId != message.getNewModelId().longValue()) {
                            if (LOG.isLoggable(Level.FINE)) {
                                LOG.fine("Uncaching: " + multiKey + " => " + userAgent.getUserAgentId());
                            }
                            iter.remove(); // un-cache it
                        }
                        break;
                    }
                }
            }
        }
    }
    
    /**
     * De-duplicate a click, return true if ready to be logged, false if duplicated or failed mapping
     */
    public boolean onClickJSONMessage(net.byyd.archive.model.v1.AdEvent ae){
        Impression impression;
        try{ 
            impression = mapper.getImpressionFromAdEvent(ae);
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Failed conversion to Impression...", e);
            counterManager.incrementCounter(getClass(), Counter.ON_CLICK_MESSAGE_DESERIALIZATION_FAILURE);
            return false; // not much else we can do
        }
        
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Handling: " + ae.toString());
        }

        counterManager.incrementCounter(getClass(), Counter.ON_CLICK_MESSAGE);

        if (LOG.isLoggable(Level.FINER)) {
            LOG.finer("Impression: " + impression);
        }

        // Grab the current version of the domain cache
        DataCollectorDomainCache dataCollectorDomainCache = dataCollectorDomainCacheManager.getCache();

        // Load the respective campaign
        CampaignDto campaign = dataCollectorDomainCache.getCampaignById(ae.getCampaignId());
        if (campaign == null) {
            LOG.warning("Campaign id=" + ae.getCampaignId() + " not found in cache");
            // This isn't a show stopper per se...if we aren't able to determine
            // the campaign, then that just means the TTL of the click will be
            // the default, and install tracking won't be done.
            counterManager.incrementCounter(getClass(), Counter.ON_CLICK_MESSAGE_CAMPAIGN_NOT_FOUND);
        }

        // Derive the appropriate click expire time based on whether we'll need
        // to give it a longer lifespan for install or conversion tracking.
        Date expireTime;
        if (campaign != null) {
            expireTime = AdserverUtils.getClickExpireTime(impression, campaign.getApplicationID(), campaign.isInstallTrackingEnabled(), campaign.isInstallTrackingAdXEnabled(),
                    campaign.isConversionTrackingEnabled(), clickDefaultTtlSeconds, installTrackingTtlSeconds, conversionTrackingTtlSeconds);
        } else {
            expireTime = AdserverUtils.getClickExpireTime(impression, null, null, null, null, clickDefaultTtlSeconds, installTrackingTtlSeconds, conversionTrackingTtlSeconds);
        }

        // The ClickService keeps track of whether it has seen a click for this
        // impression yet or not.  The trackClick call will return true if this
        // is a fresh click, or false if this is a duplicate.
        String applicationId = null;
        if (campaign != null && (campaign.isInstallTrackingEnabled() || campaign.isInstallTrackingAdXEnabled())) {
            applicationId = campaign.getApplicationID();
        }
        if (!clickService.trackClick(impression, applicationId, ae.getEventTime(), expireTime, ae.getIpAddress(), ae.getUserAgentHeader())) {
            Click click = clickService.getClickByExternalID(impression.getExternalID());
            if (click != null) {
                // A click for for this impression externalID has already been tracked
                // AF-1686 - compare the campaign of the duplicate click to the
                // already-tracked click.  If they're different, then that is
                // really bad news, indicative of weak randomness in our externalID
                // generation (i.e. when using FastUUID instead of UUID).
                if (click.getCreativeId() != impression.getCreativeId() || click.getAdSpaceId() != impression.getAdSpaceId()) {
                    // Yowsa...not good.
                    LOG.severe("ID COLLISION!!! Duplicate click for " + impression.getExternalID() + " has adSpaceId=" + impression.getAdSpaceId() + ", creativeId="
                            + impression.getCreativeId() + ", but the already-tracked click has adSpaceId=" + click.getAdSpaceId() + ", creativeId=" + click.getCreativeId());
                } else {
                    // This is just a "regular" duplicate click
                    if (LOG.isLoggable(Level.FINE)) {
                        LOG.fine("Duplicate click request, impression externalID=" + impression.getExternalID());
                    }
                }
                counterManager.incrementCounter(getClass(), Counter.ON_CLICK_MESSAGE_DUPLICATE);
            }
            return false; // don't bother logging the event
        }

        // If we got here, we know this click was a fresh one, and it needs to
        // be logged.  Log the ad event and send it to data collector.
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("First click, logging the ad event");
        }

        counterManager.incrementCounter(getClass(), Counter.ON_CLICK_MESSAGE_TRACKED);
        return true;
    }

    MultiKeyMap getUserAgentMap() {
        return userAgentMap;
    }
}
