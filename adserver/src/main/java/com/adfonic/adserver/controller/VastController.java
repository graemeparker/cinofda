package com.adfonic.adserver.controller;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.adfonic.adresponse.VastTagProcessor;
import com.adfonic.adserver.Constant;
import com.adfonic.adserver.Impression;
import com.adfonic.adserver.ImpressionService;
import com.adfonic.adserver.TargetingContext;
import com.adfonic.adserver.TargetingContextFactory;
import com.adfonic.adserver.controller.dbg.DbgUiUtil;
import com.adfonic.adserver.controller.dbg.RtbExchange;
import com.adfonic.adserver.controller.rtb.OpenRtbV1Controller;
import com.adfonic.adserver.rtb.util.AdServerStats;
import com.adfonic.adserver.rtb.util.AsCounter;
import com.adfonic.adserver.vhost.VhostManager;
import com.adfonic.domain.ContentForm;
import com.adfonic.domain.cache.dto.adserver.adspace.AdSpaceDto;
import com.adfonic.domain.cache.dto.adserver.adspace.PublicationDto;
import com.adfonic.domain.cache.dto.adserver.adspace.PublisherDto;
import com.adfonic.domain.cache.dto.adserver.creative.CreativeDto;
import com.adfonic.domain.cache.ext.AdserverDomainCache;

@Controller
public class VastController {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private AdServerStats astats;

    @Autowired
    private TargetingContextFactory targetingContextFactory;

    @Autowired
    private ImpressionService impressionService;

    @Autowired
    private VhostManager vhostManager;

    /**
     * Endpoint hosting VAST InLine tag that is required when we bid VAST Wrapper whose url will point exactly here
     */
    @RequestMapping(Constant.VAST_URI_PATH + "/{adSpaceExternalID}/{impressionExternalID}")
    public void vast(HttpServletRequest httpRequest, HttpServletResponse httpResponse, //
            @PathVariable("adSpaceExternalID") String adSpaceExternalID,//
            @PathVariable("impressionExternalID") String impressionExternalID) throws Exception {

        TargetingContext tcontext = targetingContextFactory.createTargetingContext(httpRequest, true);

        AdserverDomainCache adCache = tcontext.getAdserverDomainCache();
        // Be aware that AdSpace might be null - ExternalID may be broken or AdSpace is no longer in cache
        AdSpaceDto adSpace = adCache.getAdSpaceByExternalID(adSpaceExternalID);

        Impression impression = impressionService.getImpression(impressionExternalID);
        if (impression == null) {
            logger.warn("Impression not found: " + impressionExternalID + ", AdSpace: " + adSpaceExternalID);
            if (adSpace != null) {
                astats.increment(adSpace, AsCounter.VastImpressionNotFound);
            } else {
                astats.increment(RtbExchange.Unknown, AsCounter.VastImpressionNotFound);
            }
            return; // Cannot continue
        }

        // Make sure the creative exists
        CreativeDto creative = adCache.getCreativeById(impression.getCreativeId());
        if (creative == null) {
            creative = adCache.getRecentlyStoppedCreativeById(impression.getCreativeId());
            if (creative == null) {
                logger.warn("Creative not found: " + impression.getCreativeId() + ", AdSpace: " + adSpaceExternalID + ", Impression:" + impressionExternalID);
                astats.increment(adSpace, AsCounter.VastCreativeNotFound);
                return;
            }
        }
        // Build click redirect url
        StringBuilder clickUrlBldr = vhostManager.getClickRedirectBaseUrl(httpRequest);
        clickUrlBldr.append('/').append(adSpace.getExternalID()).append('/').append(impression.getExternalID());
        String clickRedirectUrl = clickUrlBldr.toString();

        // Build our impression tracker url
        String byydImpressionTracker = vhostManager.getBeaconBaseUrl(httpRequest, impression.getSslRequired()).append('/').append(adSpace.getExternalID()).append('/')
                .append(impression.getExternalID()).append(".gif").toString();

        List<String> impressionTrackers;
        List<String> thirdPartyTrackers = creative.getDestination().getBeaconUrls();
        if (thirdPartyTrackers != null && !thirdPartyTrackers.isEmpty()) {
            impressionTrackers = new ArrayList<String>(thirdPartyTrackers.size() + 1);
            impressionTrackers.add(byydImpressionTracker);
            for (String thirdPartyTracker : thirdPartyTrackers) {
                impressionTrackers.add(thirdPartyTracker);
            }
        } else {
            impressionTrackers = Arrays.asList(byydImpressionTracker);
        }

        String vastXml = VastTagProcessor.buildVastInLine(creative, adSpace, tcontext, impression, clickRedirectUrl, impressionTrackers);

        httpResponse.setHeader("Expires", "0");
        httpResponse.setHeader("Pragma", "No-Cache");
        httpResponse.setCharacterEncoding("utf-8");
        httpResponse.setContentType(Constant.APPL_XML + "; charset=utf-8");
        OpenRtbV1Controller.addCorsHeaders(httpRequest, httpResponse);

        PrintWriter httpWriter = httpResponse.getWriter();
        httpWriter.write(vastXml);
    }

