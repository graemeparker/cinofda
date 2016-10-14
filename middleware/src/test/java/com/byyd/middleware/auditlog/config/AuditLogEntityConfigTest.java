package com.byyd.middleware.auditlog.config;

import static org.junit.Assert.assertNotNull;

import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.adfonic.test.AbstractAdfonicTest;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/spring-context-auditlog-tests-configuration.xml"})
public class AuditLogEntityConfigTest extends AbstractAdfonicTest {
    
    private static final transient Logger LOG = Logger.getLogger(AuditLogEntityConfigTest.class.getName());
    
    @Autowired(required=false)
    private AuditLogConfig auditLogConfig;
    
    @Test
    public void testAuditDAOsConfiguration(){
        assertNotNull("AuditLogConfig is not well configured in spring context (autowired annotation failed)", auditLogConfig);
        
        Map<String, AuditLogEntityConfig> auditedEntities = auditLogConfig.getAuditedEntities();
        assertNotNull("auditedEntities Map is null", auditedEntities);
        
        StringBuffer sbLog = new StringBuffer("\nAuditLog configuration read:");
        AuditLogEntityConfig auditLogEntityConfig = null;
        for(String entityKey : auditedEntities.keySet()){
            sbLog.append("\n\tEntity " + entityKey);
            auditLogEntityConfig = auditedEntities.get(entityKey);
            assertNotNull("AuditLogEntityConfig is null for the entity with key " + entityKey, auditedEntities);
            List<AuditLogPropertyConfig> auditedProperties= auditLogEntityConfig.getAuditedProperties();
            for(AuditLogPropertyConfig auditLogPropertyConfig : auditedProperties){
                assertNotNull("One AuditLogPropertyConfig is null for the entity with key " + entityKey, auditLogPropertyConfig);
                assertNotNull("One AuditLogPropertyConfig has name field with null value for the entity with key " + entityKey, auditLogPropertyConfig.getKey());
                assertNotNull("One AuditLogPropertyConfig has type field with null value for the entity with key " + entityKey, auditLogPropertyConfig.getType());
                sbLog.append("\n\t\t" + auditLogPropertyConfig.getKey() + " as " + auditLogPropertyConfig.getType());
            }
        }
        LOG.info(sbLog.toString());
        
    }
}
