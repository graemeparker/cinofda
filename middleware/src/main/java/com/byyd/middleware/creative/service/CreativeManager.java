package com.byyd.middleware.creative.service;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.adfonic.domain.AdfonicUser;
import com.adfonic.domain.Advertiser;
import com.adfonic.domain.BeaconUrl;
import com.adfonic.domain.Campaign;
import com.adfonic.domain.Creative;
import com.adfonic.domain.CreativeAttribute;
import com.adfonic.domain.CreativeHistory;
import com.adfonic.domain.Destination;
import com.adfonic.domain.DestinationType;
import com.adfonic.domain.Format;
import com.adfonic.domain.Publication;
import com.adfonic.domain.RemovalInfo;
import com.adfonic.domain.Segment;
import com.adfonic.domain.User;
import com.byyd.middleware.creative.filter.CreativeFilter;
import com.byyd.middleware.creative.filter.CreativeStateSyncingFilter;
import com.byyd.middleware.creative.filter.DestinationFilter;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.Pagination;
import com.byyd.middleware.iface.dao.Sorting;
import com.byyd.middleware.iface.service.BaseManager;

public interface CreativeManager extends BaseManager {
        
    static final String CREATIVE_STATUS_UPDATE_STATUS = "CREATIVE_STATUS_UPDATE_STATUS";
    static final String CREATIVE_STATUS_UPDATE_ERROR_MESSAGE = "CREATIVE_STATUS_UPDATE_ERROR_MESSAGE";
    static final String CREATIVE_STATUS_UPDATE_ADX_PROVISIONING_OUTCOME = "CREATIVE_STATUS_UPDATE_ADX_PROVISIONING_OUTCOME";
    static final String CREATIVE_PROVISIONING_STATUS = "CREATIVE_STATUS_PROVISIONING_STATUS";
    
    // statuses of AdX creatives that must be resubmitted when changes impacting provisioning
    static final List<Creative.Status> ADX_REPROVISION_CREATIVE_STATUSES = Arrays.asList(Creative.Status.ACTIVE, 
                                                                                         Creative.Status.PAUSED, 
                                                                                         Creative.Status.STOPPED);
    
    //------------------------------------------------------------------------------------------
    // Creative
    //------------------------------------------------------------------------------------------
    Creative newCreative(Campaign campaign, Segment segment, Format format, String name, FetchStrategy... fetchStrategy);
    Creative newCreative(Creative creative, FetchStrategy... fetchStrategy);

    void syncStates(Creative source, Creative destination, CreativeStateSyncingFilter params);
    Creative copyCreative(Creative creative, Campaign destinationCampaign, Segment destinationSegment, FetchStrategy... fetchStrategy);
    Creative copyCreative(Creative creative, Campaign destinationCampaign, Segment destinationSegment, boolean copyRemovedPublications, FetchStrategy... fetchStrategy);

    Creative getCreativeById(String id, FetchStrategy... fetchStrategy);
    Creative getCreativeById(Long id, FetchStrategy... fetchStrategy);
    List<Creative> getCreativesById(List<Long> ids, FetchStrategy... fetchStrategy);
    
    Creative update(Creative creative);
    void delete(Creative creative);
    void deleteCreatives(List<Creative> list);

    Creative getCreativeByExternalId(String externalId, FetchStrategy... fetchStrategy);
    Creative getCreativeByIdOrExternalId(String key, FetchStrategy... fetchStrategy);

    Long countAllCreatives(CreativeFilter filter);
    List<Creative> getAllCreatives(CreativeFilter filter, FetchStrategy... fetchStrategy);
    List<Creative> getAllCreatives(CreativeFilter filter, Sorting sort, FetchStrategy... fetchStrategy);
    List<Creative> getAllCreatives(CreativeFilter filter, Pagination page, FetchStrategy... fetchStrategy);

    Long countAllCreativesForCampaign(Campaign campaign);
    List<Creative> getAllCreativesForCampaign(Campaign campaign, FetchStrategy... fetchStrategy);
    List<Creative> getAllCreativesForCampaign(Campaign campaign, Sorting sort, FetchStrategy... fetchStrategy);
    List<Creative> getAllCreativesForCampaign(Campaign campaign, Pagination page, FetchStrategy... fetchStrategy);
    List<Creative> getAllCreativesForCampaignIds(Collection<Long> campaignIds, FetchStrategy... fetchStrategy);

