package com.adfonic.dto.auditlog;

import com.adfonic.domain.auditlog.AuditLogEntry.AuditLogEntryType;

public class AuditLogDto extends AuditLogEntityDto implements Comparable<AuditLogDto> {

    private static final long serialVersionUID = 1L;

    // AuditLogEntity DTO fields are inherited

    // AuditLogEntry DTO fields
    private String oldValue;
    private String newValue;
    private String name;
    private AuditLogEntryType auditLogEntryType;

    public AuditLogDto(AuditLogEntityDto entity, AuditLogEntryDto entry) {
        setTimestamp(entity.getTimestamp());
        setEntityName(entity.getEntityName());
        setEntityId(entity.getEntityId());
        setAuditOperation(entity.getAuditOperation());
        setSource(entity.getSource());
        setUserType(entity.getUserType());
        setUserId(entity.getUserId());
        setUserName(entity.getUserName());
        setUserEmail(entity.getUserEmail());
        setTransactionId(entity.getTransactionId());
        setAdmin(entity.isAdmin());

        this.oldValue = entry.getOldValue();
        this.newValue = entry.getNewValue();
        this.name = entry.getName();
        this.auditLogEntryType = entry.getAuditLogEntryType();

        // set entry id as an id
        setId(entry.getId());
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public AuditLogEntryType getAuditLogEntryType() {
        return auditLogEntryType;
    }

    public void setAuditLogEntryType(AuditLogEntryType auditLogEntryType) {
        this.auditLogEntryType = auditLogEntryType;
    }

    // Specific getters for value transformation
    public AuditLogDto getAuditLogDtoForOldValue() {
        return this;
    }

    public AuditLogDto getAuditLogDtoForNewValue() {
        return this;
    }

    public AuditLogDto getAuditLogDtoForUserName() {
        return this;
    }

    public AuditLogDto getAuditLogDtoForUserEmail() {
        return this;
    }

    public AuditLogDto getAuditLogDtoForFieldName() {
        return this;
    }

    public AuditLogDto getAuditLogDtoForFieldToolTip() {
        return this;
    }

    /**
     * Order by entryId DESC
     */
    @Override
    public int compareTo(AuditLogDto other) {
        return this.getId().compareTo(other.getId()) * -1;
    }
}
