package com.adfonic.audit;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.PostPersist;
import javax.persistence.PostRemove;
import javax.persistence.PostUpdate;

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
public class DelegatingEntityAuditor {
    private static final transient Logger LOG = Logger.getLogger(DelegatingEntityAuditor.class.getName());

    private static EntityAuditor sharedDelegate;

    /**
     * Call this method from your Spring context using method invoking beans
     * to "wire" this class with a given instance of EntityAuditor
     */
    public static void setDelegate(EntityAuditor entityAuditor) {
        if (entityAuditor != null) {
            if (sharedDelegate != null) {
                LOG.warning("Overriding (re-setting) delegate to: " + entityAuditor.getClass().getName());
            } else {
                LOG.info("Setting delegate to: " + entityAuditor.getClass().getName());
            }
        } else if (sharedDelegate == null) {
            LOG.info("Un-setting delegate");
        } else {
            LOG.warning("Delegate is already null, why do you keep calling this?");
        }
        sharedDelegate = entityAuditor;
    }

    @PostPersist
    public void onPostPersist(Object o) {
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine(o.getClass().getName() + ": " + o.toString());
        }
        if (sharedDelegate != null) {
            sharedDelegate.onPostPersist(o);
        }
    }

    //@PostLoad
    //public void onPostLoad(Object o) {
    //    if (sharedDelegate != null) {
    //        sharedDelegate.onPostLoad(o);
    //    }
    //}

    @PostUpdate
    public void onPostUpdate(Object o) {
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine(o.getClass().getName() + ": " + o.toString());
        }
        if (sharedDelegate != null) {
            sharedDelegate.onPostUpdate(o);
        }
    }

    @PostRemove
    public void onPostRemove(Object o) {
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine(o.getClass().getName() + ": " + o.toString());
        }
        if (sharedDelegate != null) {
            sharedDelegate.onPostRemove(o);
        }
    }
}
