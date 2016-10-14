package com.adfonic.domain.auditlog;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ExcludeDefaultListeners;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.adfonic.domain.BusinessKey;

@Entity
@Table(name="AUDIT_ENTITY")
@ExcludeDefaultListeners
public class AuditLogEntity extends BusinessKey  {
	
    private static final long serialVersionUID = 1L;
    private static final TimeZone DEFAULT_TIMEZONE_OFFSET = TimeZone.getDefault();
    
    public enum AuditOperation {
    	CREATE, UPDATE, DELETE
    };
    
    public enum UserType {
    	USER, ADFONIC_USER, PARTNER, SYSTEM 
    };

    @Id 
    @GeneratedValue 
    @Column(name="ID")
    private long id;
    
    @Column(name="CREATED_AT_TIMESTAMP",nullable=false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date timestamp;
    
    @Column(name="ENTITY_NAME",length=64,nullable=false)
    private String entityName;
    
    @Column(name="ENTITY_ID",nullable=false)
    private long entityId;
    
    @Column(name="AUDIT_OPERATION",nullable=false)
    @Enumerated(EnumType.STRING)
    private AuditOperation auditOperation;
    
    @Column(name="SOURCE",length=64,nullable=false)
    private String source;
    
    @Column(name="USER_TYPE",nullable=false)
    @Enumerated(EnumType.STRING)
    private UserType userType;
    
    @Column(name="USER_ID",nullable=true)
    private long userId;
    
    @Column(name="USER_NAME",length=161,nullable=false)
    private String userName;
    
    @Column(name="USER_EMAIL",length=255,nullable=false)
    private String userEmail;
    
    @Column(name="TRANSACTION_ID",length=64,nullable=true)
    private String transactionId;
    
    @OneToMany(fetch=FetchType.LAZY)
    @OrderBy("id")
    @JoinColumn(name="AUDIT_ENTITY_ID",nullable=true)
    private List<AuditLogEntry> auditLogEntries;
    
    {
    	long localTime = Calendar.getInstance().getTimeInMillis();
    	this.timestamp = new Date(localTime - DEFAULT_TIMEZONE_OFFSET.getOffset(localTime));
    }
    
    public AuditLogEntity() { }
    
	public AuditLogEntity(String entityName, long entityId, AuditOperation auditOperation, String source, 
						  UserType userType, long userId, String userName, String userEmail, String transactionId) {
		this.entityName = entityName;
		this.entityId = entityId;
		this.auditOperation = auditOperation;
		this.source = source;
		this.userType = userType;
		this.userId = userId;
		this.userName = userName;
		this.userEmail = userEmail;
		this.transactionId = transactionId;
	}

	public long getId() { 
		return id; 
	}
	
	public Date getTimestamp() { 
		return timestamp; 
	}
	
	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
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
	
	public AuditOperation getAuditOperation() {
		return auditOperation;
	}
	
	public void setAuditOperation(AuditOperation auditOperation) {
		this.auditOperation = auditOperation;
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

	public List<AuditLogEntry> getAuditLogEntries() {
		return auditLogEntries;
	}

	public void setAuditLogEntries(List<AuditLogEntry> auditLogEntries) {
		this.auditLogEntries = auditLogEntries;
	}

	@Override
	public String toString(){
		StringBuffer sb = new StringBuffer();
		sb.append("[ID=").append(id);
		sb.append(", TIMESTAMP=").append(timestamp);
		sb.append(", ENTITY_NAME=").append(entityName);
		sb.append(", ENTITY_ID=").append(entityId);
		sb.append(", AUDIT_OPERATION=").append(auditOperation);
		sb.append(", SOURCE=").append(source);
		sb.append(", USER_TYPE=").append(userType);
		sb.append(", USER_ID=").append(userId);
		sb.append(", USER_NAME=").append(userName);
		sb.append(", USER_EMAIL=").append(userEmail);
		sb.append(", TRANSACTION_ID=").append(transactionId).append("]");
		return sb.toString();
	}
}
