package com.byyd.middleware.publication.dao;

import java.util.List;

import com.adfonic.domain.Publication;
import com.adfonic.domain.PublicationHistory;
import com.byyd.middleware.iface.dao.BusinessKeyDao;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.Pagination;
import com.byyd.middleware.iface.dao.Sorting;

public interface PublicationHistoryDao extends BusinessKeyDao<PublicationHistory> {
    List<PublicationHistory> getAll(Publication publication, FetchStrategy ... fetchStrategy);
    List<PublicationHistory> getAll(Publication publication, Sorting sort, FetchStrategy ... fetchStrategy);
    List<PublicationHistory> getAll(Publication publication, Pagination page, FetchStrategy ... fetchStrategy);
}
