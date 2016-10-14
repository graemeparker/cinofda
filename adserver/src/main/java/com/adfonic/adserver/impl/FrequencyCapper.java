package com.adfonic.adserver.impl;

import java.util.Map;
import java.util.Objects;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.adfonic.adserver.FrequencyCounter;
import com.adfonic.adserver.TargetingContext;
import com.adfonic.domain.TrackingIdentifierType;
import com.adfonic.domain.cache.dto.adserver.creative.CampaignDto;
import com.adfonic.domain.cache.dto.adserver.creative.CreativeDto;

@Component
public class FrequencyCapper {

    private final transient Logger LOG = LoggerFactory.getLogger(getClass());

    // These properties are injected at app context startup by Spring, but these
    // default values are what were in production config as of Oct 21, 2011.
    @Value("${frequencyCap.maxImpressions}")
    private int frequencyCapMaxImpressions = 0;
    @Value("${frequencyCap.periodSec}")
    private int frequencyCapPeriodSec = 3600;
    @Value("${frequencyCap.blockWrites:false}")
    private boolean frequencyCapBlockWrites = false;

    private final FrequencyCounter frequencyCounter;

    @Autowired
    public FrequencyCapper(FrequencyCounter frequencyCounter) {
        Objects.requireNonNull(frequencyCounter);
        this.frequencyCounter = frequencyCounter;
    }

    public int getImpressionCapCount(CampaignDto campaign) {
        Integer count = campaign.getCapImpressions();
        if (count != null) {
            return count;
        } else {
            return frequencyCapMaxImpressions;
        }
    }

    public int getImpressionCapPeriod(CampaignDto campaign) {
        Integer count = campaign.getCapPeriodSeconds();
        if (count != null) {
            return count;
        } else {
            return frequencyCapPeriodSec;
        }
    }

    public int getImpressionCount(String uniqueId, CreativeDto creative) {
        long entityId;
        FrequencyCounter.FrequencyEntity frequencyEntity;
        CampaignDto campaign = creative.getCampaign();
        if (campaign.isCapPerCampaign()) {
            entityId = campaign.getId();
            frequencyEntity = FrequencyCounter.FrequencyEntity.CAMPAIGN;
        } else {
            entityId = creative.getId();
            frequencyEntity = FrequencyCounter.FrequencyEntity.CREATIVE;
        }
        int periodSec = getImpressionCapPeriod(campaign);
        return frequencyCounter.getFrequencyCount(uniqueId, entityId, periodSec, frequencyEntity);
    }

