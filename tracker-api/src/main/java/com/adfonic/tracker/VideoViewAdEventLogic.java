package com.adfonic.tracker;

import java.util.ArrayList;
import java.util.List;

import com.adfonic.adserver.AdEvent;
import com.adfonic.adserver.AdEventFactory;
import com.adfonic.adserver.Click;
import com.adfonic.domain.AdAction;

public class VideoViewAdEventLogic {
    private static final int ADEVENT_TO_LOG_SIZE = 2;
    private static final double Q2_THRESHOLD = 0.25;
    private static final double Q3_THRESHOLD = 0.5;
    private static final double Q4_THRESHOLD = 0.75;
    
    private final AdEventFactory adEventFactory;
    
    public VideoViewAdEventLogic(AdEventFactory adEventFactory) {
        this.adEventFactory = adEventFactory;
    }

    /**
     * When tracking a video view, get the list of AdEvents to log.  Normally
     * it will be just a single VIEW_Qn event, but it may also have a
     * COMPLETED_VIEW event to go along with it.
     */
    public List<AdEvent> getAdEventsToLog(Click click, int viewMs, int clipMs, long campaignId, long publicationId) {
        // Log the event via data collector using the values from the initial Click,
        // with the AdAction indicating the portion of the video that was viewed.
        AdAction adAction;
        double portion = (double)viewMs / (double)clipMs;
        if (portion < Q2_THRESHOLD) {
            adAction = AdAction.VIEW_Q1;
        } else if (portion < Q3_THRESHOLD) {
            adAction = AdAction.VIEW_Q2;
        } else if (portion < Q4_THRESHOLD) {
            adAction = AdAction.VIEW_Q3;
        } else {
            adAction = AdAction.VIEW_Q4;
        }

        List<AdEvent> adEventsToLog = new ArrayList<AdEvent>(ADEVENT_TO_LOG_SIZE);
        
        // We always log the VIEW_Qn event
        AdEvent viewEvent = adEventFactory.newInstance(adAction);
        viewEvent.populate(click, campaignId, publicationId);
        viewEvent.setActionValue(viewMs);
        adEventsToLog.add(viewEvent);

        // If the user viewed the entire video, we also log a COMPLETED_VIEW event
        if (viewMs == clipMs) {
            AdEvent completedViewEvent = adEventFactory.newInstance(AdAction.COMPLETED_VIEW);
            completedViewEvent.populate(click, campaignId, publicationId);
            // don't set the actionValue on this event
            adEventsToLog.add(completedViewEvent);
        }

        return adEventsToLog;
    }
}
