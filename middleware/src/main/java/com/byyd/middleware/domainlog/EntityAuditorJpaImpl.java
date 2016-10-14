package com.byyd.middleware.domainlog;

import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.map.LRUMap;
import org.hibernate.proxy.HibernateProxyHelper;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.adfonic.audit.AuditorConfig;
import com.adfonic.audit.DelegatingEntityAuditor;
import com.adfonic.audit.EntityAuditor;
import com.adfonic.domain.AdfonicUser;
import com.adfonic.domain.AuditLog;
import com.adfonic.domain.HasPrimaryKeyId;
import com.adfonic.domain.User;
import com.byyd.middleware.domainlog.service.AuditLogManager;

public class EntityAuditorJpaImpl implements EntityAuditor {

    private static final transient Logger LOG = Logger.getLogger(EntityAuditorJpaImpl.class.getName());
    
    private static final int MAX_STRING_VALUE = 255;

    private final ThreadLocal<Context> tlContext = new ThreadLocal<Context>();
    private String systemId;
    private AuditLogManager auditLogManager;
    @SuppressWarnings("rawtypes")
    private Map<Class, List<String>> fieldsToWatchByClass;
    private final LRUMap currentValuesByPK = new LRUMap();

    @PersistenceContext(unitName="adfonic-domain")
    private EntityManager em;

    private final ThreadLocal<Boolean> skipAudit = new ThreadLocal<Boolean>(){
        @Override
        protected Boolean initialValue() {
            return false;
        }
    };

    public EntityAuditorJpaImpl(){
    }
    
    public EntityAuditorJpaImpl(AuditorConfig auditorConfig, AuditLogManager auditLogManager, String systemId) {
        this.auditLogManager = auditLogManager;
        this.systemId = systemId;
        this.fieldsToWatchByClass = auditorConfig.getWatchedClassMap();

        // Wire up the delegator so we get called
        DelegatingEntityAuditor.setDelegate(this);
    }

    /**
     * Encapsulation of the logic determining if an instance needs to be audited.
     * @param o the target object
     * @return true if yes, false otherwise
     */
    private boolean isClassAudited(Object o) {
        List<String> fieldsToWatch = getFieldsToWatch(o);
        return !CollectionUtils.isEmpty(fieldsToWatch);
    }

    /**
     * Encapsulation of the fields to watch lookup, so that changed to incorporate superclasses can be
     * hidden in here later.
     * @param o the target object
     * @return the list of fields to watch, or null if not audited
     */
    private List<String> getFieldsToWatch(Object o) {
        return fieldsToWatchByClass.get(getObjectClass(o));
    }

    //========================================================================
    // JPA lifecycle hooks
    //========================================================================

    @Override
    public void onPostPersist(Object o) {
        if(skipAudit.get()){
            return;
        }
        auditCreate(o);
    }

    @Override
    public void onPostUpdate(Object o) {
        if(skipAudit.get()){
            return;
        }
        
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Removing pending onPostUpdate for " + getObjectClass(o).getName() + ": " + o.toString());
        }
        
        getCurrentContext().getPendingOnPostUpdates().remove(o);
        
