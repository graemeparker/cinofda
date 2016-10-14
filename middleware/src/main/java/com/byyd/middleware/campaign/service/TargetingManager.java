package com.byyd.middleware.campaign.service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Set;

import com.adfonic.domain.AdSpace;
import com.adfonic.domain.Advertiser;
import com.adfonic.domain.Campaign;
import com.adfonic.domain.CampaignTimePeriod;
import com.adfonic.domain.Country;
import com.adfonic.domain.Geotarget;
import com.adfonic.domain.GeotargetList;
import com.adfonic.domain.GeotargetType;
import com.adfonic.domain.LocationTarget;
import com.adfonic.domain.Segment;
import com.byyd.middleware.campaign.filter.GeotargetFilter;
import com.byyd.middleware.campaign.filter.LocationTargetFilter;
import com.byyd.middleware.campaign.filter.SegmentStateSyncingFilter;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.LikeSpec;
import com.byyd.middleware.iface.dao.Pagination;
import com.byyd.middleware.iface.dao.Sorting;
import com.byyd.middleware.iface.service.BaseManager;

public interface TargetingManager extends BaseManager {
    
    //------------------------------------------------------------------------------------------
    // Segment
    //------------------------------------------------------------------------------------------
    Segment newSegment(Advertiser advertiser, String name, FetchStrategy... fetchStrategy);
    Segment newSegment(Segment segment, FetchStrategy... fetchStrategy);

    void syncStates(Segment source, Segment destination, SegmentStateSyncingFilter parameters);
    Segment copySegment(Segment segment, String name, FetchStrategy... fetchStrategy);

    Segment getSegmentById(String id, FetchStrategy... fetchStrategy);
    Segment getSegmentById(Long id, FetchStrategy... fetchStrategy);
    Segment update(Segment segment);
    void delete(Segment segment);
    void deleteSegments(List<Segment> list);

    boolean isAdSpaceAlreadyAllocated(Segment segment, AdSpace adSpace);
    
    Long countBrowsersForSegment(Segment segment);
    Long countCategoriesForSegment(Segment segment);
    Long countExcludedCategoriesForSegment(Segment segment);
    Long countChannelsForSegment(Segment segment);    
    Long countCountriesForSegment(Segment segment);
    Long countGeotargetsForSegment(Segment segment);
    Long countIpAddressesForSegment(Segment segment);
    Long countModelsForSegment(Segment segment);     
    Long countExcludedModelsForSegment(Segment segment);     
    Long countOperatorsForSegment(Segment segment);
    Long countPlatformsForSegment(Segment segment);
    Long countPublishersForSegment(Segment segment);
    Long countVendorsForSegment(Segment segment);

    //------------------------------------------------------------------------------------------
    // LocationTarget
    //------------------------------------------------------------------------------------------
    LocationTarget newLocationTarget(Advertiser advertiser, String name, BigDecimal latitude, BigDecimal longitude, BigDecimal radiusMiles, FetchStrategy... fetchStrategy);
    LocationTarget copyLocationTarget(LocationTarget source);
    LocationTarget getLocationTargetById(String id, FetchStrategy... fetchStrategy);
    LocationTarget getLocationTargetById(Long id, FetchStrategy... fetchStrategy);
    List<LocationTarget> getLocationTargetByIds(List<Long> ids, FetchStrategy... fetchStrategy);
    LocationTarget update(LocationTarget publicationList);
    void delete(LocationTarget publicationList);
    void deleteLocationTargets(List<LocationTarget> list);

    Long countAllLocationTargets(LocationTargetFilter filter);
    List<LocationTarget> getAllLocationTargets(LocationTargetFilter filter, FetchStrategy ... fetchStrategy);
    List<LocationTarget> getAllLocationTargets(LocationTargetFilter filter, Sorting sort, FetchStrategy ... fetchStrategy);
    List<LocationTarget> getAllLocationTargets(LocationTargetFilter filter, Pagination page, FetchStrategy ... fetchStrategy);
    
