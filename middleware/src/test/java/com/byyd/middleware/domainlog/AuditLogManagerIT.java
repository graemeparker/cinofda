package com.byyd.middleware.domainlog;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.adfonic.domain.AdfonicUser;
import com.adfonic.domain.AuditLog;
import com.adfonic.domain.User;
import com.byyd.middleware.account.service.UserManager;
import com.byyd.middleware.domainlog.filter.AuditLogFilter;
import com.byyd.middleware.domainlog.service.AuditLogManager;
import com.byyd.middleware.iface.dao.FetchStrategyImpl;
import com.byyd.middleware.iface.dao.FetchStrategyImpl.JoinType;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/adfonic-springdata-hibernate-context.xml"})
@DirtiesContext
public class AuditLogManagerIT {
    @Autowired
    private AuditLogManager auditLogManager;
    @Autowired
    private UserManager userManager;

    @Test
    public void test() {
        String systemId = "system-" + System.currentTimeMillis();
        User user = userManager.getUserById(1L);
        AdfonicUser adfonicUser = userManager.getAdfonicUserById(1L);
        String objectId = System.currentTimeMillis() + "[OID]com.adfonic.domain.Company";
        String field = "field" + System.currentTimeMillis();
        String oldValue = "oldValue" + System.currentTimeMillis();
        String newValue = "newValue" + System.currentTimeMillis();

        // test create (bare minimum test)
        AuditLog auditLog = new AuditLog(systemId, user, adfonicUser, objectId, field, oldValue, newValue);
        auditLogManager.create(auditLog);
        assertTrue(auditLog.getId() > 0);

        // test countAll (bare minimum test)
        assertEquals(Long.valueOf(1), auditLogManager.countAll(new AuditLogFilter().systemId(systemId)));
        assertEquals(Long.valueOf(1), auditLogManager.countAll(new AuditLogFilter().objectId(objectId)));
        assertEquals(Long.valueOf(1), auditLogManager.countAll(new AuditLogFilter().objectId(objectId).field(field)));
        assertEquals(Long.valueOf(1), auditLogManager.countAll(new AuditLogFilter().systemId(systemId).field(field)));
        assertEquals(Long.valueOf(1), auditLogManager.countAll(new AuditLogFilter().systemId(systemId).objectId(objectId).field(field)));
        if (user != null) {
            assertEquals(Long.valueOf(1), auditLogManager.countAll(new AuditLogFilter().systemId(systemId).user(user).objectId(objectId).field(field)));
        }
        if (adfonicUser != null) {
            assertEquals(Long.valueOf(1), auditLogManager.countAll(new AuditLogFilter().systemId(systemId).adfonicUser(adfonicUser).objectId(objectId).field(field)));
        }

        // test getAll (bare minimum test) with no fetch strategy
        List<AuditLog> list = auditLogManager.getAll(new AuditLogFilter().systemId(systemId));
        assertEquals(1, list.size());
        assertEquals(auditLog.getId(), list.get(0).getId());
        assertEquals(systemId, list.get(0).getSystemId());
        assertEquals(objectId, list.get(0).getObjectId());

        // test with a fetch strategy
        FetchStrategyImpl auditLogFetchStrategy = new FetchStrategyImpl();
        auditLogFetchStrategy.addEagerlyLoadedFieldForClass(AuditLog.class, "user", JoinType.LEFT);
        auditLogFetchStrategy.addEagerlyLoadedFieldForClass(AuditLog.class, "adfonicUser", JoinType.LEFT);

        list = auditLogManager.getAll(new AuditLogFilter().systemId(systemId), auditLogFetchStrategy);
        assertEquals(1, list.size());
        assertEquals(auditLog.getId(), list.get(0).getId());
        assertEquals(systemId, list.get(0).getSystemId());
        assertEquals(objectId, list.get(0).getObjectId());
    }
}
