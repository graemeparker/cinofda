package com.adfonic.webservices.controller;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.time.DateUtils;
import org.dozer.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.adfonic.domain.Advertiser;
import com.adfonic.domain.Advertiser_;
import com.adfonic.domain.Campaign;
import com.adfonic.domain.CampaignAudience;
import com.adfonic.domain.CampaignTimePeriod;
import com.adfonic.domain.Campaign_;
import com.adfonic.domain.Creative;
import com.adfonic.domain.DMPAudience;
import com.adfonic.domain.DMPSelector;
import com.adfonic.domain.DestinationType;
import com.adfonic.domain.DeviceIdentifierType;
import com.adfonic.domain.Segment;
import com.adfonic.domain.User;
import com.adfonic.reporting.Dimension;
import com.adfonic.reporting.Metric;
import com.adfonic.reporting.OLAPQuery;
import com.adfonic.reporting.Parameter;
import com.adfonic.util.Range;
import com.adfonic.webservices.ErrorCode;
import com.adfonic.webservices.WebServiceException;
import com.adfonic.webservices.dto.CampaignAudienceDTO;
import com.adfonic.webservices.dto.CampaignBidDTO;
import com.adfonic.webservices.dto.CampaignDTO;
import com.adfonic.webservices.dto.CampaignTimePeriodDTO;
import com.adfonic.webservices.dto.CreativeDTO;
import com.adfonic.webservices.dto.SegmentDTO;
import com.adfonic.webservices.dto.WeveCampaignDTO;
import com.adfonic.webservices.service.ICampaignService;
import com.adfonic.webservices.service.ICopyService;
import com.adfonic.webservices.service.IRestrictingCopyService;
import com.adfonic.webservices.service.ISegmentCopyService;
import com.adfonic.webservices.service.IUtilService;
import com.adfonic.webservices.util.CampaignCommand;
import com.adfonic.webservices.util.DSPetc;
import com.adfonic.webservices.util.Reporting;
import com.byyd.middleware.account.service.CompanyManager;
import com.byyd.middleware.account.service.UserManager;
import com.byyd.middleware.campaign.service.CampaignManager;
import com.byyd.middleware.device.service.DeviceManager;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.jpa.FetchStrategyBuilder;
import com.google.common.base.Joiner;

@Controller
public class CampaignController extends AbstractAdfonicWebService {
    private static final transient Logger LOG = Logger.getLogger(CampaignController.class.getName());

    private static final FetchStrategy CAMPAIGN_FETCH_STRATEGY = new FetchStrategyBuilder()
        .addInner(Campaign_.advertiser)
        .addInner(Advertiser_.company)
        .build();

    protected Campaign getCampaignForUser(User user, String externalID, String format) throws Exception {
        Campaign campaign = getCampaignManager().getCampaignByExternalId(externalID, CAMPAIGN_FETCH_STRATEGY);
        if(campaign == null) {
            throw new WebServiceException(ErrorCode.ENTITY_NOT_FOUND, "Campaign " + externalID + " not found", format);
        } else if (!getUserManager().isUserAuthorizedToManageAdvertiser(user, campaign.getAdvertiser())) {
            throw new WebServiceException(ErrorCode.ACCESS_DENIED, "You are not authorized to access this Campaign's data", format);
        } else {
            return campaign;
        }
    }

    protected Map<?,?> getStatisticsMapImpl(OLAPQuery q) {
        q.addMetrics(Metric.IMPRESSIONS,
                     Metric.CLICKS,
                     Metric.CONVERSIONS,
                     Metric.CONVERSION_PERCENT,
                     Metric.CTR,
                     Metric.ECPM_AD,
                     Metric.COST_PER_CONVERSION,
                     Metric.COST);

        // TODO: migrate to JPA
        Map<?,?> result = q.getResultAsMap();

        return result;
    }