    Long countAllLocationTargetsForAdvertiser(Advertiser advertiser);
    List<LocationTarget> getAllLocationTargetsForAdvertiser(Advertiser advertiser, FetchStrategy ... fetchStrategy);
    List<LocationTarget> getAllLocationTargetsForAdvertiser(Advertiser advertiser, Sorting sort, FetchStrategy ... fetchStrategy);
    List<LocationTarget> getAllLocationTargetsForAdvertiser(Advertiser advertiser, Pagination page, FetchStrategy ... fetchStrategy);
    
    Long countAllLocationTargetsForAdvertiserAndPartialName(Advertiser advertiser, String name, LikeSpec likeSpec, boolean caseSensitive);
    List<LocationTarget> getAllLocationTargetsForAdvertiserAndPartialName(Advertiser advertiser, String name, LikeSpec likeSpec, boolean caseSensitive, FetchStrategy ... fetchStrategy);
    List<LocationTarget> getAllLocationTargetsForAdvertiserAndPartialName(Advertiser advertiser, String name, LikeSpec likeSpec, boolean caseSensitive, Sorting sort, FetchStrategy ... fetchStrategy);
    List<LocationTarget> getAllLocationTargetsForAdvertiserAndPartialName(Advertiser advertiser, String name, LikeSpec likeSpec, boolean caseSensitive, Pagination page, FetchStrategy ... fetchStrategy);

    //------------------------------------------------------------------------------------------
    // CampaignTimePeriod
    //------------------------------------------------------------------------------------------
    CampaignTimePeriod newCampaignTimePeriod(Campaign campaign, FetchStrategy... fetchStrategy);
    CampaignTimePeriod newCampaignTimePeriod(Campaign campaign, Date startDate, Date endDate, FetchStrategy... fetchStrategy);

    boolean isTimePeriodOverlapping(Campaign campaign, CampaignTimePeriod timePeriod);

    Campaign addTimePeriodToCampaign(Campaign campaign, CampaignTimePeriod timePeriod);
    Campaign addTimePeriodsToCampaign(Campaign campaign, Set<CampaignTimePeriod> timePeriods);

    CampaignTimePeriod getCampaignTimePeriodById(String id, FetchStrategy... fetchStrategy);
    CampaignTimePeriod getCampaignTimePeriodById(Long id, FetchStrategy... fetchStrategy);
    CampaignTimePeriod create(CampaignTimePeriod campaignTimePeriod);
    CampaignTimePeriod update(CampaignTimePeriod campaignTimePeriod);
    void delete(CampaignTimePeriod campaignTimePeriod);
    void deleteCampaignTimePeriods(List<CampaignTimePeriod> list);

    Long countAllCampaignTimePeriodsForCampaign(Campaign campaign);
    List<CampaignTimePeriod> getAllCampaignTimePeriodsForCampaign(Campaign campaign, FetchStrategy... fetchStrategy);
    List<CampaignTimePeriod> getAllCampaignTimePeriodsForCampaign(Campaign campaign, Sorting sort, FetchStrategy... fetchStrategy);
    List<CampaignTimePeriod> getAllCampaignTimePeriodsForCampaign(Campaign campaign, Pagination page, FetchStrategy... fetchStrategy);

    //------------------------------------------------------------------------------------------
    // Geotarget
    //------------------------------------------------------------------------------------------

    Geotarget newGeotarget(String name, Country country, GeotargetType type, double displayLatitude, double displayLongitude, FetchStrategy... fetchStrategy);

    Geotarget getGeotargetById(String id, FetchStrategy... fetchStrategy);
    Geotarget getGeotargetById(Long id, FetchStrategy... fetchStrategy);
    Geotarget getGeotargetByName(String name, FetchStrategy ... fetchStrategy);
    
