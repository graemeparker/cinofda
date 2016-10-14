package com.byyd.middleware.campaign.service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.adfonic.domain.AdfonicUser;
import com.adfonic.domain.Advertiser;
import com.adfonic.domain.BudgetSpend;
import com.adfonic.domain.Campaign;
import com.adfonic.domain.CampaignInternalLog;
import com.adfonic.domain.CampaignNotificationFlag;
import com.adfonic.domain.CampaignStoppage;
import com.adfonic.domain.Category;
import com.adfonic.domain.Language;
import com.adfonic.domain.NotificationFlag.Type;
import com.adfonic.domain.Publication;
import com.adfonic.domain.PublicationList;
import com.adfonic.domain.Publisher;
import com.adfonic.domain.RemovalInfo;
import com.adfonic.domain.Segment.SegmentSafetyLevel;
import com.adfonic.util.Range;
import com.byyd.middleware.campaign.filter.CampaignFilter;
import com.byyd.middleware.campaign.filter.CampaignStateSyncingFilter;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.Pagination;
import com.byyd.middleware.iface.dao.Sorting;
import com.byyd.middleware.iface.service.BaseManager;

public interface CampaignManager extends BaseManager {
    
    static final String CAMPAIGN_ACTIVATION_STATUS = "CAMPAIGN_ACTIVATION_STATUS";
    static final String CAMPAIGN_ACTIVATION_ERROR_MESSAGE = "CAMPAIGN_ACTIVATION_ERROR_MESSAGE";
    static final String CREATIVES_ACTIVATION_STATUSES = "CREATIVES_ACTIVATION_STATUSES";
    static final String CREATIVE_OLD_STATUS = "CREATIVE_OLD_STATUS";
    static final String CREATIVE_NEW_STATUS = "CREATIVE_NEW_STATUS";
    
    static final int MAX_CAMPAIGN_NAME_LEN = 32;
    static final String COPY_CAMPAIGN_PREFIX = "Copy of ";
    
    //------------------------------------------------------------------------------------------
    // Campaign
    //------------------------------------------------------------------------------------------
    Campaign newCampaign(String name,
                         Advertiser advertiser,
                         Category category,
                         Language defaultLanguage,
                         boolean disableLanguageMatch,
                         FetchStrategy... fetchStrategy);
    Campaign newCampaign(Campaign campaign, FetchStrategy... fetchStrategy);
    
    void syncStates(Campaign source, Campaign destination, CampaignStateSyncingFilter params);
    String getNewCampaignName(Campaign sourceCampaign);
    Campaign copyCampaign(Campaign sourceCampaign, FetchStrategy... fetchStrategy);
    Campaign copyCampaignWithTimePeriods(Campaign sourceCampaign, FetchStrategy... fetchStrategy);
    Campaign copyCampaign(Campaign sourceCampaign, boolean copyRemovedPublications, FetchStrategy... fetchStrategy);

    Campaign getCampaignById(String id, FetchStrategy... fetchStrategy);
    Campaign getCampaignById(Long id, FetchStrategy... fetchStrategy);
    //public Campaign create(Campaign campaign);
    Campaign update(Campaign campaign);
    void delete(Campaign campaign);
    void deleteCampaigns(List<Campaign> list);

    Campaign getCampaignByExternalId(String externalId, FetchStrategy... fetchStrategy);

    Long countAllCampaigns(CampaignFilter filter);
    List<Campaign> getAllCampaigns(CampaignFilter filter, FetchStrategy... fetchStrategy);
    List<Campaign> getAllCampaigns(CampaignFilter filter, Sorting sort, FetchStrategy... fetchStrategy);
    List<Campaign> getAllCampaigns(CampaignFilter filter, Pagination page, FetchStrategy... fetchStrategy);
    List<Campaign> getAllCampaigns(CampaignFilter filter, Pagination page, Sorting sort, FetchStrategy... fetchStrategy);

    Long countAllCampaignsForAdvertiser(Advertiser advertiser);
    List<Campaign> getAllCampaignsForAdvertiser(Advertiser advertiser, FetchStrategy... fetchStrategy);
    List<Campaign> getAllCampaignsForAdvertiser(Advertiser advertiser, Sorting sort, FetchStrategy... fetchStrategy);
    List<Campaign> getAllCampaignsForAdvertiser(Advertiser advertiser, Pagination page, FetchStrategy... fetchStrategy);

    Long countAllCampaignsForAdvertiser(Advertiser advertiser, Boolean houseAds);
    List<Campaign> getAllCampaignsForAdvertiser(Advertiser advertiser, Boolean houseAds, FetchStrategy... fetchStrategy);
    List<Campaign> getAllCampaignsForAdvertiser(Advertiser advertiser, Boolean houseAds, Sorting sort, FetchStrategy... fetchStrategy);
    List<Campaign> getAllCampaignsForAdvertiser(Advertiser advertiser, Boolean houseAds, Pagination page, FetchStrategy... fetchStrategy);

