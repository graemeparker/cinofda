package com.byyd.middleware.common.dao;

import java.util.List;

import com.adfonic.domain.ThirdPartyVendorType;
import com.byyd.middleware.common.filter.ThirdPartyVendorTypeFilter;
import com.byyd.middleware.iface.dao.BusinessKeyDao;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.Pagination;
import com.byyd.middleware.iface.dao.Sorting;

public interface ThirdPartyVendorTypeDao extends BusinessKeyDao<ThirdPartyVendorType> {

	List<ThirdPartyVendorType> getAll(ThirdPartyVendorTypeFilter filter, FetchStrategy... fetchStrategy);

	List<ThirdPartyVendorType> getAll(ThirdPartyVendorTypeFilter filter, Sorting sort, FetchStrategy... fetchStrategy);

	List<ThirdPartyVendorType> getAll(ThirdPartyVendorTypeFilter filter, Pagination page, FetchStrategy... fetchStrategy);

	List<ThirdPartyVendorType> getAll(ThirdPartyVendorTypeFilter filter, Pagination page, Sorting sort,	FetchStrategy... fetchStrategy);

}
