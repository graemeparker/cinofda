package com.adfonic.presentation.dashboard.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.adfonic.dto.NameIdBusinessDto;
import com.adfonic.dto.advertiser.enums.AdvertiserStatus;
import com.adfonic.dto.campaign.enums.BidType;
import com.adfonic.dto.campaign.enums.CampaignStatus;
import com.adfonic.dto.dashboard.AgencyConsoleDashboardDto;
import com.adfonic.dto.dashboard.BaseDashboardDto;
import com.adfonic.dto.dashboard.DashboardDto;
import com.adfonic.dto.dashboard.DashboardParameters.AgencyConsoleSortBy;
import com.adfonic.dto.dashboard.DashboardParameters.Interval;
import com.adfonic.dto.dashboard.DashboardParameters.OrderBy;
import com.adfonic.dto.dashboard.DashboardParameters.PublisherReport;
import com.adfonic.dto.dashboard.DashboardParameters.PublisherSortBy;
import com.adfonic.dto.dashboard.DashboardParameters.Report;
import com.adfonic.dto.dashboard.DashboardParameters.SortBy;
import com.adfonic.dto.dashboard.PublisherDashboardDto;
import com.adfonic.dto.dashboard.statistic.AdvertiserHeadlineStatsDto;
import com.adfonic.dto.dashboard.statistic.AgencyConsoleStatisticsDto;
import com.adfonic.dto.dashboard.statistic.PublisherHeadlineStatsDto;
import com.adfonic.dto.dashboard.statistic.PublisherStatisticsDto;
import com.adfonic.dto.dashboard.statistic.StatisticsDto;
import com.adfonic.dto.publication.enums.Approval;
import com.adfonic.dto.publication.enums.Backfill;
import com.adfonic.dto.publication.enums.PublicationStatus;
import com.adfonic.dto.publication.publicationtype.PublicationtypeDto;
import com.adfonic.dto.publication.typeahead.PublicationTypeAheadDto;
import com.adfonic.presentation.dashboard.DashboardService;
import com.adfonic.presentation.dashboard.statistics.AdvertiserDashboardDao;
import com.adfonic.presentation.dashboard.statistics.AgencyConsoleDashboardDao;
import com.adfonic.presentation.dashboard.statistics.PublisherDashboardDao;
import com.adfonic.presentation.publication.service.PublicationService;
import com.adfonic.presentation.util.GenericServiceImpl;

@Service
public class DashboardServiceImpl extends GenericServiceImpl implements DashboardService {

    @Autowired
    private AdvertiserDashboardDao advertiserDashboardDao;
    
    @Autowired
    private PublisherDashboardDao publisherDashboardDao;
    
    @Autowired
    private AgencyConsoleDashboardDao agencyConsoleDashboardDao;
    
    @Autowired
    private PublicationService pService;

    public DashboardDto getDashboardHeader(final DashboardDto dashboardDto) {
        AdvertiserHeadlineStatsDto advertiserHeadlineStatsDto = null;

        Long advertiserId = dashboardDto.getAdvertiser().getId();
        int dateRange = Integer.parseInt(dashboardDto.getDatePickerPresetValue());
        BidType bidType = dashboardDto.getBidTypeFilter(); // dashboardDto.get
        CampaignStatus campaignStatus = dashboardDto.getCampaignStatusFilter(); // dashboardDto.get

        if (dashboardDto.isFiltered()) { // get filtered campaigns
            List<Long> campaignsIds = null;
            if(!CollectionUtils.isEmpty(dashboardDto.getCampaigns())){
                campaignsIds =  getIdsForBusinessKeyDtos(dashboardDto.getCampaigns());
            }
            		//getIdsForBusinessKeyDtos(dashboardDto.getCampaignsFiltered());
            //if(CollectionUtils.isEmpty(campaignsIds))campaignsIds=null;
            advertiserHeadlineStatsDto = advertiserDashboardDao.getHeadlineFiguresForCampaigns(advertiserId,
                    campaignsIds, campaignStatus, bidType, dateRange,dashboardDto.isShowDeletedCampaigns());
        }
        else if ( CollectionUtils.isEmpty(dashboardDto.getCampaigns())) { // for advertiser
            advertiserHeadlineStatsDto = advertiserDashboardDao.getHeadlineFiguresForAdvertiser(advertiserId, campaignStatus, bidType, dateRange,dashboardDto.isShowDeletedCampaigns());
        }
         
        else { // all campaigns
            List<Long> campaignsIds = getIdsForBusinessKeyDtos(dashboardDto.getCampaigns());
            advertiserHeadlineStatsDto = advertiserDashboardDao.getHeadlineFiguresForCampaigns(advertiserId,
                    campaignsIds, campaignStatus, bidType, dateRange,dashboardDto.isShowDeletedCampaigns());
        }

        dashboardDto.setStatisticsDto(advertiserHeadlineStatsDto);
        return dashboardDto;
    }

