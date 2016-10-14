package com.byyd.middleware.integrations.dao;

import java.util.List;

import com.adfonic.domain.CampaignTrigger;
import com.byyd.middleware.iface.dao.BusinessKeyDao;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.Pagination;
import com.byyd.middleware.iface.dao.Sorting;
import com.byyd.middleware.integrations.filter.CampaignTriggerFilter;

public interface CampaignTriggerDao extends BusinessKeyDao<CampaignTrigger> {
    Long countAll(CampaignTriggerFilter filter);
    List<CampaignTrigger> getAll(CampaignTriggerFilter filter, FetchStrategy ... fetchStrategy);
    List<CampaignTrigger> getAll(CampaignTriggerFilter filter, Pagination page, FetchStrategy ... fetchStrategy);
    List<CampaignTrigger> getAll(CampaignTriggerFilter filter, Sorting sort, FetchStrategy ... fetchStrategy);
}
