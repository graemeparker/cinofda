package com.byyd.middleware.campaign.dao;

import java.math.BigDecimal;
import java.util.List;

import com.adfonic.domain.BidType;
import com.adfonic.domain.Campaign;
import com.adfonic.domain.CampaignBid;
import com.byyd.middleware.iface.dao.BusinessKeyDao;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.Pagination;
import com.byyd.middleware.iface.dao.Sorting;

public interface CampaignBidDao extends BusinessKeyDao<CampaignBid> {

    List<BigDecimal> getReferenceBids(Campaign campaign, BidType bidType);
    
    Long countAllForCampaign(Campaign campaign);
    List<CampaignBid> getAllForCampaign(Campaign campaign, FetchStrategy ... fetchStrategy);
    List<CampaignBid> getAllForCampaign(Campaign campaign, Pagination page, FetchStrategy ... fetchStrategy);
    List<CampaignBid> getAllForCampaign(Campaign campaign, Sorting sort, FetchStrategy ... fetchStrategy);
}
