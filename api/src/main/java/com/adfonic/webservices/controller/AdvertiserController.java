package com.adfonic.webservices.controller;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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

import com.adfonic.domain.AccountType;
import com.adfonic.domain.Advertiser;
import com.adfonic.domain.Advertiser_;
import com.adfonic.domain.Campaign;
import com.adfonic.domain.Campaign_;
import com.adfonic.domain.Company;
import com.adfonic.domain.Publication;
import com.adfonic.domain.PublicationList;
import com.adfonic.domain.PublicationList.PublicationListLevel;
import com.adfonic.domain.Role;
import com.adfonic.domain.User;
import com.adfonic.reporting.Dimension;
import com.adfonic.reporting.Metric;
import com.adfonic.reporting.OLAPQuery;
import com.adfonic.reporting.Parameter;
import com.adfonic.util.Range;
import com.adfonic.webservices.ErrorCode;
import com.adfonic.webservices.WebServiceException;
import com.adfonic.webservices.dto.PublicationListDTO;
import com.adfonic.webservices.exception.ServiceException;
import com.adfonic.webservices.service.IPublicationListService;
import com.byyd.middleware.campaign.filter.CampaignFilter;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.SortOrder;
import com.byyd.middleware.iface.dao.Sorting;
import com.byyd.middleware.iface.dao.jpa.FetchStrategyBuilder;

@Controller
public class AdvertiserController extends AbstractAdfonicWebService {

    private static final FetchStrategy ADVERTISER_FETCH_STRATEGY = new FetchStrategyBuilder()
        .addInner(Advertiser_.company)
        .addInner(Advertiser_.account)
        .build();

    private static final FetchStrategy CAMPAIGN_FETCH_STRATEGY = new FetchStrategyBuilder()
        .addInner(Campaign_.advertiser)
        .build();

    @RequestMapping(value="/advertisers/list.{format}", method=RequestMethod.GET)
    public String getAdvertisersForCompany(HttpServletRequest request,
                                            HttpServletResponse response,
                                            Model model,
                                            @PathVariable
                                            String format) throws Exception {
        User user = authenticate(request, format);
        Company company = user.getCompany();

        List<Advertiser> advertisers = getAdvertiserManager().getAllAdvertisersForCompany(company, new Sorting(SortOrder.asc("name")), ADVERTISER_FETCH_STRATEGY);
        List<Advertiser> list = new ArrayList<Advertiser>();
        if(company.isAccountType(AccountType.AGENCY)) {
             boolean adminCheck = false;
             for (Role r: user.getRoles()) {
                if ("Administrator".equals(r.getName())) {
                    adminCheck = true;
                    break;
                }
             }
             for(Advertiser advertiser : advertisers) {
                 if(adminCheck || user.getAdvertisers().contains(advertiser)) {
                     list.add(advertiser);
                 }
             }
        } else {
            list.addAll(advertisers);
        }
        /*
         * We can just return an empty list, no need to error out
        if(list.size() == 0) {
            throw new WebServiceException(ErrorCode.ENTITY_NOT_FOUND, "No Advertisers visible for this login", format);
        }
        */
        model.addAttribute(ADVERTISERS, list);

        return format + "AdvertiserListView";
    }

    @RequestMapping(value="/advertiser/{externalID}.{format}", method=RequestMethod.GET)
    public String getAdvertiser(HttpServletRequest request,
                                HttpServletResponse response,
                                Model model,
                                @PathVariable
                                String externalID,
                                @PathVariable
                                String format) throws Exception {

        User user = authenticate(request, format);
        Advertiser advertiser = getAdvertiserForUser(user, externalID, format, ADVERTISER_FETCH_STRATEGY);
        model.addAttribute(ADVERTISER, advertiser);
        return format + "AdvertiserView";

    }

    protected Advertiser getAdvertiserForUser(User user, String externalID, String format, FetchStrategy fetchStrategy) throws Exception {
        Advertiser advertiser = getAdvertiserManager().getAdvertiserByExternalId(externalID, fetchStrategy);
        if(advertiser == null) {
            throw new WebServiceException(ErrorCode.ENTITY_NOT_FOUND, "Advertiser " + externalID + " not found", format);
        } else if (!getUserManager().isUserAuthorizedToManageAdvertiser(user, advertiser)) {
            throw new WebServiceException(ErrorCode.ACCESS_DENIED, "You are not authorized to access this Advertiser's data", format);
        } else {
            return advertiser;
        }
    }

