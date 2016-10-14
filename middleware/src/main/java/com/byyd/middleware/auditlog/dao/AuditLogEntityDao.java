package com.byyd.middleware.auditlog.dao;

import com.adfonic.domain.auditlog.AuditLogEntity;
import com.byyd.middleware.auditlog.filter.AuditLogEntityFilter;

public interface AuditLogEntityDao<T extends AuditLogEntity, E extends AuditLogEntityFilter> extends IAuditLogDataSourceDao<AuditLogEntity, AuditLogEntityFilter> {
    
}
