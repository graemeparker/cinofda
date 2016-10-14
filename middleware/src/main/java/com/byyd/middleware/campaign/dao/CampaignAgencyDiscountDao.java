package com.byyd.middleware.campaign.dao;

import java.util.List;

import com.adfonic.domain.Campaign;
import com.adfonic.domain.CampaignAgencyDiscount;
import com.byyd.middleware.iface.dao.BusinessKeyDao;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.Pagination;
import com.byyd.middleware.iface.dao.Sorting;

public interface CampaignAgencyDiscountDao extends BusinessKeyDao<CampaignAgencyDiscount> {

    Long countAllForCampaign(Campaign campaign);
    List<CampaignAgencyDiscount> getAllForCampaign(Campaign campaign, FetchStrategy ... fetchStrategy);
    List<CampaignAgencyDiscount> getAllForCampaign(Campaign campaign, Pagination page, FetchStrategy ... fetchStrategy);
    List<CampaignAgencyDiscount> getAllForCampaign(Campaign campaign, Sorting sort, FetchStrategy ... fetchStrategy);
    
}
