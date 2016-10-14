package com.byyd.middleware.creative.service;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.adfonic.domain.AdSpace;
import com.adfonic.domain.Creative;
import com.byyd.middleware.creative.service.CreativeEligibilityManager;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/test-creative-eligibility-manager-context.xml"})
@DirtiesContext
public class CreativeEligibilityManagerIT {
    private static final transient Logger LOG = Logger.getLogger(CreativeEligibilityManagerIT.class.getName());

    @Autowired
    private EntityManagerFactory emf;
    @Autowired
    private CreativeEligibilityManager creativeEligibilityManager;

    @Test
    public void testIsCreativeEligible() {
        EntityManager em = emf.createEntityManager();
        try {
            assertTrue(checkCreativeAdSpaceEligibility(em, 9569, 8933)); // Mobile site (should be eligible)
            assertFalse(checkCreativeAdSpaceEligibility(em, 9569, 8968)); // Android app (not eligible)
            assertFalse(checkCreativeAdSpaceEligibility(em, 9569, 8966)); // iPhone app (not eligible)
            checkCreativeAdSpaceEligibility(em, 3, 1);
        } finally {
            em.close();
        }
    }

    private boolean checkCreativeAdSpaceEligibility(EntityManager em, long creativeId, long adSpaceId) {
        Creative creative = em.find(Creative.class, creativeId);
        if (creative == null) {
            LOG.warning("Database lacks required data, Creative id=" + creativeId + " not found");
            assumeTrue(false);
        }

        AdSpace adSpace = em.find(AdSpace.class, adSpaceId);
        if (adSpace == null) {
            LOG.warning("Database lacks required data, AdSpace id=" + adSpaceId + " not found");
            assumeTrue(false);
        }

        List<String> reasonsWhyNot = new ArrayList<String>();
        boolean eligible = creativeEligibilityManager.isCreativeEligible(creative, adSpace, reasonsWhyNot);
        System.out.println("*** Creative id=" + creative.getId() + " is " + (eligible ? "" : "NOT ") + "eligible for AdSpace id=" + adSpace.getId());
        for (String reason : reasonsWhyNot) {
            System.out.println("REASON: " + reason);
        }
        return eligible;
    }
}
