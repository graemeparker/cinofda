package com.byyd.middleware.auditlog.config;

import java.util.List;
import java.util.Map;

public class AuditLogConfig {

    private Map<String, AuditLogEntityConfig> auditedEntities;

    public Map<String, AuditLogEntityConfig> getAuditedEntities() {
        return auditedEntities;
    }

    public void setAuditedEntities(
            Map<String, AuditLogEntityConfig> auditedEntities) {
        this.auditedEntities = auditedEntities;
    }
    
    public AuditLogEntityConfig getAuditLogEntityConfig(String entityName){
        return auditedEntities.get(entityName);
    }
    
    public List<AuditLogPropertyConfig> getAuditLogPropertyConfig(String entityName){
        List<AuditLogPropertyConfig> auditLogPropertyConfig = null;
        
        AuditLogEntityConfig auditLogEntityConfig = auditedEntities.get(entityName);
        if (auditLogEntityConfig != null){
            auditLogPropertyConfig = auditLogEntityConfig.getAuditedProperties();
        }
        
        return auditLogPropertyConfig;
    }
}
