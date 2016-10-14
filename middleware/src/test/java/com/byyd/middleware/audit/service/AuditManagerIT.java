package com.byyd.middleware.audit.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.adfonic.domain.Audit;
import com.adfonic.test.AbstractAdfonicTest;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/adfonic-springdata-hibernate-context.xml"})
@DirtiesContext
public class AuditManagerIT extends AbstractAdfonicTest{
    
    @Autowired
    private AuditManager auditManager;
    
    @Test
    public void testAudit() {
        Audit audit = auditManager.newAudit("test class", "test query");
        assertNotNull(audit);
        assertEquals("test class", audit.getClassName());
        assertEquals("test query", audit.getQuery());
    }
}
