package com.adfonic.webservices.controller;

import java.io.OutputStream;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.adfonic.domain.PublisherAuditedCreative;
import com.adfonic.domain.PublisherAuditedCreative.Status;
import com.adfonic.webservices.service.IBeaconService;
import com.adfonic.webservices.service.IPublisherAuditService;

@Controller
public class PublisherAuditController extends AbstractAdfonicWebService /*WebApplicationObjectSupport*/ {
    private static final transient Logger LOG = Logger.getLogger(PublisherAuditController.class.getName());

    @Autowired
    private IPublisherAuditService auditService;
    
    @Autowired
    private IBeaconService beaconService;

    //propsify - current one should hit the adfonic error page
    private final String redirectErrorPage="http://byyd-tech.com/about/failedredirect";
    
    @Transactional
    @RequestMapping(value="/bc/{adSpaceExternalID}/{impressionExternalID}.gif")
    public void handleAuditBeacon(HttpServletRequest request, 
                                  HttpServletResponse response, 
                                  @PathVariable String adSpaceExternalID, 
                                  @PathVariable String impressionExternalID, 
                                  @RequestParam(value = "pubr", required = false) 
                                  String publisherExternalId, 
                                  @RequestParam(value = "crid", required = false) 
                                  String creativeExternalId) throws java.io.IOException {
        beacon(request, response);
        
        LOG.fine("On adspace param " + adSpaceExternalID + " and impression param " + impressionExternalID);

        if (StringUtils.isEmpty(creativeExternalId) || StringUtils.isEmpty(publisherExternalId)) {
            LOG.warning("Bad Request; " + requestSummaryMessage(publisherExternalId, creativeExternalId));
            return;
        }

        PublisherAuditedCreative auditedCreative = null;
        Exception fetchErr = null;

        try {
            auditedCreative = auditService.getAuditedCreativeAndPublisher(creativeExternalId, publisherExternalId);
        } catch (Exception e) {
            fetchErr = e;
        }

        if (auditedCreative == null) {
            LOG.warning("Could not find audited creative for: " + requestSummaryMessage(publisherExternalId, creativeExternalId) 
                                        + (fetchErr == null ? "" : " - exception: " + fetchErr));
        } else {
            try {
                auditService.recordAuditorImpression(auditedCreative);
            } catch (Exception e) {
                LOG.warning(requestSummaryMessage(publisherExternalId, creativeExternalId) + " - Exception: " + e.getMessage());
            }
        }

    }


    @Transactional
    @RequestMapping("/ct/{adSpaceExternalID}/{impressionExternalID}")
    public void handleClickThroughRequest(HttpServletRequest request,
                                          HttpServletResponse response,
                                          @PathVariable String adSpaceExternalID, 
                                          @PathVariable String impressionExternalID, 
                                          @RequestParam(value = "pubr", required = false) 
                                          String publisherExternalId, 
                                          @RequestParam(value = "crid", required = false) 
                                          String creativeExternalId) throws java.io.IOException {
        LOG.fine("On adspace param " + adSpaceExternalID+" and impression param "+ impressionExternalID);
        
        if(StringUtils.isEmpty(creativeExternalId)||StringUtils.isEmpty(publisherExternalId)){
            LOG.warning("Bad Request; "+requestSummaryMessage(publisherExternalId, creativeExternalId));
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        
        
        PublisherAuditedCreative auditedCreative = null;
        Exception fetchErr = null;

        try {
            auditedCreative = auditService.getAuditedCreativeAndPublisher(creativeExternalId, publisherExternalId);
        } catch (Exception e) {
            fetchErr = e;
        }

        if (auditedCreative == null) {
            LOG.warning("Could not find audited creative for: " + requestSummaryMessage(publisherExternalId, creativeExternalId) 
                                        + (fetchErr == null ? "" : " - exception: " + fetchErr));
        } else {
            try {
                auditService.recordAuditorClick(auditedCreative);
                if (auditedCreative.getStatus() != Status.LOCAL_INVALID) {
                    response.sendRedirect(auditService.getRedirectUrl(auditedCreative));
                    return;
                }

                // TODO - redirect to a page saying it is possibly no more valid.. wait few seconds before redirecting to existing destination url
                LOG.warning(requestSummaryMessage(publisherExternalId, creativeExternalId) + "! Locally invalid!");
            } catch (Exception e) {
                LOG.warning(requestSummaryMessage(publisherExternalId, creativeExternalId) + " - Exception: " + e.getMessage());
            }
        }
        
        response.sendRedirect(redirectErrorPage);

    }
    

    private String requestSummaryMessage(String publisherExternalId, String creativeExternalId) {
        return "pubr=[" + publisherExternalId + "] crid=[" + creativeExternalId + "]"; // may be add ip as well
    }

    
    @RequestMapping("/bstat.gif")
    public void beacon(HttpServletRequest request, 
                       HttpServletResponse response) throws java.io.IOException {
        response.setHeader("Expires", "0");
        response.setHeader("Pragma", "No-Cache");

        response.setContentType("image/gif");
        OutputStream outputStream = response.getOutputStream();
        outputStream.write(beaconService.beaconContent());
        outputStream.flush();
    }

}