    @Override
    public DashboardDto getReportingTable(final DashboardDto dashboardDto) {

        List<StatisticsDto> statisticsDtos = null;
        Long numTotalRecords = 0L;

        Long advertiserId = dashboardDto.getAdvertiser().getId();
        int dateRange = Integer.parseInt(dashboardDto.getDatePickerPresetValue());
        BidType bidType = dashboardDto.getBidTypeFilter(); // dashboardDto.get
        CampaignStatus campaignStatus = dashboardDto.getCampaignStatusFilter(); // dashboardDto.get
        SortBy sortBy = dashboardDto.getSortBy();
        OrderBy orderBy = dashboardDto.getOrderBy();
        Long start = dashboardDto.getStart(); //
        Long numberOfRecords = dashboardDto.getNumberOfRecords();//

        //Order matters! first check should be isFiltered
        if (dashboardDto.isFiltered()) { // get filtered campaigns
            List<Long> campaignIds = null;
            if(!CollectionUtils.isEmpty(dashboardDto.getCampaigns())){
                campaignIds = getIdsForBusinessKeyDtos(dashboardDto.getCampaigns());
            }
            		//getIdsForBusinessKeyDtos(dashboardDto.getCampaignsFiltered());
           // if(CollectionUtils.isEmpty(campaignIds))campaignIds=null;
            statisticsDtos = advertiserDashboardDao.getDashboardReportingTableForCampaigns(advertiserId, campaignIds,
                    campaignStatus, bidType, dateRange, sortBy, orderBy, start, numberOfRecords,dashboardDto.isShowDeletedCampaigns());
             numTotalRecords = advertiserDashboardDao.getNumberOfRecordsForDashboardReportingTableForCampaigns(advertiserId, campaignIds, campaignStatus, bidType, dateRange,dashboardDto.isShowDeletedCampaigns());
        }
        else if ( CollectionUtils.isEmpty(dashboardDto.getCampaigns()) ) { // for
                                                                                            // advertiser
            statisticsDtos = advertiserDashboardDao.getDashboardReportingTableForAdvertiser(advertiserId,
                    campaignStatus, bidType, dateRange, sortBy, orderBy, start, numberOfRecords,dashboardDto.isShowDeletedCampaigns());
             numTotalRecords = advertiserDashboardDao.getNumberOfRecordsForDashboardReportingTableForAdvertiser(advertiserId, campaignStatus, bidType, dateRange,dashboardDto.isShowDeletedCampaigns());
        }
        
        else { // all campaigns
            List<Long> campaignIds = getIdsForBusinessKeyDtos(dashboardDto.getCampaigns());
            statisticsDtos = advertiserDashboardDao.getDashboardReportingTableForCampaigns(advertiserId, campaignIds,
                    campaignStatus, bidType, dateRange, sortBy, orderBy, start, numberOfRecords,dashboardDto.isShowDeletedCampaigns());
             numTotalRecords = advertiserDashboardDao.getNumberOfRecordsForDashboardReportingTableForCampaigns(advertiserId, campaignIds, campaignStatus, bidType, dateRange,dashboardDto.isShowDeletedCampaigns());
        }
        dashboardDto.setNumTotalRecords(numTotalRecords);
        dashboardDto.setReportingTable(statisticsDtos);
        return dashboardDto;
    }