        auditUpdate(o);
    }

    @Override
    public void onPostRemove(Object o) {
        if(skipAudit.get()){
            return;
        }
        auditDelete(o);
    }

    protected void storeFieldsValue(String objectId, Object o, List<String> fieldsToWatch) {
        Map<String,String> values = new HashMap<String,String>();
        for (String field : fieldsToWatch) {
            try {
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.fine("Looking for " + getObjectClass(o).getName() + "." + field);
                }
                String value = getObjectValueString(o, field);
                LOG.log(Level.FINE, "{0}", new Object[]{(value == null ? "No value set" : "Found \"" + value + "\"")});
                values.put(field, value);
            } catch (Exception e) {
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.log(Level.WARNING, "Failed to read field \"" + field + "\" from " + getObjectClass(o).getName(), e);
                } else {
                    LOG.log(Level.WARNING, "Failed to read field \"{0}\" from {1} (enable FINE to see stack trace)", new Object[]{field, getObjectClass(o).getName()});
                }
            }
        }
        synchronized (currentValuesByPK) {
            currentValuesByPK.put(objectId, values);
            if (LOG.isLoggable(Level.FINE)) {
                LOG.log(Level.FINE, "currentValuesByInstance's size after put(): " + currentValuesByPK.size());
            }
        }
    }

    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Transactional(readOnly=true,propagation=Propagation.REQUIRES_NEW)
    public void ensureBaselineDataPresence(Object o) {
        if(skipAudit.get()){
            return;
        }
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Ensuring baseline data for object of class " + getObjectClass(o).getName());
        }
        List<String> fieldsToWatch = getFieldsToWatch(o);
        if(fieldsToWatch == null || fieldsToWatch.isEmpty()) {
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("Nothing to watch for " + getObjectClass(o).getName());
            }
            return;
        }
        String objectId = AuditLog.getObjectId(o);
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("OID: " + objectId);
        }
        synchronized (currentValuesByPK) {
            if(currentValuesByPK.get(objectId) != null) {
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.fine("Data found");
                }
                return;
            }
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("Data not found");
            }
             // Extract relevant fields after reloading the object using the PM, so that no lazy init issues arise.
            Class clazz = getObjectClass(o);
            Long pk = getObjectPk(o);
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("PK: " + pk + " - getting instance of type " + clazz.getName());
            }
            Object dbO = em.find(clazz, pk);
            if(dbO == null) {
                LOG.log(Level.SEVERE, "Could not load instance of " + clazz.getName() + " with id=" + pk, new Exception());
                return;
            } else {
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.fine("PK: " + pk + " - instance of type " + clazz.getName() + " loaded");
                }
            }
            storeFieldsValue(objectId, dbO, fieldsToWatch);
        }
    }

    //========================================================================
    // Auditor methods
    //========================================================================

    private void auditCreate(Object o) {
        if(!isClassAudited(o)) {
            return;
        }

        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine(getObjectClass(o).getName() + ": " + o.toString());
        }

        List<String> fieldsToWatch = getFieldsToWatch(o);

        auditChange(o, AuditLog.CREATE_FIELD_PLACEHOLDER, null, null);

        for (String field : fieldsToWatch) {
            try {
                auditChange(o, field, null, getObjectValueString(o, field));
            } catch (Exception e) {
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.log(Level.WARNING, "Failed to read field \"" + field + "\" from " + getObjectClass(o).getName(), e);
                } else {
                    LOG.log(Level.WARNING, "Failed to read field \"" + field + "\" from " + getObjectClass(o).getName() + " (enable FINE to see stack trace)");
                }
            }
        }
        // Store this state as the new "old state" for the object
        String objectId = AuditLog.getObjectId(o);
        storeFieldsValue(objectId, o, fieldsToWatch);

    }

    @SuppressWarnings("unchecked")
    private void auditUpdate(Object o) {
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine(getObjectClass(o).getName() + ": " + o.toString());
        }

        List<String> fieldsToWatch = fieldsToWatchByClass.get(getObjectClass(o));
        if (CollectionUtils.isEmpty(fieldsToWatch)) {
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("Nothing to watch for class " + getObjectClass(o).getName());
            }
            return;
        }

        Map<String,String> oldValues;
        String objectId = AuditLog.getObjectId(o);
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("OID: " + objectId);
        }
        synchronized (currentValuesByPK) {
            oldValues = (Map<String,String>)currentValuesByPK.remove(objectId);
            if (LOG.isLoggable(Level.FINE)) {
                LOG.log(Level.FINE, "currentValuesByInstance's size after remove(): " + currentValuesByPK.size());
            }
            if (oldValues == null) {
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.log(Level.WARNING, getObjectClass(o).getName() + ": " + o.toString() + " - No old values found", new IllegalStateException());
                } else {
                    LOG.log(Level.WARNING, getObjectClass(o).getName() + ": " + o.toString() + " - No old values found (enable FINE to see stack trace)");
                }
            } else if (oldValues.isEmpty()) {
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.log(Level.WARNING, getObjectClass(o).getName() + ": " + o.toString() + " - Old values is empty", new IllegalStateException());
                } else {
                    LOG.log(Level.WARNING, getObjectClass(o).getName() + ": " + o.toString() + " - Old values is empty (enable FINE to see stack trace)");
                }
            }
        }

        auditChangedFields(fieldsToWatch, o, oldValues);
        
        // Shove the new state as the new "old state"
        storeFieldsValue(objectId, o, fieldsToWatch);
    }

    private void auditChangedFields(List<String> fieldsToWatch, Object o, Map<String, String> oldValues) {
        for (String field : fieldsToWatch) {
            try {
                String oldValue = oldValues == null ? null : oldValues.get(field);
                String newValue = getObjectValueString(o, field);
                if ((newValue == null && oldValue != null) || (newValue != null && !newValue.equals(oldValue))) {
                    LOG.log(Level.FINE, "{0}.{1}: was \"{2}\" - now is \"{3}\" ({4})", new Object[]{getObjectClass(o).getName(), field, oldValue, newValue, o.toString()});
                    auditChange(o, field, oldValue, newValue);
                }
            } catch (Exception e) {
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.log(Level.WARNING, "Failed to read field \"" + field + "\" from " + getObjectClass(o).getName(), e);
                } else {
                    LOG.log(Level.WARNING, "Failed to read field \"{0}\" from {1} (enable FINE to see stack trace)", new Object[]{field, getObjectClass(o).getName()});
                }
            }
        }
    }

    private void auditDelete(Object o) {
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine(getObjectClass(o).getName() + ": " + o.toString());
        }

        auditChange(o, AuditLog.DELETE_FIELD_PLACEHOLDER, null, null);
    }

    private void auditChange(Object o, String field, String oldValue, String newValue) {
        // This is a hack to put back the original OID format that was used
        // prior to the "expose ids" change.  Pretty much everything we're
        // auditing *should* be HasPrimaryKeyId, but we have a fallback to
        // the old method in case it's not.  The expected format of the
        // objectId is: 123[OID]com.adfonic.domain.FooBar
        String objectId = AuditLog.getObjectId(o);

        String localOldValue = oldValue;
        if (localOldValue != null && localOldValue.length() > MAX_STRING_VALUE) {
            localOldValue = localOldValue.substring(0, MAX_STRING_VALUE);
        }
        
        String localNewValue = newValue;
        if (localNewValue != null && localNewValue.length() > MAX_STRING_VALUE) {
            localNewValue = localNewValue.substring(0, MAX_STRING_VALUE);
        }

        // If anybody bound a User and/or AdfonicUser to this thread, make sure
        // we attribute this audit entry to them.
        Context context = getCurrentContext();
        if (LOG.isLoggable(Level.FINER)) {
            LOG.finer("Auditing change of " + objectId + "." + field + " in " + systemId + ", User " + (context.getUser() == null ? "null" : ("id=" + context.getUser().getId())) + ", AdfonicUser " + (context.getAdfonicUser() == null ? "null" : ("id=" + context.getAdfonicUser().getId())));
        }
        AuditLog auditLog = new AuditLog(systemId, context.getUser(), context.getAdfonicUser(), objectId, field, localOldValue, localNewValue);
        auditLogManager.create(auditLog);
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("unchecked")
    public void expectOnPostUpdate(Object o) {
        if(skipAudit.get()){
            return;
        }
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine(getObjectClass(o).getName() + ": " + o.toString());
        }

        getCurrentContext().getPendingOnPostUpdates().add(o);
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void invokePendingOnPostUpdates() {
        if(skipAudit.get()){
            return;
        }
        // Copy to avoid concurrent modification, as onPostUpdate does removes
        Set copy = new LinkedHashSet(getCurrentContext().getPendingOnPostUpdates());
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Invoking " + copy.size() + " pending onPostUpdate call(s)");
        }
        for (Object o : copy) {
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("Invoking pending onPostUpdate for " + getObjectClass(o).getName() + ": " + o.toString());
            }
            onPostUpdate(o);
        }
    }

    /**
     * Bind a User and/or AdfonicUser to the current thread.  This method
     * should be called once per request, typically via a servlet filter.
     * At the end of the request, the caller is responsible for calling
     * the unbindContext method in order to free up bound resources.
     * The caller should ideally use try/finally in the filter to ensure
     * that the context gets unbound.
     * @param user the User to bind to the current thread's context
     * @param adfonicUser the AdfonicUser to bind
     */
    @Override
    public void bindContext(User user, AdfonicUser adfonicUser) {
        Context context = getCurrentContext();
        if (context.getUser() != null || context.getAdfonicUser() != null) {
            // Slop...reuse it, but log a warning about it so the
            // caller is aware that they're using the wrong pattern.
            LOG.warning("Context with User and/or AdfonicUser already bound to current thread, did you forget to call unbindContext last time?");
        }
        if (LOG.isLoggable(Level.FINER)) {
            LOG.finer("Binding context: User " + (user == null ? "null" : ("id=" + user.getId())) + ", AdfonicUser " + (adfonicUser == null ? "null" : ("id=" + adfonicUser.getId())));
        }
        context.setUser(user);
        context.setAdfonicUser(adfonicUser);
    }

    /**
     * Unbind any previously bound context attributes
     */
    @Override
    public void unbindContext() {
        if (LOG.isLoggable(Level.FINER)) {
            LOG.finer("Unbinding context");
        }
        tlContext.set(null);
    }

    private Context getCurrentContext() {
        Context context = tlContext.get();
        if (context == null) {
            if (LOG.isLoggable(Level.FINER)) {
                LOG.finer("Creating ThreadLocal Context");
            }
            context = new Context();
            tlContext.set(context);
        }
        return context;
    }

    /**
     * Get the PK of a given object to be used on AuditLog
     * @param o the object
     * @return the object's PK if instance of HasPrimaryKeyId
     * @throws IllegalArgumentException if the object is not an instance of HasPrimaryKeyId
     */
    public static Long getObjectPk(Object o) {
        if (o instanceof HasPrimaryKeyId) {
            return ((HasPrimaryKeyId)o).getId();
        } else {
            throw new IllegalArgumentException("No way to derive PK for " + getObjectClass(o).getName() + " (non HasPrimaryKeyId)");
        }
    }

    /**
     * Thread local bound "context" that holds per-request attributes
     * identifying the user associated with changes being audited.
     */
    private static final class Context {
        private User user;
        private AdfonicUser adfonicUser;
        @SuppressWarnings("rawtypes")
        private Set pendingOnPostUpdates = new LinkedHashSet();

        public User getUser() {
            return user;
        }
        public void setUser(User user) {
            this.user = user;
        }

        public AdfonicUser getAdfonicUser() {
            return adfonicUser;
        }
        public void setAdfonicUser(AdfonicUser adfonicUser) {
            this.adfonicUser = adfonicUser;
        }

        @SuppressWarnings("rawtypes")
        public Set getPendingOnPostUpdates() {
            return pendingOnPostUpdates;
        }
    }

    @Override
    public boolean setEnabledForCurrentThread(boolean enable) {
        if (enable == skipAudit.get()) {
            boolean skip = !enable;
            skipAudit.set(skip);
            return skip;
        } else {
            return enable;
        }
    }
    
    @SuppressWarnings("rawtypes")
    private static Class getObjectClass(Object o){ 
        return HibernateProxyHelper.getClassWithoutInitializingProxy(o); 
    }
    
    private String getObjectValueString(Object o, String field) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException{
        Object value = null;
        value = PropertyUtils.getProperty(o, field);
        if (value instanceof BigDecimal){
            value = new Double(((BigDecimal)value).doubleValue());
        }
        
        return value == null ? null : value.toString();
    }
}
