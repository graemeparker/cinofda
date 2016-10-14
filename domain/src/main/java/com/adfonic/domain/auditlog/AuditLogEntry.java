package com.adfonic.domain.auditlog;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
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
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.adfonic.domain.BusinessKey;

@Entity
@Table(name="AUDIT_ENTRY")
@ExcludeDefaultListeners
public class AuditLogEntry extends BusinessKey {
    
    private static final long serialVersionUID = 1L;
    
    private static final TimeZone DEFAULT_TIMEZONE_OFFSET = TimeZone.getDefault();
    private static final int MAX_BLOB_LENGHT = 65535;
    private static final String MAXIMUN_BLOB_SIZE_REACHED = "[Field value too long to be stored]";
    
    public enum AuditLogEntryType {
        INT, 
        DECIMAL, 
        DATETIME, 
        VARCHAR, 
        BOOLEAN, 
        BLOB
    }

    @Id 
    @GeneratedValue
    @Column(name="ID")
    private long id;
    
    @Column(name="CREATED_AT_TIMESTAMP",nullable=false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date timestamp;
    
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="AUDIT_ENTITY_ID",nullable=false)
    private AuditLogEntity auditLogEntity;
    
    @Column(name="ENTRY_NAME",length=100,nullable=false)
    private String name;
    
    @Column(name="ENTRY_TYPE",nullable=false)
    @Enumerated(EnumType.STRING)
    private AuditLogEntryType auditLogEntryType;
    
    @Column(name="OLD_VALUE_INT",nullable=true)
    private Long oldValueInt;
    
    @Column(name="NEW_VALUE_INT",nullable=true)
    private Long newValueInt;
    
    @Column(name="OLD_VALUE_DECIMAL",nullable=true)
    private BigDecimal oldValueDecimal;
    
    @Column(name="NEW_VALUE_DECIMAL",nullable=true)
    private BigDecimal newValueDecimal;
    
    @Column(name="OLD_VALUE_DATETIME",nullable=true)
    @Temporal(TemporalType.TIMESTAMP)
    private Date oldValueDate;
    
    @Column(name="NEW_VALUE_DATETIME",nullable=true)
    @Temporal(TemporalType.TIMESTAMP)
    private Date newValueDate;
    
    @Column(name="OLD_VALUE_VARCHAR",length=1024,nullable=true)
    private String oldValueVarchar;
    
    @Column(name="NEW_VALUE_VARCHAR",length=1024,nullable=true)
    private String newValueVarchar;
    
    @Column(name="OLD_VALUE_BOOLEAN",nullable=true)
    private Boolean oldValueBoolean;
    
    @Column(name="NEW_VALUE_BOOLEAN",nullable=true)
    private Boolean newValueBoolean;
   
    @Column(name="OLD_VALUE_BLOB",nullable=true)
    @Lob
    private String oldValueBlob;
    
    @Column(name="NEW_VALUE_BLOB",nullable=true)
    @Lob
    private String newValueBlob;
    
    public AuditLogEntry() {
        setDefaultTimestamp();
    }
    
    public AuditLogEntry(AuditLogEntity auditLogEntity, String name, AuditLogEntryType auditLogEntryType) {
        this.name = name;
        this.auditLogEntity = auditLogEntity;
        this.auditLogEntryType = auditLogEntryType;
        setDefaultTimestamp();
    }
    
    public AuditLogEntry(AuditLogEntity auditLogEntity, String name, AuditLogEntryType auditLogEntryType, Object oldValue, Object newValue) {
        this.name = name;
        this.auditLogEntity = auditLogEntity;
        this.auditLogEntryType = auditLogEntryType;
        setPropertyValue(oldValue, newValue);
        setDefaultTimestamp();
    }

    @Override
    public long getId() { 
        return id; 
    }

    public AuditLogEntity getAuditLogEntity() {
        return auditLogEntity;
    }

