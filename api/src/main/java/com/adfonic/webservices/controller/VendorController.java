package com.adfonic.webservices.controller;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import javax.jms.Topic;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.time.DateUtils;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.adfonic.domain.Campaign;
import com.adfonic.domain.Campaign.Status;
import com.adfonic.domain.CampaignTrigger;
import com.adfonic.jms.JmsUtils;
import com.adfonic.jms.StopCampaignMessage;
import com.adfonic.jms.UnStopCampaignMessage;
import com.adfonic.reporting.Metric;
import com.adfonic.webservices.ErrorCode;
import com.adfonic.webservices.WebServiceException;
import com.adfonic.webservices.dto.CampaignStatusDTO;
import com.adfonic.webservices.service.ICampaignService;
import com.adfonic.webservices.util.CampaignCommand;
import com.adfonic.webservices.util.CampaignStatusChangeReason;
import com.adfonic.webservices.util.Reporting;
import com.byyd.middleware.account.service.CompanyManager;
import com.byyd.middleware.integrations.filter.CampaignTriggerFilter;

@Controller
public class VendorController extends AbstractAdfonicWebService {

    private static final transient Logger LOG = Logger.getLogger(VendorController.class.getName());
    private static final String NO_ACTIVE_TRIGGERS_ERROR_MESSAGE = "No active triggers specified for this campaign";
    private static final String NO_TRIGGERS_ERROR_MESSAGE = "No triggers specified for this campaign";
    
    private JmsUtils jmsUtils = new JmsUtils();    
    
    @Autowired
    Reporting reporting;

    @Autowired
    private CompanyManager companyManager;
    
    @Autowired
    @Qualifier("centralJmsTemplate")
    private JmsTemplate centralJmsTemplate;
    
    @Autowired
    @Qualifier("stopCampaignTopic")
    private Topic stopCampaignTopic;
    
    @Autowired
    @Qualifier("unStopCampaignTopic")
    private Topic unStopCampaignTopic;
    
    public static final List<Status> allowedStatuses = Collections.unmodifiableList(Arrays.asList(Status.ACTIVE, Status.PAUSED));

    public static final List<Metric> metrics = Collections.unmodifiableList(Arrays.asList(Metric.IMPRESSIONS));

    @Autowired
    private ICampaignService campaignService;
    
    @RequestMapping(value = "/vendor/{vendorName}/campaign/{externalID}/statistics.{format}", method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    @Transactional(readOnly=false)
    public String showCampaignStatistics(@PathVariable("vendorName")
                                     String vendorName,
                                     @PathVariable
                                     String format,
                                     Model model,
                                     @RequestParam
                                     final String start,
                                     @RequestParam
                                     final String end,
                                     @PathVariable("externalID")
                                     String externalID,
                                     HttpServletRequest request) throws Exception {

        authenticatePluginVendor(request, format);
        
        Campaign campaign = campaignService.findbyExternalID(externalID);        
        checkCampaignTriggers(campaign, RequestMethod.GET, format);
        Object result = reporting.getAdvertiserStats(campaign.getAdvertiser().getId(), campaign.getId(), start, end);
        
        model.addAttribute("metrics", metrics);
        model.addAttribute("result", result);
        model.addAttribute("unique", true);
        return format + "CampaignStatisticsView";

    }
    
    @RequestMapping(value = "/vendor/{vendorName}/campaign/{id}.{format}", method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    @Transactional(readOnly=false)
    public String showCampaignStatus(@PathVariable("vendorName")
                                     String vendorName,
                                     @PathVariable
                                     String format,
                                     Model model,
                                     @PathVariable("id")
                                     String externalID,
                                     HttpServletRequest request) throws WebServiceException {

        // TODO: Authenticate also with vendorName apart from email
        authenticatePluginVendor(request, format);
        Campaign campaign = campaignService.findbyExternalID(externalID);
        checkCampaignTriggers(campaign, RequestMethod.GET, format);
        CampaignStatusDTO campaignStatusDTO = new CampaignStatusDTO();
        campaignStatusDTO.setStatus(campaign.getStatus());
        campaignStatusDTO.setName(campaign.getName());
        return itemView(campaignStatusDTO, format, model);
    }

    @JsonSerialize(include=JsonSerialize.Inclusion.NON_NULL)
    @RequestMapping(value = "/vendor/{vendorName}/campaign/{id}.{format}", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Transactional(readOnly=false)
    public void changeCampaignStatus(@PathVariable("vendorName")
                                     String vendorName,
                                     @PathVariable
                                     String format,
                                     @PathVariable("id")
                                     String externalID,
                                     @RequestParam(value = "command", required = true)
                                     CampaignCommand campaignCommand, HttpServletRequest request) throws WebServiceException {

        // TODO: Authenticate also with vendorName apart from email
        authenticatePluginVendor(request, format);
        Campaign campaign = campaignService.findbyExternalID(externalID);
        checkCampaignTriggers(campaign, RequestMethod.POST, format);
        Campaign.Status currentStatus = campaign.getStatus();

        if (campaignCommand.status == currentStatus) {
            throw new WebServiceException(ErrorCode.FORBIDDEN_GENERAL, "Operation has no effect", HttpServletResponse.SC_FORBIDDEN, format);
        }

        if(!allowedStatuses.contains(currentStatus)) {
           throw new WebServiceException(ErrorCode.FORBIDDEN_WRITE, "Campaign not approved yet: " + currentStatus, HttpServletResponse.SC_FORBIDDEN, format);
        }
        
        if(campaign.transitionStatus(campaignCommand.status, allowedStatuses)) {
            LOG.fine("Changing campaign status from "+currentStatus+" to "+campaignCommand.status);
            if(campaignCommand.status.equals(Campaign.Status.PAUSED)) {
                LOG.fine("Pausing campaign "+campaign.getId()+" by trigger");
                Date now = new Date();
                jmsUtils.sendObject(centralJmsTemplate, stopCampaignTopic, new StopCampaignMessage(campaign.getId(), CampaignStatusChangeReason.TRIGGER.name(), now, DateUtils.addYears(now, 1)));
            } else if(campaignCommand.status.equals(Campaign.Status.ACTIVE)) {
                LOG.fine("Activating campaign "+campaign.getId()+" by trigger");
                jmsUtils.sendObject(centralJmsTemplate, unStopCampaignTopic, new UnStopCampaignMessage(campaign.getId()));
            }
            getCampaignManager().update(campaign);
        } else {
            throw new WebServiceException(ErrorCode.FORBIDDEN_WRITE, "Campaign not approved yet: " + currentStatus, HttpServletResponse.SC_FORBIDDEN, format);
        }
    }
    
    private void checkCampaignTriggers(Campaign campaign, RequestMethod requestMethod, String format) throws WebServiceException {

        List<CampaignTrigger> allCampaignTriggers = getIntegrationsManager().getCampaignTriggers(new CampaignTriggerFilter().setCampaign(campaign));

        if(allCampaignTriggers.size()==0) {
            throw new WebServiceException(ErrorCode.ACCESS_DENIED, NO_TRIGGERS_ERROR_MESSAGE, HttpServletResponse.SC_FORBIDDEN, format);
        }

        if(RequestMethod.POST.equals(requestMethod) && campaign.getCampaignTriggers().size()==0) {
            throw new WebServiceException(ErrorCode.ACCESS_DENIED, NO_ACTIVE_TRIGGERS_ERROR_MESSAGE, HttpServletResponse.SC_FORBIDDEN, format);
        }

    }

}
