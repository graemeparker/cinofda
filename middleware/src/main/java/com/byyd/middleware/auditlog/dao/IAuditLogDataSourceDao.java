package com.byyd.middleware.auditlog.dao;

import java.util.List;

import com.adfonic.domain.BusinessKey;
import com.byyd.middleware.auditlog.filter.AbstractAuditLogFilter;
import com.byyd.middleware.iface.dao.BusinessKeyDao;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.Pagination;
import com.byyd.middleware.iface.dao.Sorting;

/**
 * Base DAO interface to extend when creation DAOs which uses audit database.
 */
public interface IAuditLogDataSourceDao<T extends BusinessKey, E extends AbstractAuditLogFilter> extends BusinessKeyDao<T> {

    Long countAll(E filter);
    List<T> getAll(E filter, FetchStrategy ... fetchStrategy);
    List<T> getAll(E filter, Pagination page, FetchStrategy ... fetchStrategy);
    List<T> getAll(E filter, Sorting sort, FetchStrategy ... fetchStrategy);
}