    List<Campaign> getAllCampaignsUsingTwoPhaseLoad(CampaignFilter filter, FetchStrategy... fetchStrategy);
    List<Campaign> getAllCampaignsUsingTwoPhaseLoad(CampaignFilter filter, Sorting sort, FetchStrategy... fetchStrategy);
    List<Campaign> getAllCampaignsUsingTwoPhaseLoad(CampaignFilter filter, Pagination page, FetchStrategy... fetchStrategy);

    Long countAllCampaignsThatHaveEverBeenActiveForAdvertiser(Advertiser advertiser);
    List<Campaign> getAllCampaignsThatHaveEverBeenActiveForAdvertiser(Advertiser advertiser, FetchStrategy... fetchStrategy);
    List<Campaign> getAllCampaignsThatHaveEverBeenActiveForAdvertiser(Advertiser advertiser, Sorting sort, FetchStrategy... fetchStrategy);
    List<Campaign> getAllCampaignsThatHaveEverBeenActiveForAdvertiser(Advertiser advertiser, Pagination page, FetchStrategy... fetchStrategy);
    
    Long countAllCampaignsThatHaveEverBeenActiveForAdvertiser(Advertiser advertiser, String containsName, boolean nameWithPreviousSpace);
    List<Campaign> getAllCampaignsThatHaveEverBeenActiveForAdvertiser(Advertiser advertiser, String containsName, boolean nameWithPreviousSpace, FetchStrategy... fetchStrategy);
    List<Campaign> getAllCampaignsThatHaveEverBeenActiveForAdvertiser(Advertiser advertiser, String containsName, boolean nameWithPreviousSpace, Sorting sort, FetchStrategy... fetchStrategy);
    List<Campaign> getAllCampaignsThatHaveEverBeenActiveForAdvertiser(Advertiser advertiser, String containsName, boolean nameWithPreviousSpace, Pagination page, FetchStrategy... fetchStrategy);
   

    // In these, for houseAds:
    // - if null, this aspect is ignore
    // - if true, it will return the entries where house_ads is true
    // - if false, it will return the entries where house_ads is false
    Long countAllCampaignsThatHaveEverBeenActiveForAdvertiser(Advertiser advertiser, Boolean houseAds);
    List<Campaign> getAllCampaignsThatHaveEverBeenActiveForAdvertiser(Advertiser advertiser, Boolean houseAds, FetchStrategy... fetchStrategy);
    List<Campaign> getAllCampaignsThatHaveEverBeenActiveForAdvertiser(Advertiser advertiser, Boolean houseAds, Sorting sort, FetchStrategy... fetchStrategy);
    List<Campaign> getAllCampaignsThatHaveEverBeenActiveForAdvertiser(Advertiser advertiser, Boolean houseAds, Pagination page, FetchStrategy... fetchStrategy);
    List<Campaign> getAllCampaignsActivePausedForAdvertiser(Advertiser advertiser, String containsName, FetchStrategy... fetchStrategy);

    BigDecimal getBudgetSpendAmountForCampaign(Campaign campaign, Date date);
    BudgetSpend getBudgetSpendForCampaign(Campaign campaign, Date date);

    Long getHouseAdCountForPublisher(Publisher publisher);

    Long countCampaignWithNameForAdvertiser(String name, Advertiser advertiser, Campaign excludeCampaign);
    List<Campaign> getCampaignWithNameForAdvertiser(String name, Advertiser advertiser, Campaign excludeCampaign, FetchStrategy... fetchStrategy);
    List<Campaign> getCampaignWithNameForAdvertiser(String name, Advertiser advertiser, Campaign excludeCampaign, Sorting sort, FetchStrategy... fetchStrategy);
    List<Campaign> getCampaignWithNameForAdvertiser(String name, Advertiser advertiser, Campaign excludeCampaign, Pagination page, FetchStrategy... fetchStrategy);

    boolean isCampaignNameUnique(String name, Advertiser advertiser, Campaign excludeCampaign);

    Long countCampaignsForAdvertiserAndDateRangeAndStatuses(Advertiser advertiser, Range<Date> dateRangeForActive, Boolean houseAds, List<Campaign.Status> statuses);
    List<Campaign> getCampaignsForAdvertiserAndDateRangeAndStatuses(Advertiser advertiser, Range<Date> dateRangeForActive, Boolean houseAds, List<Campaign.Status> statuses, FetchStrategy... fetchStrategy);
    List<Campaign> getCampaignsForAdvertiserAndDateRangeAndStatuses(Advertiser advertiser, Range<Date> dateRangeForActive, Boolean houseAds, List<Campaign.Status> statuses, Sorting sort, FetchStrategy... fetchStrategy);
    List<Campaign> getCampaignsForAdvertiserAndDateRangeAndStatuses(Advertiser advertiser, Range<Date> dateRangeForActive, Boolean houseAds, List<Campaign.Status> statuses, Pagination page, FetchStrategy... fetchStrategy);