    // TODO: move this into some public business logic utility in core.
    // This same logic is now duplicated across at least a couple of modules.
    //private boolean isUserAuthorizedToManageAdvertiser(User user, Advertiser advertiser) {
    //	return getCompanyManager().isUserAuthorizedToManageAdvertiser(user, advertiser);
     //}

    @RequestMapping(value="/advertiser/{externalID}/campaigns/list.{format}", method=RequestMethod.GET)
    public String getCampaignsForAdvertiser(HttpServletRequest request,
                                            HttpServletResponse response,
                                            Model model,
                                            @PathVariable
                                            String externalID,
                                            @PathVariable
                                            String format) throws Exception {
        User user = authenticate(request, format);

        Advertiser advertiser = getAdvertiserForUser(user, externalID, format, ADVERTISER_FETCH_STRATEGY);

        List<Campaign> campaigns = getCampaignManager().getAllCampaigns(new CampaignFilter().setAdvertiser(advertiser), new Sorting(SortOrder.asc("name")), CAMPAIGN_FETCH_STRATEGY);
        model.addAttribute(CAMPAIGNS, campaigns);

        return format + "CampaignListView";
    }

    protected Map getStatisticsMapImpl(OLAPQuery q) {
        q.addMetrics(Metric.IMPRESSIONS,
                     Metric.CLICKS,
                     Metric.CONVERSIONS,
                     Metric.CONVERSION_PERCENT,
                     Metric.CTR,
                     Metric.ECPM_AD,
                     Metric.COST_PER_CONVERSION,
                     Metric.COST);

        return q.getResultAsMap();
    }


    @RequestMapping(value="/advertiser/{externalID}/statistics.{format}", method=RequestMethod.GET)
    public String getAdvertiserStats(HttpServletRequest request,
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
        Advertiser advertiser = getAdvertiserForUser(user, externalID, format, ADVERTISER_FETCH_STRATEGY);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHH");
        TimeZone tz = user.getCompany().getDefaultTimeZone();
        sdf.setTimeZone(tz);
        Date startDate = sdf.parse(start);
        Date endDate = sdf.parse(end);
        Range<Date> dateRange = new Range<Date>(startDate, endDate);
        Parameter.AdvertiserTimeByHour dateRangeParameter = new Parameter.AdvertiserTimeByHour(user.getCompany().getDefaultTimeZone(), dateRange);
        OLAPQuery q = new OLAPQuery(Locale.getDefault());
        q.addSlicers(dateRangeParameter);

        q.addParameters(new Parameter.ByDimension<Campaign>(Dimension.Advertiser, advertiser, Campaign.class, getCampaignResolver(CAMPAIGN_FETCH_STRATEGY)));

        model.addAttribute("result", getStatisticsMapImpl(q));
        model.addAttribute("unique", false);

        return format + "CampaignStatisticsView";
    }

    // Following are the many wrapper controllers for publication-list crud
    // GET Set
    
