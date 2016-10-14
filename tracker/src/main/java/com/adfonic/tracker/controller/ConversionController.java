package com.adfonic.tracker.controller;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.byyd.archive.model.v1.V1DomainModelMapper;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.adfonic.adserver.AdEvent;
import com.adfonic.adserver.AdEventFactory;
import com.adfonic.adserver.AdserverConstants;
import com.adfonic.adserver.Click;
import com.adfonic.domain.AdAction;
import com.adfonic.domain.AdSpace;
import com.adfonic.domain.AdSpace_;
import com.adfonic.domain.Campaign_;
import com.adfonic.domain.Creative;
import com.adfonic.domain.Creative_;
import com.adfonic.tracker.ClickService;
import com.adfonic.tracker.ConversionService;
import com.adfonic.tracker.kafka.TrackerKafka;
import com.byyd.middleware.creative.service.CreativeManager;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.jpa.FetchStrategyBuilder;
import com.byyd.middleware.publication.service.PublicationManager;

@Controller
public class ConversionController extends AbstractTrackerController {

    public static final String PIXEL_RESOURCE = "1x1transparent.gif";

    private static final transient Logger LOG = LoggerFactory.getLogger(ConversionController.class.getName());

    // Package to expose these to unit tests
    static final FetchStrategy CREATIVE_FETCH_STRATEGY = new FetchStrategyBuilder().addInner(Creative_.campaign).addInner(Campaign_.advertiser).build();

    static final FetchStrategy CREATIVE_FETCH_STRATEGY_SECURE = new FetchStrategyBuilder().addInner(Creative_.campaign).addInner(Campaign_.advertiser).build();

    static final FetchStrategy AD_SPACE_FETCH_STRATEGY = new FetchStrategyBuilder().addInner(AdSpace_.publication).build();

    public static final Map<String, Object> INTERNAL_ERROR_RESPONSE = buildErrorResponse(INTERNAL_ERROR);
    public static final Map<String, Object> OK_RESPONSE = buildResponse(1, null);

    final byte[] pixelBytes;

    @Autowired
    private PublicationManager publicationManager;
    @Autowired
    private CreativeManager creativeManager;
    @Autowired
    private ClickService clickService;
    @Autowired
    private ConversionService conversionService;
    @Autowired
    private AdEventFactory adEventFactory;
    @Autowired
    private TrackerKafka trackerKafka;
    @Autowired
    private V1DomainModelMapper mapper;

    public ConversionController() {
        InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream(PIXEL_RESOURCE);
        if (stream == null) {
            throw new IllegalStateException("Classpath resource not found: " + PIXEL_RESOURCE);
        }
        try {
            pixelBytes = IOUtils.toByteArray(stream);
        } catch (IOException iox) {
            throw new IllegalStateException("Failed to load existing classpath resource: " + PIXEL_RESOURCE);
        }
    }

    /**
     * Option 1 – Client-Side Conversion Tracking
     * https://developer.byyd-tech.com/index.php/Mobile_Site_Conversions
     */
    @RequestMapping("/scb/{advertiserExternalID}/conversion.gif")
    public void deDupConversionFromUserSecure(HttpServletRequest request, HttpServletResponse response, @PathVariable String advertiserExternalID,
            @CookieValue(value = AdserverConstants.CLICK_ID_COOKIE, required = false) String clickExternalID) throws IOException {

        writePixelBytes(response);

        if (StringUtils.isNotBlank(clickExternalID)) {
            deDupConversionSecure(advertiserExternalID, clickExternalID);
        } else {
            LOG.warn("Cookie " + AdserverConstants.CLICK_ID_COOKIE + " not passed, advertiserExternalID={}, Referer: {}", advertiserExternalID, request.getHeader("Referer"));
        }
    }

