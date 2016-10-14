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
import com.adfonic.domain.AdSpace;
import com.adfonic.domain.Creative;
import com.adfonic.tracker.PendingVideoView;
import com.adfonic.tracker.VideoViewAdEventLogic;
import com.adfonic.tracker.VideoViewService;

/**
 * Performs retries of video views that tracker scheduled for retry.
 */
@Component
public class VideoViewRetry extends AbstractTrackerRetry {

    private final transient Logger LOG = LoggerFactory.getLogger(getClass().getName());

    @Value("${VideoViewRetry.batchSize:1000}")
    private int batchSize;
    @Autowired
    private VideoViewService videoViewService;
    @Autowired
    private VideoViewAdEventLogic videoViewAdEventLogic;

    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }

    //@Scheduled(fixedRate=5000)
    public void processPendingVideoViews() {
        LOG.debug("Processing pending video views");
        //Overriding batchSize to 0 and it will make it streaming
        // There was an issue while upgrading mysql to mariadb that connectors were
        //converting this statment to SET OPTION SQL_SELECT_LIMIT=batchSize
        //And mariadb do not except OPTION word
        batchSize = 0;
        List<PendingVideoView> batch = videoViewService.getPendingVideoViewsToRetry(batchSize);
        LOG.debug("Pending video views found in batch: {}", batch.size());
        for (PendingVideoView pendingVideoView : batch) {
            retryPendingVideoView(pendingVideoView);
        }
    }

    void retryPendingVideoView(PendingVideoView pendingVideoView) {
        boolean scheduleAnotherRetry = true;
        try {
            LOG.debug("Retrying video view for clickExternalID={}", pendingVideoView.getClickExternalID());

            Click click = getClickService().getClick(pendingVideoView);
            if (click == null) {
                LOG.info("Click not found for clickExternalID={}, scheduling retry", pendingVideoView.getClickExternalID());
            } else {
                // Simply finding the click means this retry was successful,
                // regardless of the outcome of tracking the videoView.
                scheduleAnotherRetry = false;

                trackVideoView(click, pendingVideoView.getViewMs(), pendingVideoView.getClipMs(), pendingVideoView.getCreationTime());
            }
        } catch (Exception e) {
            LOG.error("VideoView tracking retry failed for clickExternalID={} {}", pendingVideoView.getClickExternalID(), e);
        } finally {
            if (scheduleAnotherRetry) {
                LOG.debug("Rescheduling video view retry for clickExternalID={}", pendingVideoView.getClickExternalID());
                videoViewService.scheduleRetry(pendingVideoView);
            } else {
                // Delete it
                LOG.debug("Deleting scheduled video view retry for clickExternalID={}", pendingVideoView.getClickExternalID());
                videoViewService.deleteScheduledVideoViewRetry(pendingVideoView);
            }
        }
    }

    void trackVideoView(Click click, int viewMs, int clipMs, Date eventTime) {
        if (!videoViewService.trackVideoView(click, viewMs, clipMs)) {
            LOG.warn("Duplicate video view tracking request, click.externalID={}", click.getExternalID());
            return;
        }

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

        // SC-134 - device identifiers now get logged with the AdEvent
        getClickService().loadDeviceIdentifiers(click);

        // There may be multiple events to log.  We'll always log a VIEW_Qn event,
        // but we may also need to log a COMPLETED_VIEW event.  This call gives
        // us the list of events to log, and we just log each one in sequence.
        for (AdEvent event : videoViewAdEventLogic.getAdEventsToLog(click, viewMs, clipMs, creative.getCampaign().getId(), adSpace.getPublication().getId())) {
            LOG.debug("Logging {} for click.externalID={}, viewMs={}, clipMs={}", event.getAdAction(), click.getExternalID(), viewMs, clipMs);
            event.setEventTime(eventTime, click.getUserTimeZone()); // important: use the actual time of the original event, not the retry
            try{
                net.byyd.archive.model.v1.AdEvent ae = getJSONAdEvent (event);
                logAdEvent(ae);
            }catch (Exception e){
                LOG.error("Error logging to kafka " + e.getMessage());
            }
        }
    }
}
