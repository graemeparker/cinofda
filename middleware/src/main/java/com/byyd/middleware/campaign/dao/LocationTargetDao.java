package com.byyd.middleware.campaign.dao;

import java.util.List;

import com.adfonic.domain.LocationTarget;
import com.byyd.middleware.campaign.filter.LocationTargetFilter;
import com.byyd.middleware.iface.dao.BusinessKeyDao;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.Pagination;
import com.byyd.middleware.iface.dao.Sorting;

public interface LocationTargetDao extends BusinessKeyDao<LocationTarget> {

    Long countAll(LocationTargetFilter filter);
    List<LocationTarget> getAll(LocationTargetFilter filter, FetchStrategy ... fetchStrategy);
    List<LocationTarget> getAll(LocationTargetFilter filter, Sorting sort, FetchStrategy ... fetchStrategy);
    List<LocationTarget> getAll(LocationTargetFilter filter, Pagination page, FetchStrategy ... fetchStrategy);

}
