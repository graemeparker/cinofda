package com.byyd.middleware.audience.dao;

import java.util.List;

import com.adfonic.domain.CampaignAudience;
import com.byyd.middleware.audience.filter.CampaignAudienceFilter;
import com.byyd.middleware.iface.dao.BusinessKeyDao;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.Pagination;
import com.byyd.middleware.iface.dao.Sorting;

public interface CampaignAudienceDao extends BusinessKeyDao<CampaignAudience> {

    Long countAll(CampaignAudienceFilter filter);
    List<CampaignAudience> getAll(CampaignAudienceFilter filter, FetchStrategy ... fetchStrategy);
    List<CampaignAudience> getAll(CampaignAudienceFilter filter, Pagination page, FetchStrategy ... fetchStrategy);
    List<CampaignAudience> getAll(CampaignAudienceFilter filter, Sorting sort, FetchStrategy ... fetchStrategy);
    
}
