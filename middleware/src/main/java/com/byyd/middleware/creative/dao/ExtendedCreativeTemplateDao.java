package com.byyd.middleware.creative.dao;

import java.util.List;

import com.adfonic.domain.Creative;
import com.adfonic.domain.ExtendedCreativeTemplate;
import com.byyd.middleware.iface.dao.BusinessKeyDao;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.Pagination;
import com.byyd.middleware.iface.dao.Sorting;

public interface ExtendedCreativeTemplateDao extends BusinessKeyDao<ExtendedCreativeTemplate> {

    Long countAllForCreative(Creative creative);
    List<ExtendedCreativeTemplate> getAllForCreative(Creative creative, FetchStrategy... fetchStrategy);
    List<ExtendedCreativeTemplate> getAllForCreative(Creative creative, Sorting sort, FetchStrategy... fetchStrategy);
    List<ExtendedCreativeTemplate> getAllForCreative(Creative creative, Pagination page, FetchStrategy... fetchStrategy);
}
