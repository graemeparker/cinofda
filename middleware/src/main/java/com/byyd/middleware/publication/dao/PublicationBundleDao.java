package com.byyd.middleware.publication.dao;

import java.util.List;

import com.adfonic.domain.PublicationBundle;
import com.byyd.middleware.iface.dao.BusinessKeyDao;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.Pagination;
import com.byyd.middleware.publication.filter.PublicationBundleFilter;

public interface PublicationBundleDao extends BusinessKeyDao<PublicationBundle> {

    Long countAll(PublicationBundleFilter filter);
    List<PublicationBundle> getAll(PublicationBundleFilter filter, FetchStrategy ... fetchStrategy);
    List<PublicationBundle> getAll(PublicationBundleFilter filter, Pagination page, FetchStrategy ... fetchStrategy);
}