    @RequestMapping(value="/campaign/{externalID}/statistics.{format}", method=RequestMethod.GET)
    public String getCampaignStats(HttpServletRequest request,
                                    HttpServletResponse response,
                                    Model model,
                                    @RequestParam
                                    String start,
                                    @RequestParam
                                    String end,
                                    @PathVariable
                                    String externalID,
                                    @PathVariable
                                    String format) throws Exception {

        User user = authenticate(request, format);
        Campaign campaign = getCampaignForUser(user, externalID, format);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHH");
        TimeZone tz = user.getCompany().getDefaultTimeZone();
        sdf.setTimeZone(tz);
        Date startDate = sdf.parse(start);
        Date endDate = sdf.parse(end);
        Range<Date> dateRange = new Range<Date>(startDate, endDate);
        Parameter.AdvertiserTimeByHour dateRangeParameter = new Parameter.AdvertiserTimeByHour(user.getCompany().getDefaultTimeZone(), dateRange);
        OLAPQuery q = new OLAPQuery(Locale.getDefault());
        q.addSlicers(dateRangeParameter);

        q.addParameters(new Parameter.ByDimension(Dimension.Advertiser, campaign, Campaign.class, getCampaignResolver(CAMPAIGN_FETCH_STRATEGY)));

        model.addAttribute("result", getStatisticsMapImpl(q));
        model.addAttribute("unique", true);

        return format + "CampaignStatisticsView";
    }


    @Autowired
    Reporting reporting;
    
    enum Report{
        campaign, device, creative, location, connection
    }
    
    enum SumOver{
        total, day, hour
    }
    
    @RequestMapping(value="/campaign/{externalID}/statistics/{report}.{format}", method=RequestMethod.GET)
    public String getCampaignStatsMeta(HttpServletRequest request,
                                    HttpServletResponse response,
                                    Model model,
                                    @RequestParam
                                    final String start,
                                    @RequestParam
                                    final String end,
                                    @PathVariable
                                    String externalID,
                                    @PathVariable
                                    Report report,
                                    @PathVariable
                                    String format,
                                    @RequestParam(value="sum_over", required=false, defaultValue="total")//SumOver.total
                                    SumOver sumOver) throws Exception {// stored-proc based

        final User user = authenticate(request, format);
        Campaign campaign = getCampaignForUser(user, externalID, format);

        class Dates{// because different sub reports have different date formats
            String startC, endC;
            Dates(String format)throws Exception{
                // to see if it parses alright and mainly to handle an edge case where old report 
                // used to return valid stuff when input format was not right
                SimpleDateFormat sdf = new SimpleDateFormat(format);
                TimeZone tz = user.getCompany().getDefaultTimeZone();
                sdf.setTimeZone(tz);
                startC=sdf.format(sdf.parse(start));
                endC=sdf.format(sdf.parse(end));
            }
            
            String start(){return startC;}
            String end(){return endC;}
        }

        Object result;
        switch(report){
        case creative:
            Dates dates=new Dates("yyyyMMdd");
            switch(sumOver){
            case day:
                result=reporting.getAdvStatsGroupedByCreativesByDay(campaign.getAdvertiser().getId(), campaign.getId(), dates.start(), dates.end());
                break;
            case total:
                result=reporting.getAdvStatsGroupedByCreatives(campaign.getAdvertiser().getId(), campaign.getId(), dates.start(), dates.end());
                break;
            default:
                throw new UnsupportedOperationException("sum_over period not supported!");// TODO v
            }
            break;
        default:
            throw new UnsupportedOperationException("Report type not supported!");//TODO Change it to WS standard exception
        }

        model.addAttribute("result", result);
        model.addAttribute("unique", true);

        return format + "CampaignStatisticsView";
    }

    
    @Autowired
    private ICampaignService campaignService;

    @Autowired
    IRestrictingCopyService<CampaignDTO, Campaign> campaignCopyService;

    @Autowired
    private ICopyService<CreativeDTO, Creative> creativeCopyService;