    /**
     * Static endpoint to get VAST creative for testing or debugging purposes. No need to handle errors... 
     * Because we have no Impression object, VAST xml is returned as-is. Macros are not resolved and impression and click tracking cannot be installed.  
     */
    @RequestMapping(Constant.VAST_URI_PATH + "/{creativeExternalID}")
    public void vastTest(HttpServletRequest httpRequest, HttpServletResponse httpResponse, //
            @PathVariable("creativeExternalID") String creativeExternalID,//
            @RequestParam(name = "macros", defaultValue = "false") Boolean macros, //
            @RequestParam(name = "trackers", defaultValue = "false") Boolean trackers,//
            @RequestParam(name = "secure", defaultValue = "false") Boolean secure) throws Exception {

        TargetingContext tcontext = targetingContextFactory.createTargetingContext(httpRequest, true);
        AdserverDomainCache adCache = tcontext.getAdserverDomainCache();

        Long id = DbgUiUtil.tryToLong(creativeExternalID);
        CreativeDto creative;
        if (id != null) {
            creative = adCache.getCreativeById(id);
        } else {
            creative = adCache.getCreativeByExternalID(creativeExternalID);
        }

        if (creative == null) {
            throw new IllegalArgumentException("Creative not found: " + creativeExternalID);
        }

        String vastXml = creative.getExtendedCreativeTemplates().get(ContentForm.VAST_2_0);
        if (vastXml == null) {
            throw new IllegalArgumentException("Creative: " + creative.getId() + " has no " + ContentForm.VAST_2_0 + " extended template");
        }
        if (macros || trackers) {

            Impression impression = new Impression();
            impression.setExternalID(Constant.XAUDIT_IMPRESSION_EXTERNAL_ID);
            impression.setDeviceIdentifiers(Collections.emptyMap());

            PublisherDto publisher = new PublisherDto();
            publisher.setExternalId(Constant.XAUDIT_PUBLICATION_EXTERNAL_ID);

            PublicationDto publication = new PublicationDto();
            publication.setExternalID(Constant.XAUDIT_PUBLICATION_EXTERNAL_ID);
            publication.setPublisher(publisher);

            AdSpaceDto adSpace = new AdSpaceDto();
            adSpace.setExternalID(Constant.XAUDIT_ADSPACE_EXTERNAL_ID);
            adSpace.setPublication(publication);

            tcontext.setAdSpace(adSpace);
            tcontext.setAttribute(TargetingContext.DEVICE_PROPERTIES, Collections.emptyMap());

            String clickRedirectUrl = null;
            List<String> imppressionTrackers = null;
            if (trackers) {
                StringBuilder byydClickUrlBldr = vhostManager.getClickRedirectBaseUrl(httpRequest);
                byydClickUrlBldr.append('/').append(adSpace.getExternalID()).append('/').append(impression.getExternalID());
                clickRedirectUrl = byydClickUrlBldr.toString();

                StringBuilder byydBeaconBldr = vhostManager.getBeaconBaseUrl(httpRequest, secure);
                byydBeaconBldr.append('/').append(adSpace.getExternalID()).append('/').append(impression.getExternalID()).append(".gif");
                imppressionTrackers = Arrays.asList(byydBeaconBldr.toString());
            }

            vastXml = VastTagProcessor.buildVastInLine(creative, adSpace, tcontext, impression, clickRedirectUrl, imppressionTrackers);
        }

        httpResponse.setHeader("Expires", "0");
        httpResponse.setHeader("Pragma", "No-Cache");
        httpResponse.setCharacterEncoding("utf-8");
        httpResponse.setContentType(Constant.APPL_XML + "; charset=utf-8");
        OpenRtbV1Controller.addCorsHeaders(httpRequest, httpResponse);

        PrintWriter httpWriter = httpResponse.getWriter();
        httpWriter.write(vastXml);
    }

}
