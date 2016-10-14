package com.byyd.middleware.creative.dao;

import java.util.List;

import com.adfonic.domain.Component;
import com.adfonic.domain.Format;
import com.byyd.middleware.iface.dao.BusinessKeyDao;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.Pagination;
import com.byyd.middleware.iface.dao.Sorting;

public interface ComponentDao extends BusinessKeyDao<Component> {

    Long countAllForFormat(Format format);
    List<Component> findAllByFormat(Format format, FetchStrategy... fetchStrategy);
    List<Component> findAllByFormat(Format format, Sorting sort, FetchStrategy... fetchStrategy);
    List<Component> findAllByFormat(Format format, Pagination page, FetchStrategy... fetchStrategy);

}