    @Override
    public List<Map<Object, Number>> getChartData(DashboardDto dashboardDto, Report type, boolean bigChart) {
        Long advertiserId = dashboardDto.getAdvertiser().getId();
        List<Map<Object, Number>> result = new ArrayList<Map<Object,Number>>();
        int dateRange = Integer.parseInt(dashboardDto.getDatePickerPresetValue());
        BidType bidType = dashboardDto.getBidTypeFilter(); // dashboardDto.get
        CampaignStatus campaignStatus = dashboardDto.getCampaignStatusFilter(); // dashboardDto.get

        //Order matters! first check should be isFiltered
        if (dashboardDto.isFiltered()) { // get filtered campaigns
            if(dashboardDto.isIndividualLines() && bigChart){//Separated data
                for(NameIdBusinessDto dto : dashboardDto.getCampaigns()){
                    List<Long> ids = new ArrayList<Long>();
                    ids.add(dto.getId());
                    result.add(advertiserDashboardDao.getChartDataForCampaign(advertiserId, ids, campaignStatus, bidType, dateRange, type,dashboardDto.isShowDeletedCampaigns()));
                }
            }
            else{
                List<Long> campaignIds = null;
                if(!CollectionUtils.isEmpty(dashboardDto.getCampaigns())){
                    campaignIds = getIdsForBusinessKeyDtos(dashboardDto.getCampaigns());
                }
                result.add(advertiserDashboardDao
                    .getChartDataForCampaign(advertiserId, campaignIds, campaignStatus, bidType, dateRange, type,dashboardDto.isShowDeletedCampaigns()));
            }
        }
        else if ( CollectionUtils.isEmpty(dashboardDto.getCampaigns())) { // for advertiser
            result.add(advertiserDashboardDao.getChartDataForAdvertiser(advertiserId, dateRange, type, campaignStatus, bidType,dashboardDto.isShowDeletedCampaigns()));
        }
        
        else { // all campaigns
            List<Long> campaignIds = getIdsForBusinessKeyDtos(dashboardDto.getCampaigns());
            result.add(advertiserDashboardDao
                    .getChartDataForCampaign(advertiserId, campaignIds, campaignStatus, bidType, dateRange, type,dashboardDto.isShowDeletedCampaigns()));
        }
        return result;
    }
    
  //************************Public Methods*****************************//
    
    public BaseDashboardDto getDashboardHeader(BaseDashboardDto dashboardDto) {
        if(dashboardDto instanceof PublisherDashboardDto){
            PublisherDashboardDto pubDashDto = (PublisherDashboardDto)dashboardDto;           
            dashboardDto = getPublisherDashboardHeader(pubDashDto);
        }
        else{
            //TODO dashboardDto = getAdvertiserDashboardHeader(dashboardDto);
        }        
        return dashboardDto;
    }

    @Override
    public BaseDashboardDto getReportingTable(BaseDashboardDto dashboardDto) {
        if(dashboardDto instanceof PublisherDashboardDto){
            PublisherDashboardDto pubDashDto = (PublisherDashboardDto)dashboardDto;           
            dashboardDto = getPublisherReportingTable(pubDashDto);
        }
        else if(dashboardDto instanceof AgencyConsoleDashboardDto){
            AgencyConsoleDashboardDto acDashDto = (AgencyConsoleDashboardDto) dashboardDto;
            dashboardDto = getAgencyConsoleReportingTable(acDashDto);
        }
        else{
            //TODO dashboardDto = getAdvertiserDashboardHeader(dashboardDto);
        }   
        
        return dashboardDto;
    }

