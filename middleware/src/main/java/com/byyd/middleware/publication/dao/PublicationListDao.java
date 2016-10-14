package com.byyd.middleware.publication.dao;

import java.util.List;

import com.adfonic.domain.PublicationList;
import com.byyd.middleware.iface.dao.BusinessKeyDao;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.Pagination;
import com.byyd.middleware.iface.dao.Sorting;
import com.byyd.middleware.publication.filter.PublicationListFilter;

public interface PublicationListDao extends BusinessKeyDao<PublicationList> {

    Long countAll(PublicationListFilter filter);
    List<PublicationList> getAll(PublicationListFilter filter, FetchStrategy ... fetchStrategy);
    List<PublicationList> getAll(PublicationListFilter filter, Sorting sort, FetchStrategy ... fetchStrategy);
    List<PublicationList> getAll(PublicationListFilter filter, Pagination page, FetchStrategy ... fetchStrategy);

}
