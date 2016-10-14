package com.byyd.middleware.auditlog.service;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.adfonic.domain.auditlog.AuditLogEntity;
import com.adfonic.domain.auditlog.AuditLogEntry;
import com.byyd.middleware.auditlog.dao.jpa.AuditLogDAOsJpaImplTest;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/spring-context-auditlog-tests-configuration.xml"})
@DirtiesContext
public class AuditLogManagerIT {
    @Autowired
    private AuditLogManager auditLogManager;
   
    @Test
    public void testLog() {
        AuditLogEntity auditLogEntity2 = AuditLogDAOsJpaImplTest.newAuditLogEntity();
        AuditLogEntry auditLogEntry2 = AuditLogDAOsJpaImplTest.newAuditLogEntry(auditLogEntity2);
        auditLogEntry2 = auditLogManager.log(auditLogEntity2, auditLogEntry2);
        assertAuditLogEntry(auditLogEntry2);
    }

    private void assertAuditLogEntry(AuditLogEntry auditLogEntry) {
        assertNotNull(auditLogEntry);
        assertTrue("AuditLogEntry entity not persisted (id = 0)", auditLogEntry.getId() > 0L);
        AuditLogEntity auditLogEntity = auditLogEntry.getAuditLogEntity();
        assertNotNull(auditLogEntity);
        assertTrue("AuditLogEntity entity not persisted (id = 0)", auditLogEntity.getId() > 0L);
    }
}
