package com.byyd.middleware.auditlog.config;

import java.util.List;

public class AuditLogEntityConfig {

    private List<AuditLogPropertyConfig> auditedProperties;

    public List<AuditLogPropertyConfig> getAuditedProperties() {
        return auditedProperties;
    }

    public void setAuditedProperties(List<AuditLogPropertyConfig> auditedProperties) {
        this.auditedProperties = auditedProperties;
    }
}
