package com.byyd.middleware.auditlog.listener;

import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.commons.beanutils.NestedNullException;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.map.LRUMap;
import org.apache.commons.lang.time.StopWatch;
import org.hibernate.LazyInitializationException;
import org.hibernate.proxy.HibernateProxyHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.adfonic.domain.AdfonicUser;
import com.adfonic.domain.HasPrimaryKeyId;
import com.adfonic.domain.User;
import com.adfonic.domain.auditlog.AuditLogEntity;
import com.adfonic.domain.auditlog.AuditLogEntity.AuditOperation;
import com.adfonic.domain.auditlog.AuditLogEntry;
import com.byyd.middleware.auditlog.config.AuditLogConfig;
import com.byyd.middleware.auditlog.config.AuditLogPropertyConfig;
import com.byyd.middleware.auditlog.exception.AuditLogException;
import com.byyd.middleware.auditlog.service.AuditLogManager;

public class AuditLogJpaListener {

    private static final transient Logger LOG = Logger.getLogger(AuditLogJpaListener.class.getName());
    
    private static final long DATE_MASK_MILISECONDS_PER_DAY = 24 * 60 * 60 * 1000;
    
    /**
     * Application name which performs the audit operations
     */
    private String auditSource;
    
    @Autowired
    private AuditLogManager auditLogManager;
    
    @Autowired
    private AuditLogConfig auditLogConfig;
    
    @PersistenceContext(unitName="adfonic-domain")
    private EntityManager em;
    
    /**
     * Context information
     */
    private final ThreadLocal<AuditLogContext> tlAuditLogContext = new ThreadLocal<AuditLogContext>();
   
    /**
     * Constructor
     */
    public AuditLogJpaListener(){
        
    }
    
    public AuditLogJpaListener(String auditSource){
        this.auditSource = auditSource;
        AuditLogJpaDelegator.setDelegate(this);
    }
    
