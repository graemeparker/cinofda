package com.byyd.middleware.auditlog.filter;

import java.util.Collection;
import java.util.Date;

import com.adfonic.domain.auditlog.AuditLogEntity.UserType;

public class AuditLogEntityFilter implements AbstractAuditLogFilter {
    
    private Collection<AuditEntityInformation> auditEntitiesInformation;
    private String source;
    private UserType userType;
    private Long userId;
    private String userName;
    private String userEmail;
    private String transactionId;
    private Date fromDate;
    private Date toDate;
    
    public Collection<AuditEntityInformation> getAuditEntitiesInformation() {
        return auditEntitiesInformation;
    }
    public void setAuditEntitiesInformation(
            Collection<AuditEntityInformation> auditEntitiesInformation) {
        this.auditEntitiesInformation = auditEntitiesInformation;
    }
    public String getSource() {
        return source;
    }
    public AuditLogEntityFilter setSource(String source) {
        this.source = source;
        return this;
    }
    public UserType getUserType() {
        return userType;
    }
    public void setUserType(UserType userType) {
        this.userType = userType;
    }
    public Long getUserId() {
        return userId;
    }
    public void setUserId(Long userId) {
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
    public Date getFromDate() {
        return fromDate;
    }
    public AuditLogEntityFilter setFromDate(Date fromDate) {
        this.fromDate = (fromDate==null?null:new Date(fromDate.getTime()));
        return this;
    }
    public Date getToDate() {
        return toDate;
    }
    public AuditLogEntityFilter setToDate(Date toDate) {
        this.toDate = (toDate==null?null:new Date(toDate.getTime()));
        return this;
    }
}
