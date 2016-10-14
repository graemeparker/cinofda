package com.adfonic.dto.auditlog;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.jdto.annotation.DTOCascade;
import org.jdto.annotation.DTOTransient;
import org.jdto.annotation.Source;

import com.adfonic.domain.auditlog.AuditLogEntity.AuditOperation;
import com.adfonic.domain.auditlog.AuditLogEntity.UserType;
import com.adfonic.dto.BusinessKeyDTO;

public class AuditLogEntityDto extends BusinessKeyDTO {

    private static final long serialVersionUID = 1L;

    @Source(value = "timestamp")
    private Date timestamp;

    @Source(value = "entityName")
    private String entityName;

    @Source(value = "entityId")
    private long entityId;

    @Source(value = "auditOperation")
    private AuditOperation auditOperation;

    @Source(value = "source")
    private String source;

    @Source(value = "userType")
    private UserType userType;

    @Source(value = "userId")
    private long userId;

    @Source(value = "userName")
    private String userName;

    @Source(value = "userEmail")
    private String userEmail;

    @Source(value = "transactionId")
    private String transactionId;

    @DTOCascade
    @Source(value = "auditLogEntries")
    private List<AuditLogEntryDto> auditLogEntries = new ArrayList<AuditLogEntryDto>();

    @DTOTransient
    private boolean admin;

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp =  (timestamp == null? null : new Date(timestamp.getTime()));
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

    public AuditOperation getAuditOperation() {
        return auditOperation;
    }

    public void setAuditOperation(AuditOperation auditOperation) {
        this.auditOperation = auditOperation;
    }

    public void setEntityId(long entityId) {
        this.entityId = entityId;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public UserType getUserType() {
        return userType;
    }

    public void setUserType(UserType userType) {
        this.userType = userType;
        if (UserType.ADFONIC_USER.equals(this.userType)) {
            admin = true;
        } else {
            admin = false;
        }
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public List<AuditLogEntryDto> getAuditLogEntries() {
        return auditLogEntries;
    }

    public void setAuditLogEntries(List<AuditLogEntryDto> auditLogEntries) {
        this.auditLogEntries = auditLogEntries;
    }

    public boolean isAdmin() {
        return admin;
    }

    public void setAdmin(boolean admin) {
        this.admin = admin;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[ID=").append(id);
        sb.append(", TIMESTAMP=").append(timestamp);
        sb.append(", ENTITY_NAME=").append(entityName);
        sb.append(", ENTITY_ID=").append(entityId);
        sb.append(", AUDIT_OPERATION=").append(auditOperation);
        sb.append(", SOURCE=").append(source);
        sb.append(", USER_ID=").append(userId);
        sb.append(", TRANSACTION_ID=").append(transactionId);
        sb.append(", AUDIT_LOG_ENTRIES=").append(entriesToString()).append("]");
        return sb.toString();
    }

    private String entriesToString() {
        if (auditLogEntries == null) {
            return "null";
        }

        StringBuilder entriesBuilder = new StringBuilder();
        for (AuditLogEntryDto auditLogEntry : auditLogEntries) {
            entriesBuilder.append(auditLogEntry.toString()).append(", ");
        }

        return entriesBuilder.toString();
    }

}
