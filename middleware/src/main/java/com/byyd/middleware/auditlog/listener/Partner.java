package com.byyd.middleware.auditlog.listener;

public class Partner {
    private long internalId = 0;
    private String name;
    private String email;
    
    public Partner(String name, String email) {
        super();
        this.name = name;
        this.email = email;
    }

    protected long getInternalId() {
        return internalId;
    }
    
    public String getName() {
        return name;
    }
    
    public String getEmail() {
        return email;
    }
}