    public void setAuditLogEntity(AuditLogEntity auditLogEntity) {
        this.auditLogEntity = auditLogEntity;
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

    public Long getOldValueInt() {
        return oldValueInt;
    }

    public void setOldValueInt(Long oldValueInt) {
        this.oldValueInt = oldValueInt;
    }

    public Long getNewValueInt() {
        return newValueInt;
    }

    public void setNewValueInt(Long newValueInt) {
        this.newValueInt = newValueInt;
    }

    public BigDecimal getOldValueDecimal() {
        return oldValueDecimal;
    }

    public void setOldValueDecimal(BigDecimal oldValueDecimal) {
        this.oldValueDecimal = oldValueDecimal;
    }

    public BigDecimal getNewValueDecimal() {
        return newValueDecimal;
    }

    public void setNewValueDecimal(BigDecimal newValueDecimal) {
        this.newValueDecimal = newValueDecimal;
    }

    public Date getOldValueDate() {
        return oldValueDate;
    }

    public void setOldValueDate(Date oldValueDate) {
        this.oldValueDate = (oldValueDate==null) ? null : new Date(oldValueDate.getTime());
    }

    public Date getNewValueDate() {
        return newValueDate;
    }

    public void setNewValueDate(Date newValueDate) {
        this.newValueDate = (newValueDate==null) ? null : new Date(newValueDate.getTime());
    }

    public String getOldValueVarchar() {
        return oldValueVarchar;
    }

    public void setOldValueVarchar(String oldValueVarchar) {
        this.oldValueVarchar = oldValueVarchar;
    }

    public String getNewValueVarchar() {
        return newValueVarchar;
    }

    public void setNewValueVarchar(String newValueVarchar) {
        this.newValueVarchar = newValueVarchar;
    }

    public Boolean getOldValueBoolean() {
        return oldValueBoolean;
    }

    public void setOldValueBoolean(Boolean oldValueBoolean) {
        this.oldValueBoolean = oldValueBoolean;
    }

    public Boolean getNewValueBoolean() {
        return newValueBoolean;
    }

    public void setNewValueBoolean(Boolean newValueBoolean) {
        this.newValueBoolean = newValueBoolean;
    }

    public String getOldValueBlob() {
        return oldValueBlob;
    }

    public void setOldValueBlob(String oldValueBlob) {
        this.oldValueBlob = oldValueBlob;
    }

    public String getNewValueBlob() {
        return newValueBlob;
    }

    public void setNewValueBlob(String newValueBlob) {
        this.newValueBlob = newValueBlob;
    }
    
    public void setPropertyValue(Object oldValue, Object newValue) {
        switch (this.auditLogEntryType) {
            case INT: 
                this.oldValueInt = (oldValue==null) ? null : ((Number) oldValue).longValue();
                this.newValueInt = (newValue==null) ? null : ((Number) newValue).longValue();
                break;
            case DECIMAL:
                this.oldValueDecimal = (BigDecimal) oldValue;
                this.newValueDecimal = (BigDecimal) newValue;
                break;
            case DATETIME:
                this.oldValueDate = (oldValue==null) ? null : new Date(((Date) oldValue).getTime());
                this.newValueDate = (newValue==null) ? null : new Date(((Date) newValue).getTime());
                break;
            case VARCHAR:
                this.oldValueVarchar = (oldValue==null) ? null : oldValue.toString();
                this.newValueVarchar = (newValue==null) ? null : newValue.toString();
                break;
            case BOOLEAN:
                this.oldValueBoolean = (Boolean) oldValue;
                this.newValueBoolean = (Boolean) newValue;
                break;
            case BLOB:
                this.oldValueBlob = (oldValue==null) ? null : trunkBlob(oldValue.toString());
                this.newValueBlob = (newValue==null) ? null : trunkBlob(newValue.toString());
                break;
            default:
                break;
        }
    }
    
    private String trunkBlob(String stringBlob) {
        String blobValue = null;
        if (stringBlob.getBytes().length>MAX_BLOB_LENGHT){
            blobValue = MAXIMUN_BLOB_SIZE_REACHED;
        }else{
            blobValue = stringBlob;
        }
        return blobValue;
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append("[ID=").append(id);
        sb.append(", NAME=").append(name);
        sb.append(", TIMESTAMP=").append(timestamp);
        sb.append(", AUDIT_ENTITY_ID=").append(auditLogEntity==null?"NULL":auditLogEntity.getId());
        if (auditLogEntryType==null){
            sb.append(", AUDIT_ENTRY_TYPE=NULL");
        }else{
            sb.append(", AUDIT_ENTRY_TYPE=").append(auditLogEntryType);
            switch (auditLogEntryType) {
                case INT: 
                    sb.append(", OLD_VALUE_INT=").append(oldValueInt);
                    sb.append(", NEW_VALUE_INT=").append(newValueInt);
                    break;
                case DECIMAL:
                    sb.append(", OLD_VALUE_DECIMAL=").append(oldValueDecimal);
                    sb.append(", NEW_VALUE_DECIMAL=").append(newValueDecimal);
                    break;
                case DATETIME:
                    sb.append(", OLD_VALUE_DATETIME=").append(oldValueDate);
                    sb.append(", NEW_VALUE_DATETIME=").append(newValueDate);
                    break;
                case VARCHAR:
                    sb.append(", OLD_VALUE_VARCHAR=").append(oldValueVarchar);
                    sb.append(", NEW_VALUE_VARCHAR=").append(newValueVarchar);
                    break;
                case BOOLEAN:
                    sb.append(", OLD_VALUE_BOOLEAN=").append(oldValueBoolean);
                    sb.append(", NEW_VALUE_BOOLEAN=").append(newValueBoolean);
                    break;
                case BLOB:
                    sb.append(", OLD_VALUE_BLOB=").append(oldValueBlob);
                    sb.append(", NEW_VALUE_BLOB=").append(newValueBlob);
                    break;
                default:
                    break;
            }
            sb.append("]");
        }
        return sb.toString();
    }
    
    private void setDefaultTimestamp(){
        long localTime = Calendar.getInstance().getTimeInMillis();
        this.timestamp = new Date(localTime - DEFAULT_TIMEZONE_OFFSET.getOffset(localTime));
    }
}
