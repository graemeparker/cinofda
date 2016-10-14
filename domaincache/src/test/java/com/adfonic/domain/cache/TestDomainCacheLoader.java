package com.adfonic.domain.cache;

import static org.junit.Assert.*;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.adfonic.domain.cache.dto.adserver.PlatformDto;
import com.adfonic.domain.cache.dto.adserver.IntegrationTypeDto;

@Ignore("ignored because failing in Jenkins,ned to rewrite these test cases")
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:/test-domain-cache-loader-context.xml"})
public class TestDomainCacheLoader {
    private static final transient Logger LOG = Logger.getLogger(TestDomainCacheLoader.class.getName());

    @Autowired
    private DomainCacheLoader domainCacheLoader;

    @Test
    public void test() throws Exception {
        // Load a master DomainCache (all possible data)
        DomainCache domainCache = domainCacheLoader.loadDomainCache();
        
        assertNotNull(domainCache);
        
        domainCache.logCounts("",LOG, Level.INFO);

        // Test case to check the new platformsById field
        for (PlatformDto platform : domainCache.getPlatforms()) {
            assertNotNull(domainCache.getPlatformById(platform.getId()));
        }

        // Test case to check getAllIntegrationTypes
        for (IntegrationTypeDto integrationType : domainCache.getAllIntegrationTypes()) {
            assertNotNull(domainCache.getIntegrationTypeById(integrationType.getId()));
            assertNotNull(domainCache.getIntegrationTypeBySystemName(integrationType.getSystemName()));
            assertNotNull(integrationType.getSupportedFeatures());
            assertNotNull(integrationType.getSupportedBeaconModes());
        }
   }
}
