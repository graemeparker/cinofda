package com.byyd.middleware.account.dao;

import java.util.List;

import com.adfonic.domain.IpAddressRange;
import com.byyd.middleware.account.filter.IpAddressRangeFilter;
import com.byyd.middleware.iface.dao.BusinessKeyDao;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.Pagination;
import com.byyd.middleware.iface.dao.Sorting;

public interface IpAddressRangeDao extends BusinessKeyDao<IpAddressRange> {

    List<IpAddressRange> getAll(IpAddressRangeFilter filter, Pagination page, Sorting sort, FetchStrategy... fetchStrategy);

}
