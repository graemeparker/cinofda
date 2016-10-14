package com.adfonic.dto.auditlog;

import org.jdto.annotation.Source;
import org.jdto.annotation.Sources;
import org.jdto.mergers.FirstObjectPropertyValueMerger;

import com.adfonic.domain.auditlog.AuditLogEntry.AuditLogEntryType;
import com.adfonic.dto.NameIdBusinessDto;

public class AuditLogEntryDto extends NameIdBusinessDto {

    private static final long serialVersionUID = 1L;

    @Source(value = "auditLogEntryType")
    private AuditLogEntryType auditLogEntryType;

    /** Only one field might contain not null value */
    @Sources(value = { @Source(value = "oldValueInt"), @Source(value = "oldValueDecimal"), @Source(value = "oldValueDate"), @Source(value = "oldValueVarchar"),
            @Source(value = "oldValueBoolean"), @Source(value = "oldValueBlob") }, merger = FirstObjectPropertyValueMerger.class)
    private String oldValue;

    /** Only one field might contain not null value */
    @Sources(value = { @Source(value = "newValueInt"), @Source(value = "newValueDecimal"), @Source(value = "newValueDate"), @Source(value = "newValueVarchar"),
            @Source(value = "newValueBoolean"), @Source(value = "newValueBlob") }, merger = FirstObjectPropertyValueMerger.class)
    private String newValue;

    public AuditLogEntryType getAuditLogEntryType() {
        return auditLogEntryType;
    }

    public void setAuditLogEntryType(AuditLogEntryType auditLogEntryType) {
        this.auditLogEntryType = auditLogEntryType;
    }

    public String getOldValue() {
        return oldValue;
    }

    public void setOldValue(String oldValue) {
        this.oldValue = oldValue;
    }

    public String getNewValue() {
        return newValue;
    }

    public void setNewValue(String newValue) {
        this.newValue = newValue;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[ID=").append(id);
        sb.append(", NAME=").append(name);
        if (auditLogEntryType == null) {
            sb.append(", AUDIT_ENTRY_TYPE=NULL");
        } else {
            sb.append(", AUDIT_ENTRY_TYPE=").append(auditLogEntryType);
            sb.append(", OLD_VALUE=").append(oldValue);
            sb.append(", NEW_VALUE=").append(newValue);
        }
        sb.append("]");
        return sb.toString();
    }
}
