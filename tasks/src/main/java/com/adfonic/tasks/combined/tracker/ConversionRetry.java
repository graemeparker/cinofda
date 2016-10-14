package com.adfonic.tasks.combined.tracker;

import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.adfonic.adserver.AdEvent;
import com.adfonic.adserver.Click;
import com.adfonic.domain.AdAction;
import com.adfonic.domain.AdSpace;
import com.adfonic.domain.Creative;
import com.adfonic.tracker.ConversionService;
import com.adfonic.tracker.PendingConversion;

/**
 * Performs retries of conversions that tracker scheduled for retry.
 */
@Component
public class ConversionRetry extends AbstractTrackerRetry {

    private final transient Logger LOG = LoggerFactory.getLogger(getClass().getName());

    @Value("${ConversionRetry.batchSize:1000}")
    private int batchSize;
    @Autowired
    private ConversionService conversionService;

    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }

    //@Scheduled(fixedRate=5000)
    public void processPendingConversions() {
        LOG.debug("Processing pending conversions");
        //Overriding batchSize to 0 and it will make it streaming
        // There was an issue while upgrading mysql to mariadb that connectors were
        //converting this statment to SET OPTION SQL_SELECT_LIMIT=batchSize
        //And mariadb do not except OPTION word
        batchSize = 0;

        List<PendingConversion> batch = conversionService.getPendingConversionsToRetry(batchSize);
        LOG.debug("Pending conversions found in batch: {}", batch.size());
        for (PendingConversion pendingConversion : batch) {
            retryPendingConversion(pendingConversion);
        }
    }

    void retryPendingConversion(PendingConversion pendingConversion) {
        boolean scheduleAnotherRetry = true;
        try {
            LOG.debug("Retrying conversion for clickExternalID={}", pendingConversion.getClickExternalID());

            Click click = getClickService().getClick(pendingConversion);
            if (click == null) {
                LOG.info("Click not found for clickExternalID={}, scheduling retry", pendingConversion.getClickExternalID());
            } else {
                // Simply finding the click means this retry was successful,
                // regardless of the outcome of tracking the conversion.
                scheduleAnotherRetry = false;

                trackConversion(click, pendingConversion.getCreationTime());
            }
        } catch (Exception e) {
            LOG.error("Conversion tracking retry failed for clickExternalID={} {}", pendingConversion.getClickExternalID(), e);
        } finally {
            if (scheduleAnotherRetry) {
                LOG.debug("Rescheduling conversion retry for clickExternalID={}", pendingConversion.getClickExternalID());
                conversionService.scheduleRetry(pendingConversion);
            } else {
                // Delete it
                LOG.debug("Deleting scheduled conversion retry for clickExternalID={}", pendingConversion.getClickExternalID());
                conversionService.deleteScheduledConversionRetry(pendingConversion);
            }
        }
    }

    void trackConversion(Click click, Date eventTime) {
        Creative creative = getCreative(click);
        if (creative == null) {
            LOG.error("Failed to load Creative id={} for click.externalID={}", click.getCreativeId(), click.getExternalID());
            return;
        }

        AdSpace adSpace = getAdSpace(click);
        if (adSpace == null) {
            LOG.error("Failed to load AdSpace id={} for click.externalID={}", click.getAdSpaceId(), click.getExternalID());
            return;
        }

        if (!creative.getCampaign().isConversionTrackingEnabled()) {
            LOG.warn("Not tracking conversion for click.externalID={}, Creative id={}, conversion tracking not enabled on Campaign id={}", click.getExternalID(), creative.getId(),
                    creative.getCampaign().getId());
            return;
        }

        if (!conversionService.trackConversion(click)) {
            LOG.warn("Duplicate conversion tracking request, click.externalID={}, Creative id={}, AdSpace id={}", click.getExternalID(), creative.getId(), adSpace.getId());
            return;
        }

        // SC-134 - device identifiers now get logged with the AdEvent
        getClickService().loadDeviceIdentifiers(click);

        AdEvent event = getAdEventFactory().newInstance(AdAction.CONVERSION);
        event.populate(click, creative.getCampaign().getId(), adSpace.getPublication().getId());
        event.setEventTime(eventTime, click.getUserTimeZone()); // important: use the actual time of the original event, not the retry
        net.byyd.archive.model.v1.AdEvent ae = getJSONAdEvent(event);
        logAdEvent(ae);
    }
}
