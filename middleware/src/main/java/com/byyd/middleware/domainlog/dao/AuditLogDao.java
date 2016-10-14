package com.byyd.middleware.domainlog.dao;

import java.util.List;

import com.adfonic.domain.AuditLog;
import com.byyd.middleware.domainlog.filter.AuditLogFilter;
import com.byyd.middleware.iface.dao.BusinessKeyDao;
import com.byyd.middleware.iface.dao.FetchStrategy;

public interface AuditLogDao extends BusinessKeyDao<AuditLog> {
    Long countAll(AuditLogFilter filter);
    List<AuditLog> getAll(AuditLogFilter filter, FetchStrategy ... fetchStrategy);
}
