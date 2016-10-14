package com.byyd.middleware.campaign.dao;

import java.util.List;

import com.adfonic.domain.CampaignDataFee;
import com.byyd.middleware.campaign.filter.CampaignDataFeeFilter;
import com.byyd.middleware.iface.dao.BusinessKeyDao;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.Pagination;
import com.byyd.middleware.iface.dao.Sorting;

public interface CampaignDataFeeDao extends BusinessKeyDao<CampaignDataFee> {
    
    Long countAll(CampaignDataFeeFilter filter);
    List<CampaignDataFee> getAll(CampaignDataFeeFilter filter, FetchStrategy ... fetchStrategy);
    List<CampaignDataFee> getAll(CampaignDataFeeFilter filter, Pagination page, FetchStrategy ... fetchStrategy);
    List<CampaignDataFee> getAll(CampaignDataFeeFilter filter, Sorting sort, FetchStrategy ... fetchStrategy);
}