    Long countPendingCreativesForPublication(Publication publication);
    List<Creative> getPendingCreativesForPublication(Publication publication, FetchStrategy... fetchStrategy);
    List<Creative> getPendingCreativesForPublication(Publication publication, Sorting sort, FetchStrategy... fetchStrategy);
    List<Creative> getPendingCreativesForPublication(Publication publication, Pagination page, FetchStrategy... fetchStrategy);

    Long countApprovedCreativesForPublication(Publication publication);
    List<Creative> getApprovedCreativesForPublication(Publication publication, FetchStrategy... fetchStrategy);
    List<Creative> getApprovedCreativesForPublication(Publication publication, Sorting sort, FetchStrategy... fetchStrategy);
    List<Creative> getApprovedCreativesForPublication(Publication publication, Pagination page, FetchStrategy... fetchStrategy);

    Long countDeniedCreativesForPublication(Publication publication);
    List<Creative> getDeniedCreativesForPublication(Publication publication, FetchStrategy... fetchStrategy);
    List<Creative> getDeniedCreativesForPublication(Publication publication, Sorting sort, FetchStrategy... fetchStrategy);
    List<Creative> getDeniedCreativesForPublication(Publication publication, Pagination page, FetchStrategy... fetchStrategy);

    Creative removePublicationFromCreative(Creative creative, Publication publication, RemovalInfo.RemovalType removalType);
    Creative removePublicationFromCreative(
            Creative creative, 
            Publication publication, 
            RemovalInfo.RemovalType removalType,
            User user,
            AdfonicUser adfonicUser);
    
    Creative unremovePublicationFromCreative(Creative creative, Publication publication);
    Creative unremovePublicationFromCreative(
            Creative creative, 
            Publication publication, 
            User user,
            AdfonicUser adfonicUser);
    
    List<Creative> getCreativesEligibleForAdXReprovisioning(Campaign campaign, FetchStrategy... fetchStrategy);
    List<Creative> updateCreativeStatusForAdXReprovisioning(List<Creative> creatives);
    List<Creative> updateCreativeStatusForAdXReprovisioning(Creative creative);
    Creative submitCreative(Creative creative);
    List<Creative> resubmitAllCreativesNeedingAdXReprovisioning(Campaign campaign);    
    
    Map<String, Object> updateCreativeStatusAsMap(Creative creative, Creative.Status status);

    Long countCreativesWithNameForCampaign(String name, Campaign campaign, Creative excludeCreative);
    List<Creative> getCreativesWithNameForCampaign(String name, Campaign campaign, Creative excludeCreative, FetchStrategy... fetchStrategy);
    List<Creative> getCreativesWithNameForCampaign(String name, Campaign campaign, Creative excludeCreative, Sorting sort, FetchStrategy... fetchStrategy);
    List<Creative> getCreativesWithNameForCampaign(String name, Campaign campaign, Creative excludeCreative, Pagination page, FetchStrategy... fetchStrategy);

    boolean isCreativeNameUnique(String name, Campaign campaign, Creative excludeCreative);

    boolean stopCreative(Creative creative);
    boolean startCreative(Creative creative);
    boolean pauseCreative(Creative creative);

    boolean stopCreatives(List<Creative> creatives);
    boolean startCreatives(List<Creative> creatives);
    boolean pauseCreatives(List<Creative> creatives);

    List<Creative> getCreativesByIdsAsList(Collection<Long> ids, FetchStrategy... fetchStrategy);
    List<Creative> getCreativesByIdsAsList(Collection<Long> ids, Sorting sort, FetchStrategy... fetchStrategy);
    List<Creative> getCreativesByIdsAsList(Collection<Long> ids, Pagination page, FetchStrategy... fetchStrategy);
    
    Map<Long, Creative> getCreativesByIdsAsMap(Collection<Long> ids, FetchStrategy... fetchStrategy);
    Map<Long, Creative> getCreativesByIdsAsMap(Collection<Long> ids, Sorting sort, FetchStrategy... fetchStrategy);
    Map<Long, Creative> getCreativesByIdsAsMap(Collection<Long> ids, Pagination page, FetchStrategy... fetchStrategy);
    
    Publication approveCreativeForPublication(Publication publication, Creative creative);
    Publication denyCreativeForPublication(Publication publication, Creative creative);