    Long countActiveCampaignsForPeriod(Advertiser advertiser, Range<Date> dateRangeForActive, Boolean houseAds);
    List<Campaign> getActiveCampaignsForPeriod(Advertiser advertiser, Range<Date> dateRangeForActive, Boolean houseAds, FetchStrategy... fetchStrategy);
    List<Campaign> getActiveCampaignsForPeriod(Advertiser advertiser, Range<Date> dateRangeForActive, Boolean houseAds, Sorting sort, FetchStrategy... fetchStrategy);
    List<Campaign> getActiveCampaignsForPeriod(Advertiser advertiser, Range<Date> dateRangeForActive, Boolean houseAds, Pagination page, FetchStrategy... fetchStrategy);

    boolean isCampaignCurrentlyActive(Campaign c);
    boolean isCampaignCurrentlyScheduled(Campaign c);
    
    boolean campaignHasDailySpend(Campaign campaign, Date fromDate, Date toDate);
    boolean campaignHasDailySpend(Campaign campaign, int fromTimeId, int toTimeId);
    
    Long countCampaignsForPublicationList(PublicationList publicationList, List<Campaign.Status> statuses);
    List<Campaign> getCampaignsForPublicationList(PublicationList publicationList, List<Campaign.Status> statuses, FetchStrategy... fetchStrategy);
    List<Campaign> getCampaignsForPublicationList(PublicationList publicationList, List<Campaign.Status> statuses, Sorting sort, FetchStrategy... fetchStrategy);
    List<Campaign> getCampaignsForPublicationList(PublicationList publicationList, List<Campaign.Status> statuses, Pagination page, FetchStrategy... fetchStrategy);
    
    Long countCampaignsForPublicationList(PublicationList publicationList);
    List<Campaign> getCampaignsForPublicationList(PublicationList publicationList, FetchStrategy... fetchStrategy);
    List<Campaign> getCampaignsForPublicationList(PublicationList publicationList, Sorting sort, FetchStrategy... fetchStrategy);
    List<Campaign> getCampaignsForPublicationList(PublicationList publicationList, Pagination page, FetchStrategy... fetchStrategy);
    
    Map<String, Object> adOpsActivateNewCampaign(
            Campaign campaign, 
            String advertiserDomain,
            SegmentSafetyLevel safetyLevel,
            Category campaignIabCategory,
            Set<Category> blackListedPublicationCategories,
            AdfonicUser adfonicUser);

    void adOpsUpdateExistingCampaign(
            Campaign campaign, 
            String advertiserDomain,
            SegmentSafetyLevel safetyLevel,
            Category campaignIabCategory,
            Set<Category> blackListedPublicationCategories,
            boolean approveAllNewCreatives,
            AdfonicUser adfonicUser);
    
    Campaign removePublicationFromCampaign(Campaign campaign, Publication publication, RemovalInfo.RemovalType removalType);
    Campaign unremovePublicationFromCampaign(Campaign campaign, Publication publication);
    
    //------------------------------------------------------------------------------------------
    // CampaignStoppage
    //------------------------------------------------------------------------------------------
    CampaignStoppage newCampaignStoppage(Campaign campaign, CampaignStoppage.Reason reason, FetchStrategy... fetchStrategy);
    CampaignStoppage newCampaignStoppage(Campaign campaign, CampaignStoppage.Reason reason, Date reactivateDate, FetchStrategy... fetchStrategy);

    CampaignStoppage getCampaignStoppageById(String id, FetchStrategy... fetchStrategy);
    CampaignStoppage getCampaignStoppageById(Long id, FetchStrategy... fetchStrategy);
    CampaignStoppage update(CampaignStoppage campaignStoppage);
    void delete(CampaignStoppage campaignStoppage);
    void deleteCampaignStoppages(List<CampaignStoppage> list);

    List<Object[]> getCampaignStoppagesFieldsForNullOrFutureReactivateDate();
    List<CampaignStoppage> getCampaignStoppagesForNullOrFutureReactivateDate(FetchStrategy... fetchStrategy);
    List<CampaignStoppage> getCampaignStoppagesForCampaignAndNullOrFutureReactivateDate(Campaign campaign, FetchStrategy... fetchStrategy);

    //------------------------------------------------------------------------------------------
    // CampaignNotificationFlag
    //------------------------------------------------------------------------------------------
    CampaignNotificationFlag newCampaignNotificationFlag(Campaign campaign, Type type, int ttlSeconds, FetchStrategy... fetchStrategy);
    CampaignNotificationFlag newCampaignNotificationFlag(Campaign campaign, Type type, Date expirationDate, FetchStrategy... fetchStrategy);
    CampaignNotificationFlag getCampaignNotificationFlagById(String id, FetchStrategy... fetchStrategy);
    CampaignNotificationFlag getCampaignNotificationFlagById(Long id, FetchStrategy... fetchStrategy);
    void delete(CampaignNotificationFlag campaignNotificationFlag);
    void deleteCampaignNotificationFlags(List<CampaignNotificationFlag> list);
    
    //------------------------------------------------------------------------------------------
    // Campaign Internal Log Level Data (LLD)
    //------------------------------------------------------------------------------------------
    boolean isCampaignInternalLLDEnabled(Long campaignId);
    CampaignInternalLog enableCampaignInternalLLD(Long campaignId);
    CampaignInternalLog disableCampaignInternalLLD(Long campaignId);

}
