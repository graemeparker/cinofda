package com.byyd.middleware.auditlog.service;

import java.util.List;

import com.adfonic.domain.auditlog.AuditLogEntity;
import com.adfonic.domain.auditlog.AuditLogEntry;
import com.byyd.middleware.auditlog.filter.AuditLogEntityFilter;
import com.byyd.middleware.auditlog.filter.AuditLogEntryFilter;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.Pagination;
import com.byyd.middleware.iface.dao.Sorting;
import com.byyd.middleware.iface.service.BaseManager;

public interface AuditLogManager extends BaseManager {
    
    //------ Business logic method
    AuditLogEntry log(AuditLogEntity auditLogEntity, AuditLogEntry auditLogEntry);
    
    //------ AuditLogEntity entity methods
    AuditLogEntity create(AuditLogEntity auditLogEntity);
    Long countAll(AuditLogEntityFilter filter);
    List<AuditLogEntity> getAll(AuditLogEntityFilter filter, FetchStrategy ... fetchStrategy);
    List<AuditLogEntity> getAll(AuditLogEntityFilter filter, Pagination page, FetchStrategy ... fetchStrategy);
    List<AuditLogEntity> getAll(AuditLogEntityFilter filter, Sorting sort, FetchStrategy ... fetchStrategy);
    
    //------ AuditLogEntry entity methods
    AuditLogEntry create(AuditLogEntry auditLogEntry);
    Long countAll(AuditLogEntryFilter filter);
    List<AuditLogEntry> getAll(AuditLogEntryFilter filter, FetchStrategy ... fetchStrategy);
    List<AuditLogEntry> getAll(AuditLogEntryFilter filter, Pagination page, FetchStrategy ... fetchStrategy);
    List<AuditLogEntry> getAll(AuditLogEntryFilter filter, Sorting sort, FetchStrategy ... fetchStrategy);
}
