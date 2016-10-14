package com.adfonic.tracker.controller;

import java.util.LinkedHashMap;
import java.util.Map;

import net.byyd.archive.model.v1.V1DomainModelMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.adfonic.adserver.AdEvent;
import com.adfonic.adserver.Click;
import com.adfonic.domain.AdSpace;
import com.adfonic.domain.AdSpace_;
import com.adfonic.domain.Creative;
import com.adfonic.domain.Creative_;
import com.adfonic.tracker.ClickService;
import com.adfonic.tracker.VideoViewAdEventLogic;
import com.adfonic.tracker.VideoViewService;
import com.adfonic.tracker.kafka.TrackerKafka;
import com.byyd.middleware.creative.service.CreativeManager;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.jpa.FetchStrategyBuilder;
import com.byyd.middleware.publication.service.PublicationManager;

@Controller
public class VideoViewController extends AbstractTrackerController {
    private static final transient Logger LOG = LoggerFactory.getLogger(VideoViewController.class.getName());

    static final String SUCCESS = "success";
    static final String ERROR = "error";
    static final String ERROR_INVALID_VALUE_FOR_CLIPMS = "Invalid value for clipMs";
    static final String ERROR_UNKNOWN_CLICK_IDENTIFIER = "Unknown click identifier";
    static final String ERROR_DUPLICATE = "Duplicate";
    static final String ERROR_INTERNAL_ERROR = "Internal error";

    // Package to expose these to unit tests
    static final FetchStrategy CREATIVE_FETCH_STRATEGY = new FetchStrategyBuilder().addInner(Creative_.campaign).build();

    static final FetchStrategy AD_SPACE_FETCH_STRATEGY = new FetchStrategyBuilder().addInner(AdSpace_.publication).build();

    @Autowired
    private CreativeManager creativeManager;
    @Autowired
    private PublicationManager publicationManager;
    @Autowired
    private ClickService clickService;
    @Autowired
    private VideoViewService videoViewService;
    @Autowired
    private VideoViewAdEventLogic videoViewAdEventLogic;
    @Value("${VideoViewController.minimumViewMs}")
    private int minimumViewMs;
    @Autowired
    private TrackerKafka trackerKafka;
    @Autowired
    private V1DomainModelMapper mapper;

    @RequestMapping("/vs/{clickExternalID}")
    @ResponseBody
    public Map<String, Object> trackVideoView(@PathVariable String clickExternalID, @RequestParam(required = false) Integer viewMs, @RequestParam(required = false) Integer clipMs) {
        LOG.debug("Handling video view tracking request for clickExternalID=" + clickExternalID + ", viewMs=" + viewMs + ", clipMs=" + clipMs);

        // This map is our JSON response
        Map<String, Object> response = new LinkedHashMap<String, Object>();

        // Validation...
        // 0. Yospace will call us with *no* viewMs/clipMs at the very start of the
        // video request.  We can silently ignore these requests.
        // 1. Reject requests where clipMs is zero or negative, that makes no sense
        // 2. Reject requests where viewMs is greater than clipMs, that's bogus
        // 3. Quietly ignore requests where viewMs is less than a configurable threshold
        if (viewMs == null || clipMs == null) {
            LOG.debug("Ignoring initial video tracking request with omitted duration params");
            // Quietly ignore this but tell the caller it was a raving success
            response.put(SUCCESS, 1);
            return response;
        } else if (clipMs <= 0) {
            LOG.warn("Invalid clipMs ({}) for clickExternalID={}", clipMs, clickExternalID);
            response.put(SUCCESS, 0);
            response.put(ERROR, ERROR_INVALID_VALUE_FOR_CLIPMS);
            return response;
        } else if (viewMs > clipMs) {
            LOG.warn("Invalid viewMs ({}, exceeds clipMs={}) for clickExternalID={}", viewMs, clipMs, clickExternalID);
            response.put(SUCCESS, 0);
            response.put(ERROR, "Invalid viewMs (" + viewMs + "), cannot exceed clipMs (" + clipMs + ")");
            return response;
        } else if (viewMs < minimumViewMs) {
            LOG.debug("Discarding request, viewMs ({}) < threshold ({}) for clickExternalID={}", viewMs, minimumViewMs, clickExternalID);
            // Even though we're not doing diddly with this, we still let the
            // caller think it was a raving success.
            response.put(SUCCESS, 1);
            return response;
        }

        Click click = clickService.getClickByExternalID(clickExternalID);
        if (click == null) {
            LOG.info("Click not found for clickExternalID={}, scheduling retry", clickExternalID);
            videoViewService.scheduleVideoViewRetry(clickExternalID, viewMs, clipMs);
            response.put(SUCCESS, 1);
            return response;
        }

        // Make sure we haven't already logged the videoView
        if (!videoViewService.trackVideoView(click, viewMs, clipMs)) {
            LOG.warn("Duplicate video view tracking request, click externalID={}", clickExternalID);
            response.put(SUCCESS, 0);
            response.put(ERROR, ERROR_DUPLICATE);
            return response;
        }

        Creative creative = creativeManager.getCreativeById(click.getCreativeId(), CREATIVE_FETCH_STRATEGY);
        AdSpace adSpace = publicationManager.getAdSpaceById(click.getAdSpaceId(), AD_SPACE_FETCH_STRATEGY);
        if (creative == null || adSpace == null) {
            LOG.error("Failed to load a required object, clickExternalID={}, creativeId={}, adSpaceId={}", clickExternalID, click.getCreativeId(), click.getAdSpaceId());
            response.put(SUCCESS, 0);
            response.put(ERROR, ERROR_INTERNAL_ERROR);
            return response;
        }

        // SC-134 - device identifiers now get logged with the AdEvent
        clickService.loadDeviceIdentifiers(click);

        // There may be multiple events to log.  We'll always log a VIEW_Qn event,
        // but we may also need to log a COMPLETED_VIEW event.  This call gives
        // us the list of events to log, and we just log each one in sequence.
        for (AdEvent adEvent : videoViewAdEventLogic.getAdEventsToLog(click, viewMs, clipMs, creative.getCampaign().getId(), adSpace.getPublication().getId())) {
            LOG.debug("For clickExternalID={}, viewMs={}, clipMs={}, logging {}", clickExternalID, viewMs, clipMs, adEvent.getAdAction());
            
            //Log the event to kafka
            try {
                net.byyd.archive.model.v1.AdEvent ae = mapper.map(adEvent);
                LOG.info("Logging to kafka INSTALL AdEvent for Creative id={}, AdSpace id={}", ae.getCreativeId(), ae.getAdSpaceId());
                trackerKafka.logAdEvent(ae);
            } catch (Exception e) {
                LOG.error("Error logging to kafka " + e.getMessage());
            }
        }

        response.put(SUCCESS, 1);
        return response;
    }
}