    @Override
    public List<Map<Object, Number>> getChartData(BaseDashboardDto dashboardDto, Report type, PublisherReport pType, Interval interval,boolean bigChart) {
        List<Map<Object, Number>> result = null;
        
        if(dashboardDto instanceof PublisherDashboardDto){
            PublisherDashboardDto pubDashDto = (PublisherDashboardDto)dashboardDto;           
            result = getChartData(pubDashDto, pType, interval, bigChart);
        }
        else if(dashboardDto instanceof AgencyConsoleDashboardDto){
            AgencyConsoleDashboardDto acDashDto = (AgencyConsoleDashboardDto) dashboardDto;
            result = getAgencyConsoleChartData(acDashDto, interval, bigChart);
        }
        else{
            //TODO dashboardDto = getAdvertiserDashboardHeader(dashboardDto);
        }   
        
        return result;
    }
    
    
    //************************Private Methods*****************************//
    
    
    private BaseDashboardDto getPublisherDashboardHeader(final PublisherDashboardDto dashboardDto) {
        PublisherHeadlineStatsDto publisherHeadlineStatsDto = null;

        Long publisherId = dashboardDto.getPublisherDto().getId();
        int dateRange = Integer.parseInt(dashboardDto.getDatePickerPresetValue());
        PublicationtypeDto platform = dashboardDto.getPlatformFilter();
        if(platform==null || platform.getId()==null){
            platform = new PublicationtypeDto();
            platform.setId(-1L);
        }
        Approval approval = dashboardDto.getApprovalFilter();
        Backfill backfill = dashboardDto.getBackfillFilter();
        PublicationStatus publicationStatus = dashboardDto.getPublicationStatusFilter(); 
        
        //Order matters! first check should be isFiltered
        if (dashboardDto.isFiltered()) { // get filtered publications
            List<Long> publicationIds = null;
            if(!CollectionUtils.isEmpty(dashboardDto.getPublications())){
                publicationIds = getIdsForBusinessKeyDtos(dashboardDto.getPublications());
            }
                    //getIdsForBusinessKeyDtos(dashboardDto.getCampaignsFiltered());
            //if(CollectionUtils.isEmpty(campaignsIds))campaignsIds=null;
            publisherHeadlineStatsDto = publisherDashboardDao.getHeadlineFiguresForPublications(publisherId,
                    publicationIds, publicationStatus, platform, approval, backfill,dateRange);
        }
        else if ( CollectionUtils.isEmpty(dashboardDto.getPublications())) { // for advertiser
            publisherHeadlineStatsDto = publisherDashboardDao.getHeadlineFiguresForPublisher(publisherId, publicationStatus, platform,
                    approval, backfill,dateRange);
        }
         
        else { // all publications
            List<Long> publicationIds = getIdsForBusinessKeyDtos(dashboardDto.getPublications());
            publisherHeadlineStatsDto = publisherDashboardDao.getHeadlineFiguresForPublications(publisherId,
                    publicationIds, publicationStatus, platform, approval, backfill,dateRange);
        }

        dashboardDto.setPublisherHeadlineStatsDto(publisherHeadlineStatsDto);
        return dashboardDto;
    }

    
 
    
    private PublisherDashboardDto getPublisherReportingTable(final PublisherDashboardDto dashboardDto) {

        List<PublisherStatisticsDto> statisticsDtos = null;
        Long numTotalRecords = 0L;
        
        Long pubId = dashboardDto.getPublisherDto().getId();
        int dateRange = Integer.parseInt(dashboardDto.getDatePickerPresetValue());
        PublicationtypeDto platform = dashboardDto.getPlatformFilter();
        if(platform==null || platform.getId()==null){
            platform = new PublicationtypeDto();
            platform.setId(-1L);
        }
        Approval approval = dashboardDto.getApprovalFilter();
        Backfill backfill = dashboardDto.getBackfillFilter();
        PublicationStatus publicationStatus = dashboardDto.getPublicationStatusFilter(); 
        PublisherSortBy sortBy = dashboardDto.getSortBy();
        OrderBy orderBy = dashboardDto.getOrderBy();
        Long start = dashboardDto.getStart();
        Long numberOfRecords = dashboardDto.getNumberOfRecords();

        if (dashboardDto.isFiltered()) { // get filtered publications
            List<Long> publicationsIds = null;
            if(!CollectionUtils.isEmpty(dashboardDto.getPublications())){
                publicationsIds = getIdsForBusinessKeyDtos(dashboardDto.getPublications());
            }
                    //getIdsForBusinessKeyDtos(dashboardDto.getCampaignsFiltered());
           // if(CollectionUtils.isEmpty(campaignIds))campaignIds=null;
            statisticsDtos = publisherDashboardDao.getDashboardReportingTableForPublications(pubId, publicationsIds, publicationStatus, platform,
                    approval, backfill, dateRange, sortBy, orderBy, start, numberOfRecords);
             numTotalRecords = publisherDashboardDao.getNumberOfRecordsForDashboardReportingTableForPublications(pubId, publicationsIds,
                     publicationStatus, platform, approval, backfill, dateRange, sortBy);
        }
        else if ( CollectionUtils.isEmpty(dashboardDto.getPublications())) { // for
                                                                                            // publisher
            statisticsDtos = publisherDashboardDao.getDashboardReportingTableForPublisher(pubId, publicationStatus, platform, approval, 
                    backfill, dateRange, sortBy, orderBy, start, numberOfRecords);
            numTotalRecords = publisherDashboardDao.getNumberOfRecordsForDashboardReportingTableForPublisher(pubId, publicationStatus, 
                    platform, approval, backfill, dateRange, sortBy);
        }
        else { // all publications
            List<Long> publicationIds = getIdsForBusinessKeyDtos(dashboardDto.getPublications());
            statisticsDtos = publisherDashboardDao.getDashboardReportingTableForPublications(pubId, publicationIds, publicationStatus,
                    platform, approval, backfill, dateRange, sortBy, orderBy, start, numberOfRecords);
            numTotalRecords = publisherDashboardDao.getNumberOfRecordsForDashboardReportingTableForPublications(pubId, publicationIds, 
                    publicationStatus, platform, approval, backfill, dateRange, sortBy);
        }
        
        //Platforms only have name and must be filled
        for(PublisherStatisticsDto p : statisticsDtos){
            if(p.getPlatform()!=null && p.getPlatform().getName()!=null && !p.getPlatform().getName().equals("")){
                p.setPlatform(pService.getPublicationTypeByName(p.getPlatform().getName()));
            }
        }

        dashboardDto.setNumTotalRecords(numTotalRecords);
        dashboardDto.setReportingTable(statisticsDtos);
        return dashboardDto;
    }
    
