package com.byyd.middleware.auditlog.filter;


public class AuditEntityInformation {
    private String entityName;
    private long entityId;
    
    public AuditEntityInformation(String entityName, long entityId) {
        super();
        this.entityName = entityName;
        this.entityId = entityId;
    }
    public String getEntityName() {
        return entityName;
    }
    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }
    public long getEntityId() {
        return entityId;
    }
    public void setEntityId(long entityId) {
        this.entityId = entityId;
    }
}