    @Autowired
    // TODO - All due to lack of a Campaign.setSegment(); Change when modifying domain
    ISegmentCopyService segmentCopyService;


    // IRestrictingCopyService<SegmentDTO, Segment> segmentCopyService;

    @Autowired
    private CompanyManager companyManager;
    
    @Autowired
    private UserManager userManager;

    // TODO - automatic exception translation
    @RequestMapping(value = { "/campaign/create.{format}", "/campaign.{format}" }, method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    @Transactional(readOnly=false)
    public CampaignDTO createCampaign(@PathVariable
                                      String format,
                                      @RequestBody
                                      CampaignDTO campaignDTO,
                                      HttpServletRequest request)
        throws WebServiceException
    {
        User user = authenticate(request, format);
        user = userManager.getUserById(user.getId());// open in new session

        // TODO - agency advertisers? user.advertisers no load
        Advertiser advertiser = user.getCompany().getAdvertisers().iterator().next();// should have atleast one so no prob

        // Create the new segment
        Segment segment = new Segment(advertiser);
        SegmentDTO segmentDTO = campaignDTO.getSegment();
        boolean hasInvTargetingSegmentParams = false;
        if (segmentDTO != null) {
            hasInvTargetingSegmentParams = hasInventoryTargetingParams(segmentDTO);
            segmentCopyService.copyToSegment(segmentDTO, segment, Campaign.Status.NEW);
        }
        segment = getTargetingManager().newSegment(segment);

        // Create the new campaign
        Campaign campaign = campaignService.createMinimalCampaign(advertiser, Collections.singleton(segment), campaignDTO.getName(), campaignDTO.getDefaultLanguage());

        campaignCopyService.restrictOnCampaignStatus(campaign.getStatus()).copyToDomain(campaignDTO, campaign);

        campaignService.setDailyBudgets(campaign, campaignDTO.getBudgetType(), campaignDTO.getDailyBudget(), campaignDTO.getDailyBudgetWeekday(), campaignDTO.getDailyBudgetWeekend());

        campaignService.setInstallTracking(campaign, campaignDTO.getInstallTrackingEnabled(), campaignDTO.getApplicationID());

        campaignService.setInventoryTargeting(campaign, campaignDTO.getPublicationList(), hasInvTargetingSegmentParams);
        
        campaignService.validate(campaign);

        CampaignBidDTO bid = campaignDTO.getBid();
        campaignService.validateNewBid(bid, campaign);

        campaign = getCampaignManager().newCampaign(campaign, CAMPAIGN_FETCH_STRATEGY);

        if (bid != null) {
            campaignService.createNewBid(campaign, bid.getType(), bid.getAmount());
        }

        Set<CampaignTimePeriodDTO> timePeriods = campaignDTO.getTimePeriods();
        if (timePeriods != null) {
            campaignService.setTimePeriods(campaign, getTimePeriodsFromDTOs(campaign, timePeriods));
        }

        // TODO: remove this temporary hack once advertisers are forced to
        // select device identifier types themselves.
        tempSetupDeviceIdentifierTypesAsNeeded(campaign, getCampaignManager(), getDeviceManager());

        return copyFromDomain(campaign);
    }

    private static boolean hasInventoryTargetingParams(SegmentDTO segmentDTO) {
        return !(CollectionUtils.isEmpty(segmentDTO.getTargetedPublishers()) && CollectionUtils.isEmpty(segmentDTO.getIncludedCategories()));
    }

    //========================================================================
    // BEGIN TEMPORARY HACK FOR DEVICE IDENTIFIER TYPE SETUP
    //========================================================================

    // TODO: remove these temporary methods once advertisers are forced to
    // select device identifier types themselves.

    static void tempSetupDeviceIdentifierTypesAsNeeded(Campaign campaign, CampaignManager campaignManager, DeviceManager deviceManager) {
        if (!campaign.isInstallTrackingEnabled()) {
            return; // we only care about install-trackable campaigns
        }

        for (Creative creative : campaign.getCreatives()) {
            tempSetupDeviceIdentifierTypesAsNeeded(campaign, creative, campaignManager, deviceManager);
        }
    }

    static void tempSetupDeviceIdentifierTypesAsNeeded(Campaign campaign, Creative creative, CampaignManager campaignManager, DeviceManager deviceManager) {
        if (!campaign.isInstallTrackingEnabled()) {
            return; // we only care about install-trackable campaigns
        }

        // Figure out which DeviceIdentifierType(s) we need to ensure are
        // linked to the campaign
        Set<DeviceIdentifierType> deviceIdentifierTypes = getDefaultDeviceIdentifierTypes(creative.getDestination().getDestinationType(), deviceManager);
        if (deviceIdentifierTypes.isEmpty()) {
            // That's odd...if the campaign is install trackable, in theory
            // the creative's destination type should be one that has some
            // respective device identifier type(s).  Warn at least.
            LOG.warning("No DeviceIdentifierTypes for Creative id=" + creative.getId() + ", destinationType=" + creative.getDestination().getDestinationType());
            return;
        }

        // Make sure the DeviceIdentifierType(s) are linked to the campaign
        // if they haven't been linked already
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Adding " + deviceIdentifierTypes.size() + " DeviceIdentifierType(s) to Campaign id=" + campaign.getId());
        }

        campaign.getDeviceIdentifierTypes().addAll(deviceIdentifierTypes);
        campaignManager.update(campaign); // not bothering to grab the return value
    }
    