    private List<Map<Object, Number>> getChartData(PublisherDashboardDto dashboardDto, PublisherReport type, Interval interval, boolean bigChart) {
        Long publisherId = dashboardDto.getPublisherDto().getId();
        List<Map<Object, Number>> result = new ArrayList<Map<Object,Number>>();
        int dateRange = Integer.parseInt(dashboardDto.getDatePickerPresetValue());
        PublicationtypeDto platform = dashboardDto.getPlatformFilter();
        if(platform==null || platform.getId()==null){
            platform = new PublicationtypeDto();
            platform.setId(-1L);
        }
        Approval approval = dashboardDto.getApprovalFilter();
        Backfill backfill = dashboardDto.getBackfillFilter();
        PublicationStatus publicationStatus = dashboardDto.getPublicationStatusFilter(); 
        
        //Order matters! first check should be isFiltered
        if (dashboardDto.isFiltered()) { // get filtered campaigns
            if(dashboardDto.isIndividualLines() && bigChart){//Separated data
                for(PublicationTypeAheadDto dto : dashboardDto.getPublications()){
                    List<Long> ids = new ArrayList<Long>();
                    ids.add(dto.getId());
                    result.add(publisherDashboardDao.getChartDataForPublication(publisherId, ids, publicationStatus, platform,
                            approval, backfill,dateRange, type, interval));
                }
            }
            else{
                List<Long> publicationIds = null;
                if(!CollectionUtils.isEmpty(dashboardDto.getPublications())){
                    publicationIds = getIdsForBusinessKeyDtos(dashboardDto.getPublications());
                }
                result.add(publisherDashboardDao
                    .getChartDataForPublication(publisherId, publicationIds, publicationStatus, platform,
                            approval, backfill,dateRange, type, interval));
            }
        }
        else if ( CollectionUtils.isEmpty(dashboardDto.getPublications())) { // for publisher
            result.add(publisherDashboardDao.getChartDataForPublisher(publisherId, publicationStatus, platform,
                    approval, backfill, dateRange, type, interval));
        }
        
        else { // all campaigns
            List<Long> publicationIds = getIdsForBusinessKeyDtos(dashboardDto.getPublications());
            result.add(publisherDashboardDao
                    .getChartDataForPublication(publisherId, publicationIds, publicationStatus, platform,
                            approval, backfill, dateRange, type, interval));
        }
        return result;
    }
    
