package com.byyd.middleware.campaign.dao;

import java.util.List;

import com.adfonic.domain.Campaign;
import com.adfonic.domain.CampaignTimePeriod;
import com.byyd.middleware.iface.dao.BusinessKeyDao;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.Pagination;
import com.byyd.middleware.iface.dao.Sorting;

public interface CampaignTimePeriodDao extends BusinessKeyDao<CampaignTimePeriod> {

    Long countAllForCampaign(Campaign campaign);
    List<CampaignTimePeriod> getAllForCampaign(Campaign campaign, FetchStrategy... fetchStrategy);
    List<CampaignTimePeriod> getAllForCampaign(Campaign campaign, Sorting sort, FetchStrategy... fetchStrategy);
    List<CampaignTimePeriod> getAllForCampaign(Campaign campaign, Pagination page, FetchStrategy... fetchStrategy);
}