    /**
     * Option 2 – Server-Side Conversion Tracking
     * https://developer.byyd-tech.com/index.php/Mobile_Site_Conversions
     */
    @RequestMapping("/scs/{advertiserExternalID}/{clickExternalID}")
    @ResponseBody
    public Map<String, Object> deDupConversionSecure(@PathVariable String advertiserExternalID, @PathVariable String clickExternalID) {
        LOG.debug("Handling secure conversion tracking request for advertiserExternalID={}, clickExternalID={}", advertiserExternalID, clickExternalID);

        Click click = clickService.getClickByExternalID(clickExternalID);
        if (click == null) {
            LOG.info("Click not found for clickExternalID={}, scheduling retry", clickExternalID);

            // SC-2 - schedule a retry for some point in the future, since the
            // click may just not have been tracked yet (i.e. when there's an AdEvent backlog).
            conversionService.scheduleConversionRetry(clickExternalID);

            return buildErrorResponse("Unknown unique identifier");
        }

        Creative creative = creativeManager.getCreativeById(click.getCreativeId(), CREATIVE_FETCH_STRATEGY_SECURE);
        if (creative == null) {
            LOG.error("Failed to load Creative, clickExternalID={}, creativeId={}", clickExternalID, click.getCreativeId());
            return INTERNAL_ERROR_RESPONSE;
        }

        if (advertiserExternalID == null || !creative.getCampaign().getAdvertiser().getExternalID().equals(advertiserExternalID)) {
            LOG.error("Failed to load Creative, clickExternalID={}, creativeId={}, advertiserExternalID={}", clickExternalID, click.getCreativeId(), advertiserExternalID);
            return INTERNAL_ERROR_RESPONSE;
        }

        return doConversion(click, creative, clickExternalID);
    }

    /**
     * @deprecated
     */
    @RequestMapping("/cb/conversion.gif")
    @Deprecated
    public void deDupConversionFromUser(HttpServletRequest request, HttpServletResponse response,
            @CookieValue(value = AdserverConstants.CLICK_ID_COOKIE, required = false) String clickExternalID) throws java.io.IOException {
        deDupConversionFromUser(request, response, null, clickExternalID);
    }

    /**
     * Probably sould be @deprecated but according tracker access log, this is most used conversion method 
     * regardless documentation https://developer.byyd-tech.com/index.php/Mobile_Site_Conversions
     */
    @RequestMapping("/cb/{advertiserExternalID}/conversion.gif")
    public void deDupConversionFromUser(HttpServletRequest request, HttpServletResponse response, @PathVariable String advertiserExternalID,
            @CookieValue(value = AdserverConstants.CLICK_ID_COOKIE, required = false) String clickExternalID) throws java.io.IOException {

        writePixelBytes(response);

        if (StringUtils.isNotBlank(clickExternalID)) {
            deDupConversion(advertiserExternalID, clickExternalID);
        } else {
            LOG.warn("Cookie " + AdserverConstants.CLICK_ID_COOKIE + " not passed, advertiserExternalID={}, Referer: {}", advertiserExternalID, request.getHeader("Referer"));
        }
    }

    /**
     * @deprecated
     */
    @RequestMapping("/cs/{clickExternalID}")
    @ResponseBody
    @Deprecated
    public Map<String, Object> deDupConversion(@PathVariable String clickExternalID) {
        return deDupConversion(null, clickExternalID);
    }

