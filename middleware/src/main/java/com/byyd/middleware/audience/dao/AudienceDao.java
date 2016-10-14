package com.byyd.middleware.audience.dao;

import java.util.List;

import com.adfonic.domain.Audience;
import com.byyd.middleware.audience.filter.AudienceFilter;
import com.byyd.middleware.iface.dao.BusinessKeyDao;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.Pagination;
import com.byyd.middleware.iface.dao.Sorting;

public interface AudienceDao extends BusinessKeyDao<Audience> {

    Long countAll(AudienceFilter filter);
    List<Audience> getAll(AudienceFilter filter, FetchStrategy ... fetchStrategy);
    List<Audience> getAll(AudienceFilter filter, Pagination page, FetchStrategy ... fetchStrategy);
    List<Audience> getAll(AudienceFilter filter, Sorting sort, FetchStrategy ... fetchStrategy);
}
