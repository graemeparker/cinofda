package com.byyd.middleware.auditlog.listener;

import java.util.logging.Logger;

import javax.persistence.PostPersist;
import javax.persistence.PostRemove;
import javax.persistence.PostUpdate;

import com.adfonic.domain.auditlog.AuditLogEntity.AuditOperation;

/**
 * Audits changes to specified fields in the domain fields by creating
 * AuditLog instances.
 *
 * This class is intended to be used as a "default entity listener" in
 * the JPA persistence unit, or listed in @EntityListeners on specific
 * entities.  But the default entity listener approach is really what's
 * intended by this implementation.
 *
 * It was discovered that JPA (or probably Hibernate) actually creates
 * one instance of this class for every single entity and every single
 * pre/post hook.  That was like 422 instances...  So I made this class
 * just a thin pass-through layer that simply delegates to the actual
 * EntityAuditor singleton (bean).
 */
public class AuditLogJpaDelegator {

    private static final transient Logger LOG = Logger.getLogger(AuditLogJpaDelegator.class.getName());
    
    private static AuditLogJpaListener sharedAuditLogJpaListener;
    
    /**
     * Call this method from your Spring context using method invoking beans
     * to "wire" this class with a given instance of EntityAuditor
     */
    public static void setDelegate(AuditLogJpaListener auditLogJpaListener) {
        if (auditLogJpaListener != null) {
            if (sharedAuditLogJpaListener != null) {
                LOG.warning("[AuditLog] Overriding (re-setting) delegate to: " + auditLogJpaListener.getClass().getName());
            } else {
                LOG.info("[AuditLog] Setting delegate to: " + auditLogJpaListener.getClass().getName());
            }
        } else if (sharedAuditLogJpaListener == null) {
            LOG.info("[AuditLog] Un-setting delegate");
        } else {
            LOG.warning("[AuditLog] Delegate is already null, why do you keep calling this?");
        }
        sharedAuditLogJpaListener = auditLogJpaListener;
    }
    
    //========================================================================
    // JPA lifecycle hooks
    //========================================================================

    @PostPersist
    public void onPostPersist(Object o) {
        if (sharedAuditLogJpaListener!=null){
            sharedAuditLogJpaListener.audit(o, AuditOperation.CREATE);
        }
    }
    
    @PostUpdate
    public void onPostUpdate(Object o) {
        if (sharedAuditLogJpaListener!=null){
            sharedAuditLogJpaListener.audit(o, AuditOperation.UPDATE);
        }
    }
    
    @PostRemove
    public void onPostRemove(Object o) {
        if (sharedAuditLogJpaListener!=null){
            sharedAuditLogJpaListener.audit(o, AuditOperation.DELETE);
        }
    }
}
