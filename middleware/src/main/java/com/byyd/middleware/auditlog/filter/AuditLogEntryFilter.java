package com.byyd.middleware.auditlog.filter;

import java.util.Collection;

import com.adfonic.domain.auditlog.AuditLogEntity;
import com.adfonic.domain.auditlog.AuditLogEntry.AuditLogEntryType;

public class AuditLogEntryFilter implements AbstractAuditLogFilter {
    
    private Collection<AuditLogEntity> auditEntities;
    private Collection<AuditLogEntryType> auditLogEntryTypes;
    
    public Collection<AuditLogEntity> getAuditEntities() {
        return auditEntities;
    }
    public void setAuditEntities(Collection<AuditLogEntity> auditEntities) {
        this.auditEntities = auditEntities;
    }
    public Collection<AuditLogEntryType> getAuditLogEntryTypes() {
        return auditLogEntryTypes;
    }
    public void setAuditLogEntryTypes(
            Collection<AuditLogEntryType> auditLogEntryTypes) {
        this.auditLogEntryTypes = auditLogEntryTypes;
    }
}
