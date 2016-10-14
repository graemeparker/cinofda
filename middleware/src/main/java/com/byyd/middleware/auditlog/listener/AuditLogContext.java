package com.byyd.middleware.auditlog.listener;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import org.apache.commons.collections.map.LRUMap;

import com.adfonic.domain.AdfonicUser;
import com.adfonic.domain.User;
import com.adfonic.domain.auditlog.AuditLogEntity.UserType;


public class AuditLogContext {
    
    private static final transient Logger LOG = Logger.getLogger(AuditLogContext.class.getName());
    
    private UserType userType = null;
    private long userId = 0L;
    private String userName = null;
    private String userEmail = null;
    private Map<String, Object> pendientUpdates = new ConcurrentHashMap<String, Object>();
    
    /**
     * Map to save previous property values for audited entities
     * for the current context
     */
    private LRUMap entitiesCache = new LRUMap();
    
    public AuditLogContext (User user, AdfonicUser adfonicUser){
        if (adfonicUser!=null){
            this.userType = UserType.ADFONIC_USER;
            this.userId = adfonicUser.getId();
            this.userName = getFullName(adfonicUser.getFirstName(), adfonicUser.getLastName()) ;
            this.userEmail = adfonicUser.getEmail();
        }else if (user!=null){
            this.userType = UserType.USER;
            this.userId = user.getId();
            this.userName = getFullName(user.getFirstName(), user.getLastName()) ;
            this.userEmail = user.getEmail();
        }
    }
    
    public AuditLogContext (Partner partner){
        this.userType = UserType.PARTNER;
        this.userId = partner.getInternalId();
        this.userName = partner.getName() ;
        this.userEmail = partner.getEmail();
    }
    
    public AuditLogContext (System system){
        this.userType = UserType.SYSTEM;
        this.userId = 0;
        this.userName = system.getName();
        this.userEmail = system.getName();
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

    public Map<String, Object> getPendientUpdates() {
        return pendientUpdates;
    }

    private String getFullName(String name, String lastName){
        return name + " " + lastName;
    }
    
    public LRUMap getEntitiesCache() {
        return entitiesCache;
    }

    public void clean(){
        LOG.fine("Cleaning AuditLog context");
        this.pendientUpdates.clear();
        this.entitiesCache.clear();
        this.pendientUpdates = null;
        this.entitiesCache=null;
    }
}