    public Integer checkAndDecrement(TargetingContext context, CreativeDto creative) {
        CampaignDto campaign = creative.getCampaign();
        int capCount = getImpressionCapCount(campaign);
        if (capCount > 0) {
            long freqCapEntityId;
            FrequencyCounter.FrequencyEntity frequencyEntity;
            if (campaign.isCapPerCampaign()) {
                freqCapEntityId = campaign.getId();
                frequencyEntity = FrequencyCounter.FrequencyEntity.CAMPAIGN;
            } else {
                freqCapEntityId = creative.getId();
                frequencyEntity = FrequencyCounter.FrequencyEntity.CREATIVE;
            }

            Integer capPeriodSeconds = getImpressionCapPeriod(campaign);
            String uniqueId = FrequencyCapper.getUniqueIdForFrequencyCounter(context);
            if (uniqueId != null) {
                return frequencyCounter.decrementFrequencyCount(uniqueId, freqCapEntityId, capPeriodSeconds, frequencyEntity);
            } else {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Unable to generate unique id with " + frequencyEntity + " id=" + freqCapEntityId);
                }
            }
        }
        return null;
    }

    /**
     * This method will increment and then return a counter of how many
     * times the given user (identified by the targeting context) has
     * seen a given creative in a given amount of time.
     *
     * @param context   the targeting context
     * @param entityId  the entity id whose the counter will be incremented 
     * @param periodSec the time period, in seconds
     * @param frequencyEnitity defines the entity linked to the counter (CREATIVE, CAMPAIGN)
     */
    public int incrementAndGetImpressionCount(TargetingContext context, long entityId, int periodSec, FrequencyCounter.FrequencyEntity frequencyEnitity) {
        if (frequencyCounter != null) {
            String uniqueId = getUniqueIdForFrequencyCounter(context);
            if (uniqueId == null) {
                return -1; // We can't track the count
            }
            if (frequencyCapBlockWrites) {
                // For testing, frequency counter writes have been blocked.  That way
                // we can load test in conjunction with a live frequency counter cache.
                // We just don't write to it -- we only read from it.
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Frequency counter writes blocked, only getting (not incrementing) for " + frequencyEnitity + " id=" + entityId);
                }
                return frequencyCounter.getFrequencyCount(uniqueId, entityId, periodSec, frequencyEnitity);
            } else {
                return frequencyCounter.incrementFrequencyCount(uniqueId, entityId, periodSec, frequencyEnitity);
            }
        } else {
            return -1; // We can't track the count
        }
    }

    /**
     * This method will decrement and then return a counter of how many
     * times the given user (identified by the targeting context) has
     * seen a given creative in a given amount of time.
     *
     * @param context   the targeting context
     * @param entityId  the entity id whose the counter will be decremented 
     * @param periodSec the time period, in seconds
     * @param frequencyEnitity defines the entity linked to the counter (CREATIVE, CAMPAIGN)
     */
    public int decrementAndGetImpressionCount(TargetingContext context, long entityId, int periodSec, FrequencyCounter.FrequencyEntity frequencyEnitity) {
        if (frequencyCounter != null) {
            String uniqueId = getUniqueIdForFrequencyCounter(context);
            if (uniqueId == null) {
                return -1; // We can't track the count
            }
            if (frequencyCapBlockWrites) {
                // For testing, frequency counter writes have been blocked.  That way
                // we can load test in conjunction with a live frequency counter cache.
                // We just don't write to it -- we only read from it.
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Frequency counter writes blocked, only getting (not incrementing) for " + frequencyEnitity + " id=" + entityId);
                }
                return frequencyCounter.getFrequencyCount(uniqueId, entityId, periodSec, frequencyEnitity);
            } else {
                return frequencyCounter.decrementFrequencyCount(uniqueId, entityId, periodSec, frequencyEnitity);
            }
        } else {
            return -1; // We can't track the count
        }
    }

    /**
     * This method will simply return a counter of how many times the
     * given user (identified by the targeting context) has seen a
     * given creative in a given amount of time. It does not increment.
     *
     * @param context   the targeting context
     * @param entityId  the entity id whose the counter will be given 
     * @param periodSec the time period, in seconds
     * @param frequencyEnitity defines the entity linked to the counter (CREATIVE, CAMPAIGN)
     */
    public int getImpressionCount(TargetingContext context, long entityId, int periodSec, FrequencyCounter.FrequencyEntity frequencyEnitity) {
        if (frequencyCounter != null) {
            String uniqueId = getUniqueIdForFrequencyCounter(context);
            if (uniqueId != null) {
                return frequencyCounter.getFrequencyCount(uniqueId, entityId, periodSec, frequencyEnitity);
            } else {
                return -1; // We can't track the count
            }
        } else {
            return -1;
        }
    }

    /**
     * Generate a unique identifier that can be passed into the FrequencyCounter.
     * It comprises the TrackingIdentifierType and the tracking identifier.
     *
     * @return the generated unique identifier or null
     */
    public static String getUniqueIdForFrequencyCounter(TargetingContext context) {
        // AF-1062 - if we were passed any device identifier(s), use the one whose type
        // has the highest precedence.  r.id is ranked below all others.
        Map<Long, String> publisherSuppliedDeviceIdentifiers = context.getAttribute(TargetingContext.DEVICE_IDENTIFIERS);
        if (MapUtils.isNotEmpty(publisherSuppliedDeviceIdentifiers)) {
            // Use the highest precedence device identifier XXX WTF??? precence via entrySet().iterator().next() ?
            Map.Entry<Long, String> firstEntry = publisherSuppliedDeviceIdentifiers.entrySet().iterator().next();
            return firstEntry.getKey() + "." + firstEntry.getValue();
        }

        // No device identifiers supplied...use the tracking id
        // Note: use the secure form so we don't use in-the-clear tracking ids
        // as cache keys
        String trackingId = context.getAttribute(TargetingContext.SECURE_TRACKING_ID);
        if (StringUtils.isBlank(trackingId)) {
            //throw new UniqueIdException("No tracking identifier, can't generate unique id - SECURE_TRACKING_ID: " + trackingId);
            return null;
        }

        // The TrackingIdentifierType is the first half of the unique identifier.
        // It will also have been established for us.
        TrackingIdentifierType tiType = context.getAttribute(TargetingContext.TRACKING_IDENTIFIER_TYPE);

        return new StringBuilder(tiType.name().length() + 1 + trackingId.length()) // pre-size
                .append(tiType.name()).append('.').append(trackingId).toString();
    }

}
