package com.byyd.middleware.common.dao;

import java.util.List;

import com.adfonic.domain.ThirdPartyVendor;
import com.byyd.middleware.common.filter.ThirdPartyVendorFilter;
import com.byyd.middleware.iface.dao.BusinessKeyDao;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.Pagination;
import com.byyd.middleware.iface.dao.Sorting;

public interface ThirdPartyVendorDao extends BusinessKeyDao<ThirdPartyVendor> {

	List<ThirdPartyVendor> getAll(ThirdPartyVendorFilter filter, FetchStrategy... fetchStrategy);

	List<ThirdPartyVendor> getAll(ThirdPartyVendorFilter filter, Sorting sort, FetchStrategy... fetchStrategy);

	List<ThirdPartyVendor> getAll(ThirdPartyVendorFilter filter, Pagination page, FetchStrategy... fetchStrategy);

	List<ThirdPartyVendor> getAll(ThirdPartyVendorFilter filter, Pagination page, Sorting sort,	FetchStrategy... fetchStrategy);

}
