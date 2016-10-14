package com.byyd.middleware.integrations.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.adfonic.domain.AdserverPlugin;
import com.adfonic.domain.AdserverStatus;
import com.adfonic.domain.AdserverStatus_;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.jpa.FetchStrategyBuilder;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/adfonic-springdata-hibernate-context.xml"})
@DirtiesContext
public class IntegrationsManagerIT {
    @Autowired
    IntegrationsManager integrationManager;
    
    //----------------------------------------------------------------------------------------------------------------

    @Test
    public void testAdserverPlugin() {
        String name = "Testing" + System.currentTimeMillis();
        String systemName = "st" + System.currentTimeMillis();
        boolean enabled = true;
        long expectedResponseTimeMillis = 500L;
        AdserverPlugin adserverPlugin = null;
        try {
            adserverPlugin = integrationManager.newAdserverPlugin(name, systemName, enabled, expectedResponseTimeMillis);
            assertNotNull(adserverPlugin);
            assertTrue(adserverPlugin.getId() > 0);

            assertEquals(adserverPlugin, integrationManager.getAdserverPluginById(adserverPlugin.getId()));
            assertEquals(adserverPlugin, integrationManager.getAdserverPluginById(Long.toString(adserverPlugin.getId())));

            String newSystemName = systemName + "Changed";
            adserverPlugin.setSystemName(newSystemName);
            adserverPlugin = integrationManager.update(adserverPlugin);

            adserverPlugin = integrationManager.getAdserverPluginById(adserverPlugin.getId());
            assertEquals(adserverPlugin.getSystemName(), newSystemName);

            List<AdserverPlugin> list = integrationManager.getAllAdserverPlugins();
            assertNotNull(list);
            assertTrue(list.size() > 0);
            assertTrue(list.contains(adserverPlugin));

        } catch(Exception e) {
            String stackTrace = ExceptionUtils.getStackTrace(e);
            System.out.println(stackTrace);
            fail(stackTrace);
        } finally {
            integrationManager.delete(adserverPlugin);
            assertNull(integrationManager.getAdserverPluginById(adserverPlugin.getId()));
        }
    }
    
    //----------------------------------------------------------------------------------------------------------------

    @Test
    public void testLoadAdserverStatuses() {
        try {
            FetchStrategy SHARD_FS = new FetchStrategyBuilder()
            .addInner(AdserverStatus_.shard)
            .build();
            
            List<AdserverStatus> adserverStatuses = integrationManager.getAllStatuses(SHARD_FS);
            for(AdserverStatus status : adserverStatuses) {
                System.out.println(status.getName() + ": " + status.getDescription() + " - " + status.getShard().getName());
            }
            
        } catch (Exception e) {
            String stackTrace = ExceptionUtils.getStackTrace(e);
            System.out.println(stackTrace);
            fail(stackTrace);
        } finally {
        }
    }

}
