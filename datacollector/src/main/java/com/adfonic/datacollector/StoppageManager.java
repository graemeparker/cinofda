package com.adfonic.datacollector;

import java.util.Date;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Resource;
import javax.jms.Topic;

import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

import com.adfonic.datacollector.dao.ToolsDao;
import com.adfonic.domain.AdvertiserStoppage;
import com.adfonic.domain.CampaignStoppage;
import com.adfonic.domain.cache.dto.datacollector.campaign.AdvertiserDto;
import com.adfonic.domain.cache.dto.datacollector.campaign.CampaignDto;
import com.adfonic.jms.JmsUtils;
import com.adfonic.jms.StopAdvertiserMessage;
import com.adfonic.jms.StopCampaignMessage;
import com.adfonic.jms.UnStopAdvertiserMessage;
import com.adfonic.jms.UnStopCampaignMessage;
import com.adfonic.util.DateUtils;

/**
 * Manager of advertiser and campaign stoppages
 */
@Component
public class StoppageManager {

    private static final transient Logger LOG = Logger.getLogger(StoppageManager.class.getName());

    enum DuplicateOrNew {
        DUPLICATE, NEW
    }

    @Autowired
    private ToolsDao toolsDao;
    @Resource(name = "campaignStoppageCache")
    private Ehcache campaignStoppageCache;
    @Resource(name = "advertiserStoppageCache")
    private Ehcache advertiserStoppageCache;
    @Resource(name = "missingCampaignsCache")
    private Ehcache missingCampaignsCache;
    @Autowired
    private JmsUtils jmsUtils;
    @Autowired
    @Qualifier("centralJmsTemplate")
    private JmsTemplate centralJmsTemplate;
    @Autowired
    @Qualifier("stopCampaignTopic")
    private Topic stopCampaignTopic;
    @Autowired
    @Qualifier("stopAdvertiserTopic")
    private Topic stopAdvertiserTopic;

    public void stopCampaign(Date eventTime, CampaignDto campaign, CampaignStoppage.Reason reason) throws java.sql.SQLException {
        Date timestamp = new Date();
        TimeZone timeZone = campaign.getAdvertiser().getCompany().getDefaultTimeZone();
        Date reactivateDate = calculateReactivateDate(reason, eventTime, timeZone);

        // Before we proceed, it's conceivable that the reactivate date would
        // have already occurred.  This would be the case if datacollector has
        // a backlog, and we're processing the given event on a different day
        // than it originally occurred.  This could be as simple as...the event
        // occurred just before midnight, and we're processing it just after
        // midnight.  If reactivateDate has passed, just bail.
        if (hasReactivateDatePassed(reactivateDate)) {
            if (LOG.isLoggable(Level.INFO)) {
                LOG.info("Not creating CampaignStoppage for Campaign id=" + campaign.getId() + ", reason=" + reason + ", reactivateDate elapsed already: " + reactivateDate
                        + " (tzID=" + timeZone.getID() + ")");
            }
            return;
        }

        // De-dup.  In the case of processing a backlog, from time to time we
        // see a scenario where a slew of ad events come in for a given Campaign
        // that should be stopped due to budget overage.  The events may continue
        // to pour in since they're in a backlog generated *before* the Campaign
        // got stopped.  Prior to this change, we didn't detect the duplicates,
        // and we happily stopped the same Campaign over and over.  The problem
        // with this is that we sent stoppage JMS messages to the given topic
        // over and over, which ends up flooding adservers with duplicate messages
        // that it's not well equipped to handle -- since it typically dedicates
        // only a small number of consumers (if multiple at all) to consuming
        // those messages.
        //
        // Ok, that said, let's de-dup stoppages by tracking (in a local in-JVM
        // ehcache instance) the recent stoppages we've detected and published.
        if (DuplicateOrNew.DUPLICATE.equals(deDup(campaign.getId(), reactivateDate, campaignStoppageCache))) {
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("Already handled CampaignStoppage for Campaign id=" + campaign.getId() + ", reactivateDate=null");
            }
            return;
        }

        // Create a stoppage condition for the campaign
        if (LOG.isLoggable(Level.INFO)) {
            LOG.info("Creating a CampaignStoppage for Campaign id=" + campaign.getId() + ", reason=" + reason + ", reactivateDate=" + reactivateDate + " (tzID=" + timeZone.getID()
                    + ")");
        }
        toolsDao.createCampaignStoppage(campaign.getId(), reason, timestamp, reactivateDate);

