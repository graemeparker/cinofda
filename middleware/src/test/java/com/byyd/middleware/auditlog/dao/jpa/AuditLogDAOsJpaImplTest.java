package com.byyd.middleware.auditlog.dao.jpa;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import com.adfonic.domain.Campaign;
import com.adfonic.domain.auditlog.AuditLogEntity;
import com.adfonic.domain.auditlog.AuditLogEntity.AuditOperation;
import com.adfonic.domain.auditlog.AuditLogEntity.UserType;
import com.adfonic.domain.auditlog.AuditLogEntry;
import com.adfonic.domain.auditlog.AuditLogEntry.AuditLogEntryType;
import com.adfonic.test.AbstractAdfonicTest;
import com.byyd.middleware.auditlog.dao.AuditLogEntityDao;
import com.byyd.middleware.auditlog.dao.AuditLogEntryDao;
import com.byyd.middleware.auditlog.filter.AuditEntityInformation;
import com.byyd.middleware.auditlog.filter.AuditLogEntityFilter;
import com.byyd.middleware.auditlog.filter.AuditLogEntryFilter;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/spring-context-auditlog-tests-configuration.xml"})
public class AuditLogDAOsJpaImplTest extends AbstractAdfonicTest {
    
    @Autowired(required=false)
    private AuditLogEntityDao<AuditLogEntity, AuditLogEntityFilter> auditLogEntityDao;
    @Autowired(required=false)
    private AuditLogEntryDao<AuditLogEntry, AuditLogEntryFilter> auditLogEntryDao;
    
    // Test data
    private static final String VALUE_ENTITY_NAME                 = Campaign.class.getName();
    private static final Long VALUE_ENTITY_ID                     = 9999L;
    private static final AuditOperation VALUE_AUDIT_OPERATION     = AuditOperation.UPDATE;
    private static final String VALUE_ENTITY_SOURCE               = "middleware_tests";
    private static final UserType VALUE_ENTITY_USER_TYPE          = UserType.ADFONIC_USER;
    private static final long VALUE_ENTITY_USER_ID                = 8888L;
    private static final String VALUE_ENTITY_USER_NAME            = "User Name Test";
    private static final String VALUE_ENTITY_USER_EMAIL           = "user.email@test.com";
    private static final String VALUE_ENTITY_TRANSACTION_ID       = "middlewareTidTest";
    
    private static final String VALUE_ENTRY_NAME                  = "propery.name.path";
    private static final AuditLogEntryType VALUE_ENTRY_TYPE       = AuditLogEntryType.VARCHAR;
    private static final long VALUE_ENTRY_OLD_VALUE_INT           = 6666L;
    private static final long VALUE_ENTRY_NEW_VALUE_INT           = 5555L;
    private static final BigDecimal VALUE_ENTRY_OLD_VALUE_DECIMAL = new BigDecimal(4444);
    private static final BigDecimal VALUE_ENTRY_NEW_VALUE_DECIMAL = new BigDecimal(3333);
    private static final Date VALUE_ENTRY_OLD_VALUE_DATE          = new Date();
    private static final Date VALUE_ENTRY_NEW_VALUE_DATE          = new Date();
    private static final String VALUE_ENTRY_OLD_VALUE_VARCHAR     = "VALUE_ENTRY_OLD_VALUE_VARCHAR";
    private static final String VALUE_ENTRY_NEW_VALUE_VARCHAR     = "VALUE_ENTRY_NEW_VALUE_VARCHAR";
    private static final Boolean VALUE_ENTRY_OLD_VALUE_BOOLEAN    = true;
    private static final Boolean VALUE_ENTRY_NEW_VALUE_BOOLEAN    = true;
    private static final String VALUE_ENTRY_OLD_VALUE_BLOB        = "VALUE_ENTRY_OLD_VALUE_BLOB";
    private static final String VALUE_ENTRY_NEW_VALUE_BLOB        = "VALUE_ENTRY_NEW_VALUE_BLOB";
    
    @Test
    public void testAuditDAOsConfiguration(){
        assertNotNull("AuditLogEntityDao is not well configured in spring context (autowired annotation failed)", auditLogEntityDao);
        assertNotNull("AuditLogEntryDao is not well configured in spring context (autowired annotation failed)", auditLogEntryDao);
    }
    
    @Test
    @Transactional(readOnly=false)
    public void testCreateAuditJPAEntities() {
        // Create AuditLogEntity
        AuditLogEntity auditLogEntity = newAuditLogEntity();
        assertTrue(auditLogEntity.getId() == 0L);
        auditLogEntity = auditLogEntityDao.create(auditLogEntity);
        assertNotNull(auditLogEntity);
        assertTrue("AuditLogEntity entity not persisted (id = 0)", auditLogEntity.getId() > 0L);
        assertNotNull(auditLogEntity.getTimestamp());
        assertEquals(auditLogEntity.getEntityName(), VALUE_ENTITY_NAME);
        assertEquals(new Long(auditLogEntity.getEntityId()), VALUE_ENTITY_ID);
        
        // Create AuditLogEntry
        AuditLogEntry auditLogEntry = newAuditLogEntry(auditLogEntity);
        assertTrue(auditLogEntry.getId() == 0L);
        auditLogEntry = auditLogEntryDao.create(auditLogEntry);
        assertNotNull(auditLogEntry);
        assertTrue("AuditLogEntry entity not persisted (id = 0)", auditLogEntry.getId() > 0L);
    }
    