    @RequestMapping(value="/advertisers/publication-blacklists/{listname}.{format}", method=RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    @Transactional(readOnly=true)
    public @ResponseBody PublicationListDTO getCompanyPublicationBlacklist(@PathVariable("listname") String listName, 
                                                                @PathVariable String format,
                                                                HttpServletRequest request)throws WebServiceException{
        User user=authenticate(request, format);
        
        return getPublicationList(listName, user.getCompany().getId(), null, false, PublicationListLevel.COMPANY_LEVEL);
    }
    
    @RequestMapping(value="/advertisers/{externalID}/publication-blacklists/{listname}.{format}", method=RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    @Transactional(readOnly=true)
    public @ResponseBody PublicationListDTO getAdvertiserPublicationBlacklist(@PathVariable("listname") String listName, 
                                                                @PathVariable String format,
                                                                @PathVariable String externalID,
                                                                HttpServletRequest request)throws Exception{
        User user=authenticate(request, format);
        Advertiser advertiser = getAdvertiserForUser(user, externalID, format, ADVERTISER_FETCH_STRATEGY);
        
        return getPublicationList(listName, user.getCompany().getId(), advertiser.getId(), false, PublicationListLevel.ADVERTISER_LEVEL);
    }
    
    @RequestMapping(value="/advertisers/publication-whitelists/{listname}.{format}", method=RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    @Transactional(readOnly=true)
    public @ResponseBody PublicationListDTO getCompanyPublicationWhitelist(@PathVariable("listname") String listName, 
                                                                @PathVariable String format,
                                                                HttpServletRequest request)throws WebServiceException{
        User user=authenticate(request, format);
        
        return getPublicationList(listName, user.getCompany().getId(), null, true, PublicationListLevel.COMPANY_LEVEL);
    }
    
    @RequestMapping(value="/advertisers/{externalID}/publication-whitelists/{listname}.{format}", method=RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    @Transactional(readOnly=true)
    public @ResponseBody PublicationListDTO getAdvertiserPublicationWhitelist(@PathVariable("listname") String listName, 
                                                                @PathVariable String format,
                                                                @PathVariable String externalID,
                                                                HttpServletRequest request)throws Exception{
        User user=authenticate(request, format);
        Advertiser advertiser = getAdvertiserForUser(user, externalID, format, ADVERTISER_FETCH_STRATEGY);
        
        return getPublicationList(listName, user.getCompany().getId(), advertiser.getId(), true, PublicationListLevel.ADVERTISER_LEVEL);
    }
    
    @Autowired
    private IPublicationListService publicationListService;
    
    private PublicationListDTO getPublicationList(String name, long companyId, Long advertiserId, boolean isWhiteList, PublicationListLevel pubListLevel){
        return getPubListDto(getPublicationListDomain(name, companyId, advertiserId, isWhiteList, pubListLevel));
    }
    
    private PublicationList getPublicationListDomain(String name, long companyId, Long advertiserId, boolean isWhiteList, PublicationListLevel pubListLevel){
        PublicationList publicationList=publicationListService.getPublicationListByName(name, companyId, advertiserId, isWhiteList, pubListLevel);
        if(publicationList==null){
            throw new ServiceException(ErrorCode.ENTITY_NOT_FOUND, "List not found!");
        }
        return publicationList;
    }
    
    private PublicationListDTO getPubListDto(PublicationList pubList){
        PublicationListDTO pubListDto=new PublicationListDTO();
        Set<Publication> pubs=pubList.getPublications();
        List<String> pubExternalIds=new ArrayList<>(pubs.size());
        for(Publication pub:pubs){
            pubExternalIds.add(pub.getExternalID());
        }
        pubListDto.setPublications(pubExternalIds);
        return pubListDto;
    }
    
    
    // CREATE Set

    @RequestMapping(value="/advertisers/publication-blacklists/{listname}.{format}", method=RequestMethod.POST)
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    @Transactional(readOnly=false)
    public PublicationListDTO createCompanyPublicationBlackList(@PathVariable("listname") String listName, 
                                        @PathVariable String format, 
                                        @RequestBody PublicationListDTO publicationList,
                                        HttpServletRequest request)throws WebServiceException{
        User user=authenticate(request, format);
        
        return createPublicationList(listName, user.getCompany().getId(), null, false, PublicationListLevel.COMPANY_LEVEL, publicationList);
    }
    
    @RequestMapping(value="/advertisers/{externalID}/publication-blacklists/{listname}.{format}", method=RequestMethod.POST)
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    @Transactional(readOnly=false)
    public PublicationListDTO createAdvertiserPublicationBlackList(@PathVariable("listname") String listName, 
                                        @PathVariable String format,
                                        @PathVariable String externalID,
                                        @RequestBody PublicationListDTO publicationList,
                                        HttpServletRequest request)throws Exception{
        User user=authenticate(request, format);
        Advertiser advertiser = getAdvertiserForUser(user, externalID, format, ADVERTISER_FETCH_STRATEGY);
        
        return createPublicationList(listName, user.getCompany().getId(), advertiser.getId(), false, PublicationListLevel.ADVERTISER_LEVEL, publicationList);
    }
    
    @RequestMapping(value="/advertisers/publication-whitelists/{listname}.{format}", method=RequestMethod.POST)
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    @Transactional(readOnly=false)
    public PublicationListDTO createCompanyPublicationWhiteList(@PathVariable("listname") String listName, 
                                        @PathVariable String format, 
                                        @RequestBody PublicationListDTO publicationList,
                                        HttpServletRequest request)throws WebServiceException{
        User user=authenticate(request, format);
        
        return createPublicationList(listName, user.getCompany().getId(), null, true, PublicationListLevel.COMPANY_LEVEL, publicationList);
    }
    
    @RequestMapping(value="/advertisers/{externalID}/publication-whitelists/{listname}.{format}", method=RequestMethod.POST)
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    @Transactional(readOnly=false)
    public PublicationListDTO createAdvertiserPublicationWhiteList(@PathVariable("listname") String listName, 
                                        @PathVariable String format,
                                        @PathVariable String externalID,
                                        @RequestBody PublicationListDTO publicationList,
                                        HttpServletRequest request)throws Exception{
        User user=authenticate(request, format);
        Advertiser advertiser = getAdvertiserForUser(user, externalID, format, ADVERTISER_FETCH_STRATEGY);
        
        return createPublicationList(listName, user.getCompany().getId(), advertiser.getId(), true, PublicationListLevel.ADVERTISER_LEVEL, publicationList);
    }
    
    private PublicationListDTO createPublicationList(String name, long companyId, Long advertiserId, boolean isWhiteList, PublicationListLevel pubListLevel, PublicationListDTO publicationListDto){
        PublicationList publicationList=publicationListService.createPublicationList(name, companyId, advertiserId, isWhiteList, pubListLevel, publicationListDto.getPublications());

        return getPubListDto(publicationList);
    }
    

    // UPDATE Set
    
    @RequestMapping(value="/advertisers/publication-blacklists/{listname}.{format}", method=RequestMethod.PUT)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    @Transactional(readOnly=false)
    public PublicationListDTO updateCompanyPublicationBlackList(@PathVariable("listname") String listName, 
                                        @PathVariable String format, 
                                        @RequestBody PublicationListDTO publicationList,
                                        HttpServletRequest request)throws WebServiceException{
        User user=authenticate(request, format);
        
        PublicationList publicationListDomain=getPublicationListDomain(listName, user.getCompany().getId(), null, false, PublicationListLevel.COMPANY_LEVEL);
        // need not fetch again. doesn't hurt
        return getPubListDto(publicationListService.updatePublicationList(publicationListDomain.getId(), publicationList.getPublications()));
    }
    
    @RequestMapping(value="/advertisers/{externalID}/publication-blacklists/{listname}.{format}", method=RequestMethod.PUT)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    @Transactional(readOnly=false)
    public PublicationListDTO updateAdvertiserPublicationBlackList(@PathVariable("listname") String listName, 
                                        @PathVariable String format,
                                        @PathVariable String externalID,
                                        @RequestBody PublicationListDTO publicationList,
                                        HttpServletRequest request)throws Exception{
        User user=authenticate(request, format);
        Advertiser advertiser = getAdvertiserForUser(user, externalID, format, ADVERTISER_FETCH_STRATEGY);
        
        PublicationList publicationListDomain=getPublicationListDomain(listName, user.getCompany().getId(), advertiser.getId(), false, PublicationListLevel.ADVERTISER_LEVEL);
        return getPubListDto(publicationListService.updatePublicationList(publicationListDomain.getId(), publicationList.getPublications()));
    }
    
    @RequestMapping(value="/advertisers/publication-whitelists/{listname}.{format}", method=RequestMethod.PUT)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    @Transactional(readOnly=false)
    public PublicationListDTO updateCompanyPublicationWhiteList(@PathVariable("listname") String listName, 
                                        @PathVariable String format, 
                                        @RequestBody PublicationListDTO publicationList,
                                        HttpServletRequest request)throws WebServiceException{
        User user=authenticate(request, format);
        
        PublicationList publicationListDomain=getPublicationListDomain(listName, user.getCompany().getId(), null, true, PublicationListLevel.COMPANY_LEVEL);
        return getPubListDto(publicationListService.updatePublicationList(publicationListDomain.getId(), publicationList.getPublications()));
    }
    
    @RequestMapping(value="/advertisers/{externalID}/publication-whitelists/{listname}.{format}", method=RequestMethod.PUT)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    @Transactional(readOnly=false)
    public PublicationListDTO updateAdvertiserPublicationWhiteList(@PathVariable("listname") String listName, 
                                        @PathVariable String format,
                                        @PathVariable String externalID,
                                        @RequestBody PublicationListDTO publicationList,
                                        HttpServletRequest request)throws Exception{
        User user=authenticate(request, format);
        Advertiser advertiser = getAdvertiserForUser(user, externalID, format, ADVERTISER_FETCH_STRATEGY);
        
        PublicationList publicationListDomain=getPublicationListDomain(listName, user.getCompany().getId(), advertiser.getId(), true, PublicationListLevel.ADVERTISER_LEVEL);
        return getPubListDto(publicationListService.updatePublicationList(publicationListDomain.getId(), publicationList.getPublications()));
    }
    
    
    // DELETE Set
    
    @RequestMapping(value="/advertisers/publication-blacklists/{listname}.{format}", method=RequestMethod.DELETE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Transactional(readOnly=false)
    public void deleteCompanyPublicationBlackList(@PathVariable("listname") String listName, 
                                        @PathVariable String format, 
                                        HttpServletRequest request)throws WebServiceException{
        User user=authenticate(request, format);
        
        PublicationList publicationList=getPublicationListDomain(listName, user.getCompany().getId(), null, false, PublicationListLevel.COMPANY_LEVEL);
        publicationListService.deletePublicationList(publicationList.getId());
    }
    
    @RequestMapping(value="/advertisers/{externalID}/publication-blacklists/{listname}.{format}", method=RequestMethod.DELETE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Transactional(readOnly=false)
    public void deleteAdvertiserPublicationBlackList(@PathVariable("listname") String listName, 
                                        @PathVariable String format,
                                        @PathVariable String externalID,
                                        HttpServletRequest request)throws Exception{
        User user=authenticate(request, format);
        Advertiser advertiser = getAdvertiserForUser(user, externalID, format, ADVERTISER_FETCH_STRATEGY);
        
        PublicationList publicationList=getPublicationListDomain(listName, user.getCompany().getId(), advertiser.getId(), false, PublicationListLevel.ADVERTISER_LEVEL);
        publicationListService.deletePublicationList(publicationList.getId());
    }
    
    @RequestMapping(value="/advertisers/publication-whitelists/{listname}.{format}", method=RequestMethod.DELETE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Transactional(readOnly=false)
    public void deleteCompanyPublicationWhiteList(@PathVariable("listname") String listName, 
                                        @PathVariable String format, 
                                        HttpServletRequest request)throws WebServiceException{
        User user=authenticate(request, format);
        
        PublicationList publicationList=getPublicationListDomain(listName, user.getCompany().getId(), null, true, PublicationListLevel.COMPANY_LEVEL);
        publicationListService.deletePublicationList(publicationList.getId());
    }
    
    @RequestMapping(value="/advertisers/{externalID}/publication-whitelists/{listname}.{format}", method=RequestMethod.DELETE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Transactional(readOnly=false)
    public void deleteAdvertiserPublicationWhiteList(@PathVariable("listname") String listName, 
                                        @PathVariable String format,
                                        @PathVariable String externalID,
                                        HttpServletRequest request)throws Exception{
        User user=authenticate(request, format);
        Advertiser advertiser = getAdvertiserForUser(user, externalID, format, ADVERTISER_FETCH_STRATEGY);
        
        PublicationList publicationList=getPublicationListDomain(listName, user.getCompany().getId(), advertiser.getId(), true, PublicationListLevel.ADVERTISER_LEVEL);
        publicationListService.deletePublicationList(publicationList.getId());
    }
    
}
