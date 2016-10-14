package com.byyd.middleware.auditlog.dao;

import com.adfonic.domain.auditlog.AuditLogEntry;
import com.byyd.middleware.auditlog.filter.AuditLogEntryFilter;

public interface AuditLogEntryDao<T extends AuditLogEntry, E extends AuditLogEntryFilter> extends IAuditLogDataSourceDao<AuditLogEntry, AuditLogEntryFilter> {
    
}
