package com.byyd.middleware.domainlog.filter;

import com.adfonic.domain.AdfonicUser;
import com.adfonic.domain.User;

public class AuditLogFilter {
    private String systemId;
    private User user;
    private AdfonicUser adfonicUser;
    private String objectId;
    private String field;

    public String getSystemId() {
        return systemId;
    }
    public AuditLogFilter systemId(String systemId) {
        this.systemId = systemId;
        return this;
    }

    public User getUser() {
        return user;
    }
    public AuditLogFilter user(User user) {
        this.user = user;
        return this;
    }

    public AdfonicUser getAdfonicUser() {
        return adfonicUser;
    }
    public AuditLogFilter adfonicUser(AdfonicUser adfonicUser) {
        this.adfonicUser = adfonicUser;
        return this;
    }

    public String getObjectId() {
        return objectId;
    }
    public AuditLogFilter objectId(String objectId) {
        this.objectId = objectId;
        return this;
    }

    public String getField() {
        return field;
    }
    public AuditLogFilter field(String field) {
        this.field = field;
        return this;
    }
}
