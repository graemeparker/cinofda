package com.byyd.middleware.audience.dao;

import java.util.List;

import com.adfonic.domain.DMPAttribute;
import com.byyd.middleware.audience.filter.DMPAttributeFilter;
import com.byyd.middleware.iface.dao.BusinessKeyDao;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.Pagination;
import com.byyd.middleware.iface.dao.Sorting;

public interface DMPAttributeDao extends BusinessKeyDao<DMPAttribute> {

    Long countAll(DMPAttributeFilter filter);
    List<DMPAttribute> getAll(DMPAttributeFilter filter, FetchStrategy ... fetchStrategy);
    List<DMPAttribute> getAll(DMPAttributeFilter filter, Pagination page, FetchStrategy ... fetchStrategy);
    List<DMPAttribute> getAll(DMPAttributeFilter filter, Sorting sort, FetchStrategy ... fetchStrategy);

}
