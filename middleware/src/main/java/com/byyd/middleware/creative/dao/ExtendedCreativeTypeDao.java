package com.byyd.middleware.creative.dao;

import java.util.List;

import com.adfonic.domain.ExtendedCreativeType;
import com.byyd.middleware.creative.filter.ExtendedCreativeTypeFilter;
import com.byyd.middleware.iface.dao.BusinessKeyDao;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.Pagination;
import com.byyd.middleware.iface.dao.Sorting;

public interface ExtendedCreativeTypeDao extends BusinessKeyDao<ExtendedCreativeType> {
    
    Long countAll(ExtendedCreativeTypeFilter filter);
    List<ExtendedCreativeType> findAll(ExtendedCreativeTypeFilter filter, FetchStrategy... fetchStrategy);
    List<ExtendedCreativeType> findAll(ExtendedCreativeTypeFilter filter, Sorting sort, FetchStrategy... fetchStrategy);
    List<ExtendedCreativeType> findAll(ExtendedCreativeTypeFilter filter, Pagination page, FetchStrategy... fetchStrategy);
    List<ExtendedCreativeType> findAll(ExtendedCreativeTypeFilter filter, Pagination page, Sorting sort, FetchStrategy... fetchStrategy);
}