    private AgencyConsoleDashboardDto getAgencyConsoleReportingTable(final AgencyConsoleDashboardDto dashboardDto) {
        List<AgencyConsoleStatisticsDto> statisticsDtos = null;
        Long numTotalRecords = 0L;

        int dateRange = Integer.parseInt(dashboardDto.getDatePickerPresetValue());
        AgencyConsoleSortBy sortBy = dashboardDto.getSortBy();
        OrderBy orderBy = dashboardDto.getOrderBy();
        Long start = dashboardDto.getStart(); //
        Long numberOfRecords = dashboardDto.getNumberOfRecords();
        AdvertiserStatus advertiserStatus = dashboardDto.getStatusFilter(); 

        List<Long> advertisersIds = null;
        if(!CollectionUtils.isEmpty(dashboardDto.getAdvertisersRequested())){
            advertisersIds = getIdsForBusinessKeyDtos(dashboardDto.getAdvertisersRequested());
        }
        statisticsDtos = agencyConsoleDashboardDao.getDashboardReportingTable(advertisersIds, advertiserStatus,dateRange, sortBy, orderBy, start, numberOfRecords);
        numTotalRecords = agencyConsoleDashboardDao.getNumberOfRecordsForDashboardReportingTable(advertisersIds, advertiserStatus,dateRange);
        
        dashboardDto.setNumTotalRecords(numTotalRecords);
        dashboardDto.setReportingTable(statisticsDtos);
        return dashboardDto;
    }
    