    Geotarget update(Geotarget geotarget);
    void delete(Geotarget geotarget);
    void deleteGeotargets(List<Geotarget> list);

    List<Geotarget> getAllGeotargets(FetchStrategy ... fetchStrategy);

    Long countAllGeotargets(GeotargetFilter filter);
    List<Geotarget> getAllGeotargets(GeotargetFilter filter, Sorting sort, FetchStrategy ... fetchStrategy);
    List<Geotarget> getAllGeotargets(GeotargetFilter filter, Pagination page, FetchStrategy ... fetchStrategy);

    Long countGeotargetsByNameAndTypeAndIsoCode(String isoCode, GeotargetType geotargetType, String name, boolean caseSensitive, LikeSpec like);
    List<Geotarget> getGeotargetsByNameAndTypeAndIsoCode(String isoCode, GeotargetType geotargetType, String name, boolean caseSensitive, LikeSpec like, FetchStrategy... fetchStrategy);
    List<Geotarget> getGeotargetsByNameAndTypeAndIsoCode(String isoCode, GeotargetType geotargetType, String name, boolean caseSensitive, LikeSpec like, Sorting sort, FetchStrategy... fetchStrategy);
    List<Geotarget> getGeotargetsByNameAndTypeAndIsoCode(String isoCode, GeotargetType geotargetType, String name, boolean caseSensitive, LikeSpec like, Pagination page, FetchStrategy... fetchStrategy);

    Long countGeotargetTypesForCountryIsoCode(String isoCode);
    List<GeotargetType> getGeotargetTypesForCountryIsoCode(String isoCode);
    List<GeotargetType> getGeotargetTypesForCountryIsoCode(String isoCode, Sorting sort);
    List<GeotargetType> getGeotargetTypesForCountryIsoCode(String isoCode, Pagination page);

    public List<GeotargetList> getGeotargetListByGeotargetBy(long id, FetchStrategy fetchStrategy);
    
    //------------------------------------------------------------------------------------------
    // GeotargetType yes we have a db entity GeotargetType and an enum Geotarget.Type. 
    // Ask The Architect
    //------------------------------------------------------------------------------------------
    GeotargetType getGeotargetTypeById(Long id, FetchStrategy... fetchStrategy);
    GeotargetType getGeotargetTypeById(String id, FetchStrategy... fetchStrategy);
    
    Long countAllGeotargetTypes();
    List<GeotargetType> getAllGeotargetTypes(FetchStrategy... fetchStrategy);
    List<GeotargetType> getAllGeotargetTypes(Sorting sort, FetchStrategy... fetchStrategy);
    List<GeotargetType> getAllGeotargetTypes(Pagination page, FetchStrategy... fetchStrategy);
    
    GeotargetType getGeotargetTypeByNameAndType(String name, String type);
    
    Long countAllGeotargetTypesForCountryIsoCode(String isoCode);
    List<GeotargetType> getAllGeotargetTypesForCountryIsoCode(String isoCode, FetchStrategy... fetchStrategy);
    List<GeotargetType> getAllGeotargetTypesForCountryIsoCode(String isoCode, Sorting sort, FetchStrategy... fetchStrategy);
    List<GeotargetType> getAllGeotargetTypesForCountryIsoCode(String isoCode, Pagination page, FetchStrategy... fetchStrategy);

    Long countAllGeotargetTypesForCountryIsoCode(String isoCode, Boolean isRadiusType);
    List<GeotargetType> getAllGeotargetTypesForCountryIsoCode(String isoCode, Boolean isRadiusType, FetchStrategy... fetchStrategy);
    List<GeotargetType> getAllGeotargetTypesForCountryIsoCode(String isoCode, Boolean isRadiusType, Sorting sort, FetchStrategy... fetchStrategy);
    List<GeotargetType> getAllGeotargetTypesForCountryIsoCode(String isoCode, Boolean isRadiusType, Pagination page, FetchStrategy... fetchStrategy);

}
