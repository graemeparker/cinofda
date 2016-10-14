package com.byyd.middleware.domainlog.service;

import java.util.List;

import com.adfonic.domain.AuditLog;
import com.byyd.middleware.domainlog.filter.AuditLogFilter;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.service.BaseManager;

public interface AuditLogManager extends BaseManager {
    AuditLog create(AuditLog auditLog);
    
    Long countAll(AuditLogFilter filter);
    List<AuditLog> getAll(AuditLogFilter filter, FetchStrategy ... fetchStrategy);
}
