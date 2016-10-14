package com.byyd.middleware.iface.dao;

import static org.junit.Assert.fail;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.adfonic.domain.Campaign;
import com.byyd.middleware.campaign.service.CampaignManager;
import com.byyd.middleware.utils.AdfonicBeanDispatcher;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/adfonic-multiple-ds-context.xml"})
@DirtiesContext
public class MultipleDataSourcesIT {
    
    @Test
    public void testMultipleDataSources() {
        try {
            CampaignManager campaignManager = (CampaignManager)AdfonicBeanDispatcher.getBean("campaignManager");
            CampaignManager campaignManagerRO = (CampaignManager)AdfonicBeanDispatcher.getBean("campaignManagerRO");
            
            System.out.println("Getting campaign1");
            Campaign campaign1 = campaignManager.getCampaignById(1L);
            System.out.println("Campaign1: " + campaign1.getName());
            
            System.out.println("Getting campaign2");
            Campaign campaign2 = campaignManagerRO.getCampaignById(1L);
            System.out.println("Campaign2: " + campaign2.getName());
        } catch(Exception e) {
            String stackTrace = ExceptionUtils.getStackTrace(e);
            System.out.println(stackTrace);
            fail(stackTrace);
        } finally {
        }
        
    }

}