    @Test
    @Transactional(readOnly=true)
    public void testGetAuditJPAEntities(){
        // AuditLogEntity
        AuditLogEntity auditLogEntity = newAuditLogEntity();
        auditLogEntity = auditLogEntityDao.create(auditLogEntity);
        // -- Get by Id
        AuditLogEntity persistedAuditLogEntity = auditLogEntityDao.getById(auditLogEntity.getId());
        assertNotNull("GetById for AuditLogEntity does not return any row", persistedAuditLogEntity);
        // -- Get All
        List<AuditLogEntity> auditLogEntities = auditLogEntityDao.getAll();
        assertNotNull("GetAll for AuditLogEntity does not return any row", auditLogEntities);
        // -- Get All filtered
        AuditLogEntityFilter auditLogEntityFilter = new AuditLogEntityFilter();
        Collection<AuditEntityInformation> auditEntitiesInformation = new ArrayList<AuditEntityInformation>();
        auditEntitiesInformation.add(new AuditEntityInformation(auditLogEntity.getEntityName(), auditLogEntity.getEntityId()));
        auditLogEntityFilter.setAuditEntitiesInformation(auditEntitiesInformation);
        auditLogEntityFilter.setUserType(auditLogEntity.getUserType());
        auditLogEntityFilter.setUserId(auditLogEntity.getUserId());
        auditLogEntityFilter.setUserName(auditLogEntity.getUserName());
        auditLogEntityFilter.setUserEmail(auditLogEntity.getUserEmail());
        auditLogEntityFilter.setSource(auditLogEntity.getSource());
        auditLogEntityFilter.setFromDate(auditLogEntity.getTimestamp());
        auditLogEntities = auditLogEntityDao.getAll(auditLogEntityFilter);
        assertNotNull("GetAll using  filter for AuditLogEntity does not return any row", auditLogEntities);
        assertEquals("GetAll using  filter for AuditLogEntity does not return the expected entity", auditLogEntity.getId(), auditLogEntities.get(0).getId());
        
        // AuditLogEntry
        AuditLogEntry auditLogEntry = newAuditLogEntry(auditLogEntity);
        auditLogEntry = auditLogEntryDao.create(auditLogEntry);
        // -- Get by Id
        AuditLogEntry persistedAuditLogEntry = auditLogEntryDao.getById(auditLogEntry.getId());
        assertNotNull("GetById for AuditLogEntry does not return any row", persistedAuditLogEntry);
        // -- Get All
        List<AuditLogEntry> auditLogEntries = auditLogEntryDao.getAll();
        assertNotNull("GetAll for AuditLogEntry does not return any row", auditLogEntries);
        // -- Get All filtered
        AuditLogEntryFilter auditLogEntryFilter = new AuditLogEntryFilter();
        Collection<AuditLogEntryType> auditLogEntryTypes = new HashSet<AuditLogEntryType>();
        auditLogEntryTypes.add(auditLogEntry.getAuditLogEntryType());
        auditLogEntryFilter.setAuditLogEntryTypes(auditLogEntryTypes);
        auditLogEntries = auditLogEntryDao.getAll(auditLogEntryFilter);
        assertNotNull("GetAll using  filter for AuditLogEntry does not return any row", auditLogEntries);
        assertTrue("GetAll using  filter for AuditLogEntry does not return the expected entity", auditLogEntries.contains(auditLogEntry));
    }
    
    
    public static AuditLogEntity newAuditLogEntity() {
        return new AuditLogEntity(VALUE_ENTITY_NAME, VALUE_ENTITY_ID, VALUE_AUDIT_OPERATION, VALUE_ENTITY_SOURCE, VALUE_ENTITY_USER_TYPE, 
                                  VALUE_ENTITY_USER_ID, VALUE_ENTITY_USER_NAME, VALUE_ENTITY_USER_EMAIL, VALUE_ENTITY_TRANSACTION_ID);
    }
    
    public static AuditLogEntry newAuditLogEntry(AuditLogEntity auditLogEntity) {
        AuditLogEntry auditLogEntry = new AuditLogEntry();
        auditLogEntry.setName(VALUE_ENTRY_NAME);
        auditLogEntry.setAuditLogEntity(auditLogEntity);
        auditLogEntry.setAuditLogEntryType(VALUE_ENTRY_TYPE);
        auditLogEntry.setOldValueInt(VALUE_ENTRY_OLD_VALUE_INT);
        auditLogEntry.setNewValueInt(VALUE_ENTRY_NEW_VALUE_INT);
        auditLogEntry.setOldValueDecimal(VALUE_ENTRY_OLD_VALUE_DECIMAL);
        auditLogEntry.setNewValueDecimal(VALUE_ENTRY_NEW_VALUE_DECIMAL);
        auditLogEntry.setOldValueDate(VALUE_ENTRY_OLD_VALUE_DATE);
        auditLogEntry.setNewValueDate(VALUE_ENTRY_NEW_VALUE_DATE);
        auditLogEntry.setOldValueVarchar(VALUE_ENTRY_OLD_VALUE_VARCHAR);
        auditLogEntry.setNewValueVarchar(VALUE_ENTRY_NEW_VALUE_VARCHAR);
        auditLogEntry.setOldValueBoolean(VALUE_ENTRY_OLD_VALUE_BOOLEAN);
        auditLogEntry.setNewValueBoolean(VALUE_ENTRY_NEW_VALUE_BOOLEAN);
        auditLogEntry.setOldValueBlob(VALUE_ENTRY_OLD_VALUE_BLOB);
        auditLogEntry.setNewValueBlob(VALUE_ENTRY_NEW_VALUE_BLOB);
        return auditLogEntry;
    }
}
