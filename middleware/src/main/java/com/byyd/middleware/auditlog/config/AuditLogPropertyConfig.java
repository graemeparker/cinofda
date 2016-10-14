package com.byyd.middleware.auditlog.config;

import com.adfonic.domain.auditlog.AuditLogEntry.AuditLogEntryType;

public class AuditLogPropertyConfig {

    private static final char COLLECTION_PROPERTY_SEPARATOR = '*';
    
    private String key;
    private AuditLogEntryType type;
    private String propertyName;
    private String nestedPropertyName;
    private boolean hasNestedProperty = false;
    
    
    
    public String getKey(){
        return this.key;
    }
    public void setKey(String key) {
        this.key = key;
        int separatorIdx = key.indexOf(COLLECTION_PROPERTY_SEPARATOR);
        if (separatorIdx>=0){
            this.nestedPropertyName = key.substring(separatorIdx+1, key.length());
            this.propertyName = key.substring(0, separatorIdx);
            this.hasNestedProperty = true;
        }else{
            this.propertyName = key;
        }
    }
    
    public AuditLogEntryType getType() {
        return this.type;
    }
    public void setType(AuditLogEntryType type) {
        this.type = type;
    }
    public String getPropertyName() {
        return propertyName;
    }
    public String getNestedPropertyName() {
        return nestedPropertyName;
    }
    public boolean hasNestedProperty(){
        return this.hasNestedProperty;
    }
}