    private List<Map<Object, Number>> getAgencyConsoleChartData(AgencyConsoleDashboardDto dashboardDto, Interval interval, boolean bigChart) {
        List<Map<Object, Number>> result = new ArrayList<Map<Object,Number>>();
        int dateRange = Integer.parseInt(dashboardDto.getDatePickerPresetValue());
        AdvertiserStatus advertiserStatus = dashboardDto.getStatusFilter(); 

        List<Long> advertisersIds = null;
        if(!CollectionUtils.isEmpty(dashboardDto.getAdvertisersRequested())){
            advertisersIds = getIdsForBusinessKeyDtos(dashboardDto.getAdvertisersRequested());
        }
        result.add(agencyConsoleDashboardDao.getChartData(advertisersIds, advertiserStatus, dateRange));

        return result;
    }
    
    
/**  TODO: USE WHEN ADVERTISERDASHBOARD EXTENDS BASEDASHBOARD*/
//  private BaseDashboardDto getAdvertiserDashboardHeader(final BaseDashboardDto dashboardDto) {
//  AdvertiserHeadlineStatsDto advertiserHeadlineStatsDto = null;
//
//  Long advertiserId = dashboardDto.getAdvertiser().getId();
//  Date from = dashboardDto.getFrom();
//  DateTime end = new DateTime(dashboardDto.getTo());
//    if(end.getMinuteOfHour()==59)
//        end = end.plusMinutes(1);
//    Date to = end.toDate();
//  
//  /** HACKED UNTIL PROCEDURES ERROR GET SOLVED*/
//  advertiserId = new Long(21557);
//  from = new DateTime().minusDays(10).toDate();
//  to = new DateTime().toDate();
//  //Order matters! first check should be isFiltered
//  if (dashboardDto.isFiltered()) { // get filtered campaigns
//      List<Long> campaignsIds =  dashboardDto.getCampaignsIdFiltered();
//              //getIdsForBusinessKeyDtos(dashboardDto.getCampaignsFiltered());
//      //if(CollectionUtils.isEmpty(campaignsIds))campaignsIds=null;
//      advertiserHeadlineStatsDto = advertiserDashboardDao.getHeadlineFiguresForCampaigns(advertiserId,
//              campaignsIds, from, to);
//  }
//  else if ( CollectionUtils.isEmpty(dashboardDto.getCampaigns())) { // for advertiser
//      advertiserHeadlineStatsDto = advertiserDashboardDao.getHeadlineFiguresForAdvertiser(advertiserId, from, to);
//  }
//   
//  else { // all campaigns
//      List<Long> campaignsIds = getIdsForBusinessKeyDtos(dashboardDto.getCampaigns());
//      advertiserHeadlineStatsDto = advertiserDashboardDao.getHeadlineFiguresForCampaigns(advertiserId,
//              campaignsIds, from, to);
//  }
//
//  dashboardDto.setStatisticsDto(advertiserHeadlineStatsDto);
//  return dashboardDto;
  
//  private DashboardDto getAdvertiserReportingTable(final DashboardDto dashboardDto) {
//
//    List<StatisticsDto> statisticsDtos = null;
//    Long numTotalRecords = 0L;
//
//    Long advertiserId = dashboardDto.getAdvertiser().getId();
//    Date from = dashboardDto.getFrom();
//    DateTime end = new DateTime(dashboardDto.getTo());
//        if(end.getMinuteOfHour()==59)
//            end = end.plusMinutes(1);
//        Date to = end.toDate();
//    BidType bidType = dashboardDto.getBidTypeFilter(); // dashboardDto.get
//    CampaignStatus campaignStatus = dashboardDto.getCampaignStatusFilter(); // dashboardDto.get
//    SortBy sortBy = dashboardDto.getSortBy();
//    OrderBy orderBy = dashboardDto.getOrderBy();
//    Long start = dashboardDto.getStart(); //
//    Long numberOfRecords = dashboardDto.getNumberOfRecords();//
//    
//    /** HACKED UNTIL PROCEDURES ERROR GET SOLVED*/
//    advertiserId = new Long(21557);
//    from = new DateTime().minusDays(10).toDate();
//    to = new DateTime().toDate();
//    //numberOfRecords = 10L;
//    /*
//    Date from = new DateTime().minusDays(10).toDate();
//    Date to = new DateTime().toDate();
//    Long advertiserId = new Long(21557);
//    Long numberOfRecords = 10L;
//    SortBy sortBy = SortBy.CAMPAIGN_NAME;
//    OrderBy orderBy = OrderBy.ASCENDING;
//    Long start = 0L;
//    CampaignStatus campaignStatus = null;
//    BidType bidType = null;
//    */
//    //Order matters! first check should be isFiltered
//    if (dashboardDto.isFiltered()) { // get filtered campaigns
//        List<Long> campaignIds = dashboardDto.getCampaignsIdFiltered();
//                //getIdsForBusinessKeyDtos(dashboardDto.getCampaignsFiltered());
//       // if(CollectionUtils.isEmpty(campaignIds))campaignIds=null;
//        statisticsDtos = advertiserDashboardDao.getDashboardReportingTableForCampaigns(advertiserId, campaignIds,
//                campaignStatus, bidType, from, to, sortBy, orderBy, start, numberOfRecords);
//         numTotalRecords = advertiserDashboardDao.getNumberOfRecordsForDashboardReportingTableForCampaigns(advertiserId, campaignIds, campaignStatus, bidType, from, to);
//    }
//    else if ( CollectionUtils.isEmpty(dashboardDto.getCampaigns()) ) { // for
//                                                                                        // advertiser
//        statisticsDtos = advertiserDashboardDao.getDashboardReportingTableForAdvertiser(advertiserId,
//                campaignStatus, bidType, from, to, sortBy, orderBy, start, numberOfRecords);
//         numTotalRecords = advertiserDashboardDao.getNumberOfRecordsForDashboardReportingTableForAdvertiser(advertiserId, campaignStatus, bidType, from, to);
//    }
//    
//    else { // all campaigns
//        List<Long> campaignIds = getIdsForBusinessKeyDtos(dashboardDto.getCampaigns());
//        statisticsDtos = advertiserDashboardDao.getDashboardReportingTableForCampaigns(advertiserId, campaignIds,
//                campaignStatus, bidType, from, to, sortBy, orderBy, start, numberOfRecords);
//         numTotalRecords = advertiserDashboardDao.getNumberOfRecordsForDashboardReportingTableForCampaigns(advertiserId, campaignIds, campaignStatus, bidType, from, to);
//    }
//    dashboardDto.setNumTotalRecords(numTotalRecords);
//    dashboardDto.setReportingTable(statisticsDtos);
//    return dashboardDto;
//  }
//    
//    private Map<Object, Number> getChartData(PublisherDashboardDto dashboardDto, Report type, Interval interval) {
//        Long advertiserId = dashboardDto.getAdvertiser().getId();
//        Map<Object, Number> result = null;
//        Date from = dashboardDto.getFrom();
//        DateTime end = new DateTime(dashboardDto.getTo());
//    if(end.getMinuteOfHour()==59)
//        end = end.plusMinutes(1);
//    Date to = end.toDate();
//        
//        /** HACKED UNTIL PROCEDURES ERROR GET SOLVED*/
//        advertiserId = new Long(21557);
//        from = new DateTime().minusDays(10).toDate();
//        to = new DateTime().toDate();
//        //Order matters! first check should be isFiltered
//        if (dashboardDto.isFiltered()) { // get filtered campaigns
//            List<Long> campaignIds = dashboardDto.getCampaignsIdFiltered();
//            //getIdsForBusinessKeyDtos(dashboardDto.getCampaignsFiltered());
//           // if(CollectionUtils.isEmpty(campaignIds))campaignIds=null;
//            result = advertiserDashboardDao
//                    .getChartDataForCampaign(advertiserId, campaignIds, from, to, type, interval);
//        }
//        else if ( CollectionUtils.isEmpty(dashboardDto.getCampaigns())) { // for advertiser
//            result = advertiserDashboardDao.getChartDataForAdvertiser(advertiserId, from, to, type, interval);
//        }
//        
//        else { // all campaigns
//            List<Long> campaignIds = getIdsForBusinessKeyDtos(dashboardDto.getCampaigns());
//            result = advertiserDashboardDao
//                    .getChartDataForCampaign(advertiserId, campaignIds, from, to, type, interval);
//        }
//        return result;
//    }

}
