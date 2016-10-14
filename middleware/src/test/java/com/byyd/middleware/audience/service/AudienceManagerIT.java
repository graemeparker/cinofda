package com.byyd.middleware.audience.service;

import static com.byyd.middleware.iface.dao.SortOrder.asc;
import static org.junit.Assert.fail;

import java.util.List;
import java.util.logging.Logger;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.adfonic.domain.Audience;
import com.adfonic.domain.Campaign;
import com.adfonic.domain.DMPAudience;
import com.adfonic.domain.DMPSelector;
import com.adfonic.test.AbstractAdfonicTest;
import com.byyd.middleware.iface.dao.SortOrder;
import com.byyd.middleware.iface.dao.Sorting;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/adfonic-springdata-hibernate-context.xml" })
@DirtiesContext
public class AudienceManagerIT extends AbstractAdfonicTest {

    private static final Logger LOG = Logger.getLogger(AudienceManagerIT.class.getName());

    @Autowired
    private AudienceManager audienceManager;

    // ----------------------------------------------------------------------------------------------------------

    @Test
    public void testDMPAudience() {
        try {
            DMPAudience audience = new DMPAudience();
            List<DMPSelector> selectors = audienceManager.getDMPSelectorsForDMPAudience(audience, new Sorting(SortOrder.asc(DMPSelector.class, "name")));
            for (DMPSelector selector : selectors) {
                LOG.info(selector.getName());
            }

        } catch (Exception e) {
            String stackTrace = ExceptionUtils.getStackTrace(e);
            LOG.info(stackTrace);
            fail(stackTrace);
        } finally {
        }

    }

    @Test
    public void testCampaignsLinkedToAudience() {
        try {
            Audience audience = new Audience();
            List<Campaign> list = audienceManager.getCampaignsLinkedToAudience(audience, new Sorting(asc(Campaign.class, "name")));
            for (Campaign campaign : list) {
                LOG.info(campaign.getName());
            }
        } catch (Exception e) {
            String stackTrace = ExceptionUtils.getStackTrace(e);
            LOG.info(stackTrace);
            fail(stackTrace);
        } finally {
        }

    }
}