    @RequestMapping("/cs/{advertiserExternalID}/{clickExternalID}")
    @ResponseBody
    public Map<String, Object> deDupConversion(@PathVariable String advertiserExternalID, @PathVariable String clickExternalID) {
        LOG.debug("Handling conversion tracking request for advertiserExternalID={}, clickExternalID={}", advertiserExternalID, clickExternalID);

        // clickExternalID (same value as Impression external Id) is inserted into TrackerDB's click table by DataCollector from AdAction.CLICK AdEvent
        Click click = clickService.getClickByExternalID(clickExternalID);
        if (click == null) {
            LOG.info("Click not found for clickExternalID={}, scheduling retry", clickExternalID);
            // SC-2 - schedule a retry for some point in the future, since the
            // click may just not have been tracked yet (i.e. when there's an AdEvent backlog).
            conversionService.scheduleConversionRetry(clickExternalID);
            return buildErrorResponse("Unknown unique identifier");
        }

        Creative creative = creativeManager.getCreativeById(click.getCreativeId(), CREATIVE_FETCH_STRATEGY);
        if (creative == null) {
            LOG.error("Failed to load Creative, clickExternalID={}, creativeId={}", clickExternalID, click.getCreativeId());
            return INTERNAL_ERROR_RESPONSE;
        }

        // AD-269 - we are providing a way to protect the adveritser from dirty conversions from a impression in a cookie.
        // Thus if the advertiser is conversionProtected no conversions will be recorded. 
        // conversionProtection is false by default and has to be set as true in the database on a particular advertiser.
        // In the future we might set it to true as default for any new advertisers that are created /maybe/
        if (creative.getCampaign().getAdvertiser().isConversionProtected()) {
            LOG.warn("Can't convert clickExternalID={}, creativeId={}, for advertiserId={}", clickExternalID, click.getCreativeId(), creative.getCampaign().getAdvertiser().getId());
            return INTERNAL_ERROR_RESPONSE;
        }

        return doConversion(click, creative, clickExternalID);
    }

    private Map<String, Object> doConversion(Click click, Creative creative, String clickExternalID) {
        AdSpace adSpace = publicationManager.getAdSpaceById(click.getAdSpaceId(), AD_SPACE_FETCH_STRATEGY);
        if (adSpace == null) {
            LOG.error("Failed to load AdSpace, clickExternalID={}, adSpaceId={}", clickExternalID, click.getAdSpaceId());
            return INTERNAL_ERROR_RESPONSE;
        }

        // AF-1484 - don't track it unless the campaign is conversion tracking enabled
        if (!creative.getCampaign().isConversionTrackingEnabled()) {
            LOG.debug("Conversion tracking not enabled clickExternalID={}, creativeId={}, ", clickExternalID, creative.getId());
            return buildErrorResponse("Conversion tracking not enabled on campaign");
        }

        // Make sure we haven't already logged the conversion
        if (!conversionService.trackConversion(click)) {
            LOG.debug("Duplicate conversion tracking request, click externalID={}", click.getExternalID());
            return buildErrorResponse("Duplicate conversion");
        }

        // SC-134 - device identifiers now get logged with the AdEvent
        clickService.loadDeviceIdentifiers(click);

        // Log the event via data collector using the values from the
        // initial Click, zero cost, and AdAction.CONVERSION.
        AdEvent event = adEventFactory.newInstance(AdAction.CONVERSION);
        event.populate(click, creative.getCampaign().getId(), adSpace.getPublication().getId());

        //Log the event to kafka
        try {
            net.byyd.archive.model.v1.AdEvent ae = mapper.map(event);
            LOG.info("Logging to kafka CONVERSION AdEvent for Creative id={}, AdSpace id={}", ae.getCreativeId(), ae.getAdSpaceId());
            trackerKafka.logAdEvent(ae);
        } catch (Exception e) {
            LOG.error("Error logging to kafka " + e.getMessage());
        }

        return OK_RESPONSE;
    }

    public static Map<String, Object> buildErrorResponse(String errorParam) {
        return buildResponse(0, errorParam);
    }

    private static Map<String, Object> buildResponse(int success, String errorParam) {
        Map<String, Object> response = new LinkedHashMap<String, Object>();
        response.put(RESPONSE_PARAM_SUCCESS, success);
        if (errorParam != null) {
            response.put(RESPONSE_PARAM_ERROR, errorParam);
        }
        return response;
    }

    private void writePixelBytes(HttpServletResponse response) throws IOException {
        response.setContentType("image/gif");
        OutputStream outputStream = response.getOutputStream();
        outputStream.write(pixelBytes);
        outputStream.flush();
    }
}
