package com.byyd.middleware.account.service;

import static org.junit.Assert.assertNotNull;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.adfonic.domain.Advertiser;
import com.adfonic.domain.AdvertiserCloudInformation;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/adfonic-springdata-hibernate-context.xml"})
@DirtiesContext
public class AdvertiserCloudManagerIT {
    
    private static final Logger LOG = Logger.getLogger(AdvertiserCloudManagerIT.class.getName());
    
    @Autowired
    private AdvertiserManager advertiserManager;
    
    @Autowired
    private AdvertiserCloudManager advertiserCloudManager;
    
    //----------------------------------------------------------------------------------------------------------------

    @Test
    public void testCreateAdvertiserCloudInformation(){
        Advertiser advertiser = advertiserManager.getAdvertiserById(1L);
        
        AdvertiserCloudInformation advertiserCloudInformation = advertiserCloudManager.createAdvertiserCloudInformation(advertiser);
        
        assertNotNull(advertiserCloudInformation);
        LOG.log(Level.INFO, "Credentials generated for user {0}: {1}", new Object[]{advertiser.getExternalID(), advertiserCloudInformation});
    }
    
    @Test
    public void testDeleteAdvertiserCloudInformation(){
        Advertiser advertiser = advertiserManager.getAdvertiserById(1L);
        
        advertiserCloudManager.deleteAdvertiserCloudInformation(advertiser);
    }
}
