package com.byyd.middleware.campaign.dao;

import java.math.BigDecimal;
import java.util.List;

import com.adfonic.domain.Advertiser;
import com.adfonic.domain.Audience;
import com.adfonic.domain.Campaign;
import com.adfonic.domain.Publisher;
import com.byyd.middleware.campaign.filter.CampaignFilter;
import com.byyd.middleware.iface.dao.BusinessKeyDao;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.Pagination;
import com.byyd.middleware.iface.dao.Sorting;

public interface CampaignDao extends BusinessKeyDao<Campaign>  {

    Long countAll(CampaignFilter filter);
    List<Campaign> getAll(CampaignFilter filter, FetchStrategy... fetchStrategy);
    List<Campaign> getAll(CampaignFilter filter, Sorting sort, FetchStrategy... fetchStrategy);
    List<Campaign> getAll(CampaignFilter filter, Pagination page, FetchStrategy... fetchStrategy);
    List<Campaign> getAll(CampaignFilter filter, Pagination page, Sorting sort, FetchStrategy... fetchStrategy);

    List<Campaign> getAllUsingTwoPhaseLoad(CampaignFilter filter, FetchStrategy... fetchStrategy);
    List<Campaign> getAllUsingTwoPhaseLoad(CampaignFilter filter, Sorting sort, FetchStrategy... fetchStrategy);
    List<Campaign> getAllUsingTwoPhaseLoad(CampaignFilter filter, Pagination page, FetchStrategy... fetchStrategy);

    Long getHouseAdCountForPublisher(Publisher publisher);

    Long countCampaignWithNameForAdvertiser(String name, Advertiser advertiser, Campaign excludeCampaign);
    List<Campaign> getCampaignWithNameForAdvertiser(String name, Advertiser advertiser, Campaign excludeCampaign, FetchStrategy... fetchStrategy);
    List<Campaign> getCampaignWithNameForAdvertiser(String name, Advertiser advertiser, Campaign excludeCampaign, Sorting sort, FetchStrategy... fetchStrategy);
    List<Campaign> getCampaignWithNameForAdvertiser(String name, Advertiser advertiser, Campaign excludeCampaign, Pagination page, FetchStrategy... fetchStrategy);

    BigDecimal getTotalDailySpendForCampaign(Campaign campaign, int fromTimeId, int toTimeId);
    
    Long countCampaignsLinkedToAudience(Audience audience);
    List<Campaign> getCampaignsLinkedToAudience(Audience audience, FetchStrategy... fetchStrategy);
    List<Campaign> getCampaignsLinkedToAudience(Audience audience, Sorting sort, FetchStrategy... fetchStrategy);
    List<Campaign> getCampaignsLinkedToAudience(Audience audience, Pagination page, FetchStrategy... fetchStrategy);
    
    void deleteDailyAndOverallSpend(Campaign campaign);
}