    /**
     * Entry method to perform preupdate JPA lifecycle operation
     * This method opens a new transaction to recover the previous state of 
     * the object which will be updated.
     * 
     * @param o Object instance to be audited
     */
    @SuppressWarnings("rawtypes")
    @Transactional(readOnly=true,propagation=Propagation.REQUIRES_NEW)
    public void saveCurrentPersistedValues(Object o) {
        if ((o!=null)&&(o instanceof HasPrimaryKeyId)){
            Class objectClass = getObjectClass(o);
            List<AuditLogPropertyConfig> propertiesToWath = auditLogConfig.getAuditLogPropertyConfig(objectClass.getName());
            // Check if the entity has to be audited
            if (propertiesToWath!=null){
                Long entityId = ((HasPrimaryKeyId)o).getId();
                
                if (entityId>0){
                    String objectKey = getObjectKey(objectClass.getName(), entityId);
                    
                    // Checks if we already have the current values saved
                    synchronized (this.em) {
                        storeCurrentObjectProperties(objectClass, propertiesToWath, entityId, objectKey);
                    }
                }
                
                addPendientUpdate(o);
            }
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void storeCurrentObjectProperties(Class objectClass, List<AuditLogPropertyConfig> propertiesToWath, Long entityId, String objectKey) {
        LRUMap entitiesCache = getCurrentContext().getEntitiesCache();
        if (!entitiesCache.containsKey(objectKey)){
            Object currentPersistedObject = em.find(objectClass, entityId);
            if (currentPersistedObject !=null){
                Map<String, Object> propertiesValues = getEntityPropertyValues(currentPersistedObject, propertiesToWath);
                if (!propertiesValues.isEmpty()){
                    entitiesCache.put(objectKey, propertiesValues);
                }
            }
        }
    }
    
    /**
     * Entry method to perform an audit operation.
     * 
     * @param o Object instance to be audited
     * @param auditOperation Audit operation
     */
    protected void audit(Object o, AuditOperation auditOperation) {
        String entityName = getObjectClass(o).getName();
        
        List<AuditLogPropertyConfig>  auditLogProperties = auditLogConfig.getAuditLogPropertyConfig(entityName);
        if (auditLogProperties==null){
            LOG.log(Level.FINE, "[AuditLog] {0} class will NOT be audited. There are no configuration for this entity.", entityName);
        }else if(!(o instanceof HasPrimaryKeyId)){
            throw new AuditLogException(entityName + " class can not be audited, it does not implement HasPrimaryKeyId interface.");
        }else{
            // Take starting time
            StopWatch stWatch = new StopWatch();
            stWatch.start();
            LOG.log(Level.FINE, "[AuditLog] {0} class will be audited.", entityName);
            
            Long entityId = ((HasPrimaryKeyId) o).getId();
            
            AuditLogContext auditLogContext = getCurrentContext();
            
            switch(auditOperation){
                case CREATE:
                case UPDATE:
                    auditCreateAndUpdate(o, entityName, entityId, auditLogProperties, auditLogContext, auditOperation);
                    break;
                case DELETE:
                    auditDelete(entityName, entityId, auditLogContext);
                    break;
                default:
                    break;
            }
            
            //Take final time, logging total process time for this entity
            stWatch.stop();
            LOG.log(Level.INFO, "[AuditLog] Process time in {0} audit operation for entity {1}: {2}ms", new Object[]{auditOperation.name(), getObjectKey(o), stWatch.getTime()});
        }
    }
    
    /**
     * Audit entity on CREATE and UPDATE
     */
    private void auditCreateAndUpdate(Object o, String entityName, Long entityId, List<AuditLogPropertyConfig> auditLogProperties, AuditLogContext auditLogContext, AuditOperation auditOperation) {
        // Get object key (classname+id)
        String objectKey = getObjectKey(entityName, entityId);
        
        // Removing pending update operations (see addPendientUpdate and removePendientUpdate)
        if (AuditOperation.UPDATE.equals(auditOperation)){
            getCurrentContext().getPendientUpdates().remove(getObjectKey(o));
        }
        
        // Catch new & old properties values
        Map<String, Object> newPropertiesValues = getEntityPropertyValues(o, auditLogProperties);
        Map<String, Object> oldPropertiesValues = getCurrentEntityPropertiesValues(objectKey);
        
        // Persist AuditLogEntry entities for each property value which has changed
        AuditLogEntity auditLogEntity = null;
        for(AuditLogPropertyConfig auditLogPropertyConfig: auditLogProperties){
            auditLogEntity = persistProperty(entityName, entityId, auditLogContext, auditOperation, newPropertiesValues, oldPropertiesValues, auditLogEntity, auditLogPropertyConfig);
        }
        
        // Add the current object information to the currentValues map
        synchronized (this.em) {
            getCurrentContext().getEntitiesCache().put(objectKey, newPropertiesValues);
        }
    }

    private AuditLogEntity persistProperty(String entityName, 
                                           Long entityId, 
                                           AuditLogContext auditLogContext, 
                                           AuditOperation auditOperation, 
                                           Map<String, Object> newPropertiesValues, 
                                           Map<String, Object> oldPropertiesValues,
                                           AuditLogEntity auditLogEntity,
                                           AuditLogPropertyConfig auditLogPropertyConfig) {
        AuditLogEntity localAuditLogEntity = auditLogEntity;
        Object oldPropertyValue = oldPropertiesValues.get(auditLogPropertyConfig.getKey());
        
        if (newPropertiesValues.containsKey(auditLogPropertyConfig.getKey())){
            Object newPropertyValue = newPropertiesValues.get(auditLogPropertyConfig.getKey());
            if (!haveSameValue(oldPropertyValue, newPropertyValue)){
                if (localAuditLogEntity==null){
                    // Create AuditLogEntity for the current audit operation
                    localAuditLogEntity = createAuditLogEntity(entityName, entityId, auditLogContext, auditOperation);
                }
                createAuditLogEntry(localAuditLogEntity, auditLogPropertyConfig, oldPropertyValue, newPropertyValue);
            }
        }else{
            newPropertiesValues.put(auditLogPropertyConfig.getKey(), oldPropertyValue);
        }
        return localAuditLogEntity;
    }
    
    /**
     * Audit entity on DELETE.
     */
    private void auditDelete(String entityName, Long entityId, AuditLogContext auditLogContext) {
        // Create AuditLogEntity for the current audit operation
        createAuditLogEntity(entityName, entityId, auditLogContext, AuditOperation.DELETE);
    }

    /**
     * Persist a new AuditLogEntity into AuditLog DB
     */
    private AuditLogEntity createAuditLogEntity(String entityName, Long entityId, AuditLogContext auditLogContext, AuditOperation auditOperation) {
        // Create a new AuditLogEntity instance
        AuditLogEntity auditLogEntity = new AuditLogEntity(entityName, 
                                                           entityId, 
                                                           auditOperation, 
                                                           auditSource, 
                                                           auditLogContext.getUserType(),
                                                           auditLogContext.getUserId(),
                                                           auditLogContext.getUserName(),
                                                           auditLogContext.getUserEmail(),
                                                           getCurrentTransactionId());
        
        // Persist the new AuditLogEntity information
        auditLogEntity = auditLogManager.create(auditLogEntity);
        LOG.log(Level.FINE, "[AuditLog] AuitLogEntity persisted: {0}", auditLogEntity);
        
        return auditLogEntity;
    }
    
    /**
     * Persist a new AuditLogEntry into AuditLog DB
     */
    private AuditLogEntry createAuditLogEntry(AuditLogEntity auditLogEntity, AuditLogPropertyConfig auditLogPropertyConfig, Object oldPropertyValue, Object newPropertyValue) {
        // Create a new AuditLogEntry instance
        AuditLogEntry auditLogEntry = new AuditLogEntry(auditLogEntity, 
                                                        auditLogPropertyConfig.getKey(), 
                                                        auditLogPropertyConfig.getType(), 
                                                        oldPropertyValue, 
                                                        newPropertyValue);
        
        // Persist the new AuditLogEntry entity information
        auditLogEntry = auditLogManager.log(auditLogEntity, auditLogEntry);
        LOG.log(Level.FINE, "[AuditLog] AuditLogEntry persisted: {0}", auditLogEntry);
        
        return auditLogEntry;
    }

    /**
     * Method to get all values from the properties configured
     * @param o Object instance to get the values from the properties configured 
     * @param propertiesToWath Set of properties to watch
     * 
     * @return Map which contains the property name as key and the value of this property obtained from the object
     */
    protected Map<String, Object> getEntityPropertyValues(Object o, List<AuditLogPropertyConfig> propertiesToWath) {
        Map<String, Object> propertiesValues = new HashMap<String, Object>();
        for (AuditLogPropertyConfig auditLogPropertyConfig : propertiesToWath){
            try{
                Object propertyValue = getFieldValue(o, auditLogPropertyConfig);
                propertiesValues.put(auditLogPropertyConfig.getKey(), propertyValue);
            }catch(LazyInitializationException lazyException){
                LOG.log(Level.FINE, "[AuditLog] Property {0} of audited entity {1} has not been load, lazy-load, assumming it does not change.", 
                        new Object[]{auditLogPropertyConfig.getKey(), getObjectKey(o)});
            }
        }
        return propertiesValues;
    }

    /**
     * Method to get the value from a property in the instance object passed by parameter.
     * 
     * @param o Object instance to get the value
     * @param auditLogPropertyConfig Property configuration info
     * 
     * @return Value contained in the entity property
     */
    private Object getFieldValue(Object o, AuditLogPropertyConfig auditLogPropertyConfig) {
        Object value = null;
        
        try{
            value = PropertyUtils.getProperty(o, auditLogPropertyConfig.getPropertyName());
            
            if (auditLogPropertyConfig.hasNestedProperty()){
                value = getNestedPropertyFieldValue(auditLogPropertyConfig, value);
            }
        }catch(NestedNullException nne){
            LOG.log(Level.FINE, "[AuditLog] Property {0} of audited entity {1} has null value", 
                    new Object[]{auditLogPropertyConfig.getKey(), getObjectKey(o)});
        }catch(InvocationTargetException ite){
            if (ite.getCause() instanceof LazyInitializationException){
                throw (LazyInitializationException) ite.getCause();
            }else{
                throw new AuditLogException("[AuditLog] Property " + auditLogPropertyConfig.getKey() + " of " + getObjectClass(o).getName() + " can not be audited, accessing error.", ite);
            }
        }catch(IllegalAccessException iae){
            throw new AuditLogException("[AuditLog] Property " + auditLogPropertyConfig.getKey() + " of " + getObjectClass(o).getName() + " can not be audited, it is not readable. Verify java access modifier for this property.", iae);
        }catch(NoSuchMethodException nsme){
            throw new AuditLogException("[AuditLog] Property " + auditLogPropertyConfig.getKey() + " of " + getObjectClass(o).getName() + " can not be audited, it does not have an accessor method. Verify the property has a well defined \"get\" method for this property", nsme);
        }
        
        return value;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private Object getNestedPropertyFieldValue(AuditLogPropertyConfig auditLogPropertyConfig, Object value) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        Object returnValue = null;
        if (value instanceof Collection){
            List values = new ArrayList(((Collection) value).size());
            for (Object collectionObject : (Collection) value){
                values.add(PropertyUtils.getProperty(collectionObject, auditLogPropertyConfig.getNestedPropertyName()));
            }
            if (!values.isEmpty()){
                returnValue = values;
            }
        }
        return returnValue;
    }
    
    /**
     * Return the AuditLogContext for the current thread. 
     * This method creates it if it does not exist.
     */
    public AuditLogContext getCurrentContext() {
        AuditLogContext auditLogContext = tlAuditLogContext.get();
        if (auditLogContext == null) {
            auditLogContext = new AuditLogContext(null, null);
            tlAuditLogContext.set(auditLogContext);
        }
        return auditLogContext;
    }
    
    public void setContextInfo(User user, AdfonicUser adfonicUser){
        AuditLogContext auditLogContext = null;
        if ((user!=null)||(adfonicUser!=null)){
            auditLogContext = new AuditLogContext(user, adfonicUser);
        }
        tlAuditLogContext.set(auditLogContext);            
    }
    
    public void setContextInfo(Partner partner){
        AuditLogContext auditLogContext = null;
        if (partner!=null){
            auditLogContext = new AuditLogContext(partner);
        }
        tlAuditLogContext.set(auditLogContext);         
    }
    
    public void setContextInfo(System system){
        AuditLogContext auditLogContext = null;
        if (system!=null){
            auditLogContext = new AuditLogContext(system);
        }
        tlAuditLogContext.set(auditLogContext);         
    }
    
    public void cleanContextInfo(){
        AuditLogContext auditLogContext = tlAuditLogContext.get();
        if (auditLogContext!=null){
            auditLogContext.clean();
            tlAuditLogContext.set(null);
        }
    }
    
    /**
     * Get current values saved for the entity passed by parameters 
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object>  getCurrentEntityPropertiesValues(String objectKey) {
        Map<String, Object> oldPropertiesValues = null;
        
        synchronized (this.em) {
            oldPropertiesValues = (Map<String, Object>) getCurrentContext().getEntitiesCache().get(objectKey);
        }
        
        if (oldPropertiesValues==null){
            oldPropertiesValues = new HashMap<String, Object>();
        }
        
        return oldPropertiesValues;
    }
    
    /**
     * Builds object key to use on the currentValuesByPK 
     * since the object entity
     */
    private String getObjectKey(Object o){
        Long entityId = ((HasPrimaryKeyId) o).getId();
        String entityName = getObjectClass(o).getName();
        return getObjectKey(entityName, entityId);
    }
    
    /**
     * Builds object key to use on the currentValuesByPK 
     * using the entityName and its Id
     */
    private String getObjectKey(String entityName, Long entityId) {
        if ((entityName!=null)&&(entityId!=null)){
            return entityName + entityId;
        }else{
            return "";
        }
    }
    
    /**
     * Checks if two object have/contains the same values
     */
    @SuppressWarnings("rawtypes")
    private boolean haveSameValue(Object oldPropertyValue, Object newPropertyValue) {
        boolean result = false;
        if ((oldPropertyValue==null)&&(newPropertyValue==null)){
            result = true;
        }else if (((oldPropertyValue==null)&&(newPropertyValue!=null)) ||
                  ((oldPropertyValue!=null)&&(newPropertyValue==null))){
            result = false;
        }else if (oldPropertyValue instanceof Collection){
                result = CollectionUtils.isEqualCollection((Collection)oldPropertyValue, (Collection) newPropertyValue);
        }else if (oldPropertyValue instanceof BigDecimal){
            result = (((BigDecimal) oldPropertyValue).compareTo((BigDecimal) newPropertyValue)!=0?false:true);
        }else if (oldPropertyValue instanceof Date){
            result = (((((Date) oldPropertyValue).getTime() / DATE_MASK_MILISECONDS_PER_DAY) - (((Date) newPropertyValue).getTime() / DATE_MASK_MILISECONDS_PER_DAY))==0);  
        }else if (oldPropertyValue!=null){
            result = oldPropertyValue.equals(newPropertyValue);
        }else{
            result = false;
        }
            
        return result;
    }
    
    /**
     * Returns the Id for the current transaction
     */
    private String getCurrentTransactionId(){
        return ""; // MAD-2703 removes atomikos, spring does not have any alternative to retrieve a transaction id 
    }
    
    /**
     * Method to get the current entity without the hibernate proxy class
     * 
     * @param o Entity from which get the class
     * 
     * @return Entity class
     */
    @SuppressWarnings("rawtypes")
    private Class getObjectClass(Object o){
        return HibernateProxyHelper.getClassWithoutInitializingProxy(o);
    }
    
    // It has been discovered that some update operations related with ManyToMany 
    // relationships between entities do not call the JPA Hooks. This issue affect mainly the 
    // entity fields that are collections (Set, List, Map, etc.)   
    // To sort it out this issue it has been implemented a solution which requires 
    // to store object to be updated (addPendientUpdate()) before the update, perform 
    // the merge JPA operation, and if the merge was done successfully and the object update is still 
    // pendient, execute the pendient audit operation (executePendientUpdate()).
    //
    // See also: BusinessKeyDaoJpaImpl's preUpdateAuditLogActions() and postUpdateAuditLogActions() methods 
    public void addPendientUpdate(Object o){
        getCurrentContext().getPendientUpdates().put(getObjectKey(o), o);
        LOG.log(Level.FINE, "[AuditLog] {0} added to pendient updates collection in current context.", getObjectKey(o));
    }
    public void removePendientUpdate(Object o){
        // Copy to avoid concurrent modification, as onPostUpdate does removes
        if (getCurrentContext().getPendientUpdates().containsKey(getObjectKey(o))){
            LOG.log(Level.FINE, "[AuditLog] Executed pendient update for entity {0}", getObjectKey(o));
            audit(o, AuditOperation.UPDATE);
        }
    }
}