    //------------------------------------------------------------------------
    // CreativeHistory
    //------------------------------------------------------------------------
    CreativeHistory newCreativeHistory(Creative creative, FetchStrategy ... fetchStrategy);
    CreativeHistory newCreativeHistory(Creative creative, String comment, AdfonicUser adfonicUser, FetchStrategy ... fetchStrategy);

    CreativeHistory update(CreativeHistory creativeHistory);

    CreativeHistory getCreativeHistoryById(long id, FetchStrategy ... fetchStrategy);

    List<CreativeHistory> getCreativeHistory(Creative creative, FetchStrategy ... fetchStrategy);
    
    //------------------------------------------------------------------------------------------
    // Destination
    //------------------------------------------------------------------------------------------
    Destination newDestination(Advertiser advertiser, DestinationType destinationType, String urlString, FetchStrategy... fetchStrategy);
    Destination newDestination(Advertiser advertiser, DestinationType destinationType, String urlString, boolean dataIsFinalDestination, String finalDestination, FetchStrategy... fetchStrategy);
    Destination newDestination(Advertiser advertiser, DestinationType destinationType, String urlString, List<BeaconUrl> beacons, FetchStrategy... fetchStrategy);
    Destination newDestination(Advertiser advertiser, DestinationType destinationType, String urlString, List<BeaconUrl> beacons, boolean dataIsFinalDestination, String finalDestination, FetchStrategy... fetchStrategy);
    
    Destination getDestinationById(String id, FetchStrategy... fetchStrategy);
    Destination getDestinationById(Long id, FetchStrategy... fetchStrategy);
    Destination update(Destination destination);
    BeaconUrl getBeaconById(Long id, FetchStrategy... fetchStrategy);
    BeaconUrl create(BeaconUrl beaconUrl);
    void delete(Destination destination);
    void deleteDestinations(List<Destination> list);

    Long countAllDestinations(DestinationFilter filter);
    List<Destination> getAllDestinations(DestinationFilter filter, FetchStrategy... fetchStrategy);
    List<Destination> getAllDestinations(DestinationFilter filter, Sorting sort, FetchStrategy... fetchStrategy);
    List<Destination> getAllDestinations(DestinationFilter filter, Pagination page, FetchStrategy... fetchStrategy);

    Destination getDestinationForAdvertiserAndDestinationTypeAndData(
            Advertiser advertiser, 
            DestinationType destinationType, 
            String data, 
            FetchStrategy... fetchStrategy);
    Destination getDestinationForAdvertiserAndDestinationTypeAndData(
            Advertiser advertiser, 
            DestinationType destinationType, 
            String data, 
            List<BeaconUrl> beacons, 
            FetchStrategy... fetchStrategy);
    Destination getDestinationForAdvertiserAndDestinationTypeAndData(
            Advertiser advertiser, 
            DestinationType destinationType, 
            String data, 
            boolean createIfNotFound, 
            List<BeaconUrl> beacons, 
            FetchStrategy... fetchStrategy);
    Destination getDestinationForAdvertiserAndDestinationTypeAndData(
            Advertiser advertiser, 
            DestinationType destinationType, 
            String data, 
            boolean createIfNotFound, 
            List<BeaconUrl> beacons, 
            boolean dataIsFinalDestination, 
            String finalDestination,
            FetchStrategy... fetchStrategy);

    //------------------------------------------------------------------------------
    // CreativeAttribute
    //------------------------------------------------------------------------------
    CreativeAttribute newCreativeAttribute(String name,FetchStrategy... fetchStrategy);
    CreativeAttribute create(CreativeAttribute creativeAttribute);
    CreativeAttribute update(CreativeAttribute creativeAttribute);
    void delete(CreativeAttribute creativeAttribute);
    void deleteCreativeAttributes(List<CreativeAttribute> list);
    CreativeAttribute getCreativeAttributeById(String id, FetchStrategy... fetchStrategy);
    CreativeAttribute getCreativeAttributeById(Long id, FetchStrategy... fetchStrategy);
    
    CreativeAttribute getCreativeAttributeByName(String name, FetchStrategy... fetchStrategy);
    
    Long countAllCreativeAttributes();
    List<CreativeAttribute> getAllCreativeAttributes(FetchStrategy... fetchStrategy);
    List<CreativeAttribute> getAllCreativeAttributes(Sorting sort, FetchStrategy... fetchStrategy);
    List<CreativeAttribute> getAllCreativeAttributes(Pagination page, FetchStrategy... fetchStrategy);

}
