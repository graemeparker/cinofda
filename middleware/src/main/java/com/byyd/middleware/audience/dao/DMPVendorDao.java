package com.byyd.middleware.audience.dao;

import java.util.List;

import com.adfonic.domain.DMPVendor;
import com.byyd.middleware.audience.filter.DMPVendorFilter;
import com.byyd.middleware.iface.dao.BusinessKeyDao;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.Pagination;
import com.byyd.middleware.iface.dao.Sorting;

public interface DMPVendorDao extends BusinessKeyDao<DMPVendor> {

    Long countAll(DMPVendorFilter filter);
    List<DMPVendor> getAll(DMPVendorFilter filter, FetchStrategy ... fetchStrategy);
    List<DMPVendor> getAll(DMPVendorFilter filter, Pagination page, FetchStrategy ... fetchStrategy);
    List<DMPVendor> getAll(DMPVendorFilter filter, Sorting sort, FetchStrategy ... fetchStrategy);
}
