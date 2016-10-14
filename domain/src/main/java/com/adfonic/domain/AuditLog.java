package com.adfonic.domain;

import java.util.Date;

import javax.persistence.*;

import org.hibernate.proxy.HibernateProxyHelper;

@Entity
@Table(name="AUDIT_LOG")
@ExcludeDefaultListeners
public class AuditLog extends BusinessKey {
    private static final long serialVersionUID = 3L;

    public static final String CREATE_FIELD_PLACEHOLDER = "@CREATE";
    public static final String DELETE_FIELD_PLACEHOLDER = "@DELETE";

    @Id @GeneratedValue @Column(name="ID")
    private long id;
    @Column(name="SYSTEM_ID",length=32,nullable=false)
    private String systemId;
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="USER_ID",nullable=true)
    private User user;
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="ADFONIC_USER_ID",nullable=true)
    private AdfonicUser adfonicUser;
    @Column(name="OBJECT_ID",length=255,nullable=false)
    private String objectId;
    @Column(name="FIELD",length=255,nullable=true)
    private String field;
    @Column(name="OLD_VALUE",length=255,nullable=true)
    private String oldValue;
    @Column(name="NEW_VALUE",length=255,nullable=true)
    private String newValue;
    @Column(name="TIMESTAMP",nullable=false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date timestamp;

    {
    this.timestamp = new Date();
    }

    AuditLog() {}

    public AuditLog(String systemId, User user, AdfonicUser adfonicUser, String objectId, String field, String oldValue, String newValue) {
        this.systemId = systemId;
        this.user = user;
        this.adfonicUser = adfonicUser;
        this.objectId = objectId;
        this.field = field;
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    public long getId() { return id; };

    public String getSystemId() {
        return systemId;
    }

    public User getUser() {
        return user;
    }

    public AdfonicUser getAdfonicUser() {
        return adfonicUser;
    }

    public String getObjectId() {
        return objectId;
    }

    public String getField() {
        return field;
    }

    public String getOldValue() {
        return oldValue;
    }

    public String getNewValue() {
        return newValue;
    }

    public Date getTimestamp() {
        return timestamp;
    }
    
    /**
     * Get the object id of a given object to be used on AuditLog
     * @param o the object
     * @return the value used on AuditLog.objectId
     * @throws IllegalArgumentException if the object is not an instance of HasPrimaryKeyId
     */
    @SuppressWarnings("rawtypes")
    public static String getObjectId(Object o) {
		Class clazz = HibernateProxyHelper.getClassWithoutInitializingProxy(o);
        if (o instanceof HasPrimaryKeyId) {
            String objectId = ((HasPrimaryKeyId)o).getId() + "[OID]" + clazz.getName();
            if (objectId.length() > 255) {
                objectId = objectId.substring(0, 255);
            }
            return objectId;
        } else {
            throw new IllegalArgumentException("No way to derive object id for " + clazz.getName() + " (non HasPrimaryKeyId)");
        }
    }
}
