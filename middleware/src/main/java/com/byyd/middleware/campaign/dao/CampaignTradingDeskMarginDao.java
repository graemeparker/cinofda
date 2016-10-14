package com.byyd.middleware.campaign.dao;

import java.util.List;

import com.adfonic.domain.Campaign;
import com.adfonic.domain.CampaignTradingDeskMargin;
import com.byyd.middleware.iface.dao.BusinessKeyDao;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.Pagination;
import com.byyd.middleware.iface.dao.Sorting;

public interface CampaignTradingDeskMarginDao extends BusinessKeyDao<CampaignTradingDeskMargin> {
    
    Long countAllForCampaign(Campaign campaign);
    List<CampaignTradingDeskMargin> getAllForCampaign(Campaign campaign, FetchStrategy ... fetchStrategy);
    List<CampaignTradingDeskMargin> getAllForCampaign(Campaign campaign, Pagination page, FetchStrategy ... fetchStrategy);
    List<CampaignTradingDeskMargin> getAllForCampaign(Campaign campaign, Sorting sort, FetchStrategy ... fetchStrategy);

}