    private static Set<DeviceIdentifierType> getDefaultDeviceIdentifierTypes(DestinationType destinationType, DeviceManager deviceManager) {
        Set<DeviceIdentifierType> deviceIdentifierTypes = new HashSet<DeviceIdentifierType>();
        switch (destinationType) {
        case IPHONE_APP_STORE:
            deviceIdentifierTypes.add(deviceManager.getDeviceIdentifierTypeBySystemName("udid"));
            deviceIdentifierTypes.add(deviceManager.getDeviceIdentifierTypeBySystemName("dpid"));
            deviceIdentifierTypes.add(deviceManager.getDeviceIdentifierTypeBySystemName("openudid"));
            break;
        case ANDROID:
            deviceIdentifierTypes.add(deviceManager.getDeviceIdentifierTypeBySystemName("android"));
            deviceIdentifierTypes.add(deviceManager.getDeviceIdentifierTypeBySystemName("dpid"));
            break;
        default:
            break;
        }
        return deviceIdentifierTypes;
    }
    

    //========================================================================
    // END TEMPORARY HACK FOR DEVICE IDENTIFIER TYPE SETUP
    //========================================================================

    @Autowired
    private Mapper dozer;

    @Transactional(readOnly=true)
    @RequestMapping(value = "/weve/campaign/{id}.{format}", method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public WeveCampaignDTO getCampaignInfoWeveStyle(@PathVariable
                                       String format,
                                       @PathVariable("id")
                                       String externalID,
                                       HttpServletRequest request) throws WebServiceException {
        User user = authenticate(request, format);

        Campaign campaign = campaignService.findbyExternalID(user, externalID);
        WeveCampaignDTO weveCampaignDTO = dozer.map(copyFromDomain(campaign), WeveCampaignDTO.class);

        Set<String> bs = new HashSet<>();
        weveCampaignDTO.setAudiences(new HashSet<CampaignAudienceDTO>());
        
        for (CampaignAudience campaignAudience : campaign.getCampaignAudiences()) {
            CampaignAudienceDTO audienceDTO = new CampaignAudienceDTO();
            audienceDTO.setId(campaignAudience.getAudience().getExternalID());
            audienceDTO.setInclude(campaignAudience.isInclude());
//            audienceDTO.setRecency(campaignAudience.getNumDays());
            weveCampaignDTO.getAudiences().add(audienceDTO);
            
            DMPAudience dmpAudience = campaignAudience.getAudience().getDmpAudience();
            if (dmpAudience != null) {
                for (DMPSelector dmpSelector : dmpAudience.getDmpSelectors()) {
                    bs.add(dmpSelector.getExternalID());
                }
            }
        }
        
        if (!bs.isEmpty()) {
            weveCampaignDTO.overrideSeg(Joiner.on('~').join(bs));
        }

        return weveCampaignDTO;
    }
    
    
    @Transactional(readOnly=true)
    @RequestMapping(value = "/campaign/{id}.{format}", method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public CampaignDTO getCampaignInfo(@PathVariable
                                       String format,
                                       @PathVariable("id")
                                       String externalID,
                                       HttpServletRequest request)
        throws WebServiceException
    {
        User user = authenticate(request, format);

        Campaign campaign = campaignService.findbyExternalID(user, externalID);

        return copyFromDomain(campaign);
    }


    @RequestMapping(value = "/campaign/{id}/creatives/list.{format}", method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    public String getCreatives(@PathVariable
                                       String format,
                                       Model model,
                                       @PathVariable("id")
                                       String externalID,
                                       HttpServletRequest request)
        throws WebServiceException
    {
        User user = authenticate(request, format);

        List<Creative> creatives = campaignService.getAllCreativesForCampaign(user, externalID);
        List<CreativeDTO> creativesList = new ArrayList<>(creatives.size());
        for (Creative creative : creatives) {
            creativesList.add(creativeCopyService.copyFromDomain(creative, CreativeDTO.class));
        }

        return listView(creativesList, format, model, "creatives");
    }


    @RequestMapping(value = "/campaign/{id}.{format}", method = RequestMethod.PUT)
    // TODO differentiate between updates in different states
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    @Transactional(readOnly=false)
    public CampaignDTO updateCampaign(@PathVariable
                                      String format,
                                      @PathVariable("id")
                                      String externalID,
                                      @RequestBody
                                      CampaignDTO campaignDTO,
                                      HttpServletRequest request) throws WebServiceException {
        User user = authenticate(request, format);

        Campaign campaign = campaignService.findbyExternalID(user, externalID);
        String currentApplicationId = campaign.getApplicationID();
        String newApplicationId = campaignDTO.getApplicationID();

        campaignCopyService.restrictOnCampaignStatus(campaign.getStatus()).copyToDomain(campaignDTO, campaign);
        SegmentDTO segmentDTO = campaignDTO.getSegment();
        boolean hasInvTargetingSegmentParams = false;
        if (segmentDTO != null) {
            hasInvTargetingSegmentParams = hasInventoryTargetingParams(segmentDTO);
            Segment segment = campaign.getSegments().get(0);
            segmentCopyService.copyToSegment(segmentDTO, segment, campaign.getStatus());
            segment = getTargetingManager().update(segment);
        }

        campaignService.setDailyBudgets(campaign, campaignDTO.getBudgetType(), campaignDTO.getDailyBudget(), campaignDTO.getDailyBudgetWeekday(), campaignDTO.getDailyBudgetWeekend());

        campaignService.setInstallTracking(campaign, campaignDTO.getInstallTrackingEnabled(), campaignDTO.getApplicationID());

        CampaignBidDTO bid = campaignDTO.getBid();
        if (bid != null) {
            campaignService.createNewBid(campaign, bid.getType(), bid.getAmount());
        }

        Set<CampaignTimePeriodDTO> timePeriods = campaignDTO.getTimePeriods();
        if (timePeriods != null) {
            campaignService.setTimePeriods(campaign, getTimePeriodsFromDTOs(campaign, timePeriods));
        }

        campaignService.setInventoryTargeting(campaign, campaignDTO.getPublicationList(), hasInvTargetingSegmentParams);
        
        campaignService.validate(campaign);

        campaign = getCampaignManager().update(campaign);

        // TODO: remove this temporary hack once advertisers are forced to
        // select device identifier types themselves.
        tempSetupDeviceIdentifierTypesAsNeeded(campaign, getCampaignManager(), getDeviceManager());
        
        /*
         * AO-158
         * AdX provisioning. Creative-level changes already trigger
         * re-submission. If the campaign's application id is changing we need 
         * to trigger re-submissions 
         */
        if (campaign.isInstallTrackingAdXEnabled() && ObjectUtils.notEqual(currentApplicationId, newApplicationId)) {
        	campaignService.resubmitAdXCreatives(campaign);
        }


        return copyFromDomain(campaign);
    }
    
    @Autowired
    private IUtilService utilService;

    // segment/targeted-publishers has to be mapped back - so dozer converter needs access to user_role   
    private CampaignDTO copyFromDomain(Campaign campaign){
        DSPetc.setDspAccessOnThread(utilService.getEffectiveDspAccess(campaign.getAdvertiser().getCompany()));
        return campaignCopyService.copyFromDomain(campaign, CampaignDTO.class);
    }


    @RequestMapping(value = /* { */"/campaign/{id}.{format}"/* , "/campaign/{id}"} */, method = RequestMethod.DELETE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCampaign(@PathVariable String format, @PathVariable("id") String externalID, HttpServletRequest request) throws WebServiceException {
        /*
         * if(format==null){ format="json"; }
         */
        User user = authenticate(request, format);

        //pm.currentTransaction().begin();

        Campaign campaign = campaignService.findbyExternalID(user, externalID);
        campaignService.delete(campaign);

        //pm.currentTransaction().commit();
    }

    @RequestMapping(value = "/campaign/{id}.{format}", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Transactional(readOnly=false)
    public void changeCampaignStatus(@PathVariable
                                     String format,
                                     @PathVariable("id")
                                     String externalID,
                                     @RequestParam(value = "command", required = true)
    								 CampaignCommand campaignCommand, HttpServletRequest request) throws WebServiceException {
        User user = authenticate(request, format);
        Campaign campaign = campaignService.findbyExternalID(user, externalID);
        
        Campaign.Status currentStatus = campaign.getStatus();
        
        if (campaignCommand.status == currentStatus) {
            throw new WebServiceException(ErrorCode.FORBIDDEN_GENERAL, "Operation has no effect", HttpServletResponse.SC_FORBIDDEN, format);
        }

        if (campaignCommand == CampaignCommand.submit) {
            campaignService.submit(campaign);
        } else if (!campaign.transitionStatus(campaignCommand.status)) {
            throw new WebServiceException(ErrorCode.FORBIDDEN_WRITE, "Not allowed from current state: " + currentStatus, HttpServletResponse.SC_FORBIDDEN, format);
        }

        getCampaignManager().update(campaign);
        
    }
    
    private Set<CampaignTimePeriod> getTimePeriodsFromDTOs(Campaign campaign, Set<CampaignTimePeriodDTO> timePeriodDTOs) {
        Set<CampaignTimePeriod> timePeriods = new HashSet<CampaignTimePeriod>();
        for (CampaignTimePeriodDTO timePeriodDTO : timePeriodDTOs) {
            timePeriods.add(
                    new CampaignTimePeriod(campaign,
                            adjustDate(timePeriodDTO.getStartDate()),
                            adjustDate(timePeriodDTO.getEndDate())));
        }
        return timePeriods;
    }


    private Date adjustDate(Long epochTime) {// timezone ?
        if (epochTime == null) {
            return (null);
        }

        Date date = new Date(epochTime * 1000);// given epoch time is input in seconds
        Date truncDate = DateUtils.truncate(date, Calendar.HOUR);// too many objects now. to avoid the 23:59 range
        Date roundDate = DateUtils.round(date, Calendar.HOUR);

        return truncDate.compareTo(roundDate) < 0 ? DateUtils.addMinutes(truncDate, 30) : truncDate;
    }

}