        // Publish a JMS message to anybody who might be interested (adserver)
        jmsUtils.sendObject(centralJmsTemplate, stopCampaignTopic, new StopCampaignMessage(campaign.getId(), reason.name(), timestamp, reactivateDate));
    }

    public void stopAdvertiser(Date eventTime, AdvertiserDto advertiser, AdvertiserStoppage.Reason reason) throws java.sql.SQLException {
        Date timestamp = new Date();
        TimeZone timeZone = advertiser.getCompany().getDefaultTimeZone();
        Date reactivateDate = calculateReactivateDate(reason, eventTime, timeZone);

        // Before we proceed, it's conceivable that the reactivate date would
        // have already occurred.  This would be the case if datacollector has
        // a backlog, and we're processing the given event on a different day
        // than it originally occurred.  This could be as simple as...the event
        // occurred just before midnight, and we're processing it just after
        // midnight.  If reactivateDate has passed, just bail.
        if (hasReactivateDatePassed(reactivateDate)) {
            if (LOG.isLoggable(Level.INFO)) {
                LOG.info("Not creating AdvertiserStoppage for Advertiser id=" + advertiser.getId() + ", reason=" + reason + ", reactivateDate elapsed already: " + reactivateDate
                        + " (tzID=" + timeZone.getID() + ")");
            }
            return;
        }

        // De-dup.  In the case of processing a backlog, from time to time we
        // see a scenario where a slew of ad events come in for a given Advertiser
        // that should be stopped due to budget overage.  The events may continue
        // to pour in since they're in a backlog generated *before* the Advertiser
        // got stopped.  Prior to this change, we didn't detect the duplicates,
        // and we happily stopped the same Advertiser over and over.  The problem
        // with this is that we sent stoppage JMS messages to the given topic
        // over and over, which ends up flooding adservers with duplicate messages
        // that it's not well equipped to handle -- since it typically dedicates
        // only a small number of consumers (if multiple at all) to consuming
        // those messages.
        //
        // Ok, that said, let's de-dup stoppages by tracking (in a local in-JVM
        // ehcache instance) the recent stoppages we've detected and published.
        if (DuplicateOrNew.DUPLICATE.equals(deDup(advertiser.getId(), reactivateDate, advertiserStoppageCache))) {
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("Already handled AdvertiserStoppage for Advertiser id=" + advertiser.getId() + ", reactivateDate=null");
            }
            return;
        }

        // Create a stoppage condition for the advertiser
        if (LOG.isLoggable(Level.INFO)) {
            LOG.info("Creating a AdvertiserStoppage for Advertiser id=" + advertiser.getId() + ", reason=" + reason + ", reactivateDate=" + reactivateDate + " (tzID="
                    + timeZone.getID() + ")");
        }
        toolsDao.createAdvertiserStoppage(advertiser.getId(), reason, timestamp, reactivateDate);

        // Publish a JMS message to anybody who might be interested (adserver)
        jmsUtils.sendObject(centralJmsTemplate, stopAdvertiserTopic, new StopAdvertiserMessage(advertiser.getId(), reason.name(), timestamp, reactivateDate));
    }

    /**
     * Listen to the UnstopAdvertiserMessage sent from task when we need to unstop an advertiser.
     * We remove the concerning stoppage message from the cache based on the message received from task.
     * 
     * scenario: It could be possible that if a Advertiser has reached their daily / overall budget at 1:00 PM and 
     * received a notification about it and the Advertiser is stopped at 1:00 PM. The advertiser then increase daily/overall 
     * budget at 1:30 PM and reach their second budget limit at 1:45 PM. Since the stoppage message was being cached the 
     * advertiser wouldn't be stopped until 2:00 PM as the stoppage message was being cached for 3600 seconds until evicted
     * 
     * Now as soon as the Advertiser is unstopped by tasks, it is handled by the following method by removing the respective Advertiser from the cache.  
     * @param msg
     */

    public void onUnStopAdvertiser(UnStopAdvertiserMessage msg) {
        if (LOG.isLoggable(Level.INFO)) {
            LOG.info("Handling: " + msg);
        }

        if (advertiserStoppageCache != null) {
            Element element = advertiserStoppageCache.get(msg.getAdvertiserId());
            if (element != null) {
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.fine("Removing AdvertiserStoppage for Advertiser id=" + msg.getAdvertiserId());
                }
                advertiserStoppageCache.remove(msg.getAdvertiserId());
            }
        }
    }

    /**
     * Listen to the UnstopCampaignrMessage sent from task when we need to unstop an campaign.
     * We remove the concerning stoppage message from the cache based on the message received from task.
     * 
     * scenario: It could be possible that if a campaign has reached it's daily / overall budget at 1:00 PM and 
     * received a notification about it and the Campaign is stopped at 1:00 PM. The campaign then has it's daily/overall 
     * budget increased at 1:30 PM and reaches the second budget limit at 1:45 PM. Since the stoppage message was being cached the 
     * campaign wouldn't be stopped until 2:00 PM as the stoppage message was being cached for 3600 seconds until evicted.
     * 
     * Now as soon as the Campaign is unstopped by tasks, it is handled by the following method by removing the respective campaign from the cache.
     * @param msg
     */
    public void onUnStopCampaign(UnStopCampaignMessage msg) {
        if (LOG.isLoggable(Level.INFO)) {
            LOG.info("Handling: " + msg);
        }

        if (campaignStoppageCache != null) {
            Element element = campaignStoppageCache.get(msg.getCampaignId());
            if (element != null) {
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.fine("Removing CampaignStoppage for Campaign id=" + msg.getCampaignId());
                }
                campaignStoppageCache.remove(msg.getCampaignId());
            }
        }

        //MAX-165
        if (missingCampaignsCache != null) {
            //since we are unstopping the campaign, the campaign details might have changed, so we remove it 
            //and wait till there is a need refresh this campaign again
            missingCampaignsCache.remove(msg.getCampaignId());
        }
    }

    /**
     * Calculate a reactivation date for an advertiser stoppage.
     * If the stoppage is related to a daily budget, the reactivation
     * date will be "start of day tomorrow" in the given time zone.
     * For any other type of stoppage (i.e. overall budget), the
     * reactivation date will be null, meaning stopped indefinitely.
     */
    static Date calculateReactivateDate(AdvertiserStoppage.Reason reason, Date eventTime, TimeZone timeZone) {
        if (AdvertiserStoppage.Reason.DAILY_BUDGET.equals(reason)) {
            // For this particular mode of stopping, we only need to stop
            // the advertiser until "tomorrow" (relative to the event time)
            // in the advertiser's time zone.
            return DateUtils.getStartOfDayTomorrow(eventTime, timeZone);
        } else {
            return null; // no automatic reactivation
        }
    }

    /**
     * Calculate a reactivation date for a campaign stoppage.
     * If the stoppage is related to a daily budget, the reactivation
     * date will be "start of day tomorrow" in the given time zone.
     * For any other type of stoppage (i.e. overall budget), the
     * reactivation date will be null, meaning stopped indefinitely.
     */
    static Date calculateReactivateDate(CampaignStoppage.Reason reason, Date eventTime, TimeZone timeZone) {
        if (CampaignStoppage.Reason.HOURLY_BUDGET.equals(reason)) {
            // For this particular mode of stopping, we are stopping the 
            // campaign until the 'next hour' (relative to the event time)
            // in the campaign's time zone
            return DateUtils.getStartOfNextHour(eventTime, timeZone);
        } else if (CampaignStoppage.Reason.DAILY_BUDGET.equals(reason)) {
            // For this particular mode of stopping, we only need to stop
            // the campaign until "tomorrow" (relative to the event time)
            // in the campaign's time zone.
            return DateUtils.getStartOfDayTomorrow(eventTime, timeZone);
        } else {
            return null; // no automatic reactivation
        }
    }

    /**
     * Has a reactivate date passed?
     * @return true if the reactivate date is not null and has already elapsed
     */
    static boolean hasReactivateDatePassed(Date reactivateDate) {
        return reactivateDate != null && reactivateDate.getTime() <= System.currentTimeMillis();
    }

    /**
     * De-duplicate a stoppage request.  This is a generic method that
     * can de-dup either campaign or advertiser stoppages, or what not.
     * Just pass in the id of the campaign or advertiser or what not,
     * along with the respective cache, and the calculated reactivate
     * date, and this method will return DUPLICATE if it has already
     * been processed and cached, or NEW if it hasn't yet been processed.
     * If it's NEW, it will be cached for future checks.
     * @param id the id of the campaign, advertiser, what have you
     * @param reactivateDate the calculated reactivation date
     * @param cache the respective de-dup cache to use
     * @return either DUPLICATE or NEW
     */
    static DuplicateOrNew deDup(long id, Date reactivateDate, Ehcache cache) {
        Element element = cache.get(id);
        if (element != null) {
            Date cachedReactivateDate = (Date) element.getValue();
            if ((cachedReactivateDate == null && reactivateDate == null) || (cachedReactivateDate != null && cachedReactivateDate.equals(reactivateDate))) {
                // This stoppage was recently encountered and already handled/cached
                return DuplicateOrNew.DUPLICATE;
            }
        }

        // Before we go any further, cache it so that any other consumer threads
        // in this instance that need to handle the same stoppage don't bother.
        // TODO: set a shorter TTL on this!!!
        cache.put(new Element(id, reactivateDate));
        return DuplicateOrNew.NEW;
    }
}
