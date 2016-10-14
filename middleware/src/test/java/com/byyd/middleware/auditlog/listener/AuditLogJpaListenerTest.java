package com.byyd.middleware.auditlog.listener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.apache.commons.collections.MapIterator;
import org.apache.commons.collections.map.LRUMap;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import com.adfonic.domain.AdfonicUser;
import com.adfonic.domain.Advertiser;
import com.adfonic.domain.Campaign;
import com.adfonic.domain.Creative;
import com.adfonic.domain.Segment;
import com.adfonic.test.AbstractAdfonicTest;
import com.byyd.middleware.auditlog.config.AuditLogConfig;
import com.byyd.middleware.auditlog.config.AuditLogPropertyConfig;
import com.byyd.middleware.auditlog.exception.AuditLogException;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/spring-context-auditlog-tests-configuration.xml" })
public class AuditLogJpaListenerTest extends AbstractAdfonicTest {

    private static final transient Logger LOG = Logger.getLogger(AuditLogJpaListenerTest.class.getName());

    @Autowired
    AuditLogJpaListener auditLogJpaListener;

    @Autowired
    AuditLogConfig auditLogConfig;
    
    @Autowired
    private AuditLogTestService auditLogTestService;

    @Test
    @Transactional(readOnly = false)
    @Rollback(false)
    public void testGetEntityPropertyValues() throws AuditLogException {
        Campaign campaign = null;
        Segment segment = null;
        List<Creative> creatives = null;
        
        try{
            LRUMap currentValuesByPK = new LRUMap();
    
            // Setting context info
            AdfonicUser adfonicUser = auditLogTestService.getAdfonicUser();
            auditLogJpaListener.setContextInfo(null, adfonicUser);
    
            Advertiser advertiser = auditLogTestService.getAdvertiserInstance();
    
            // Campaign entity
            campaign = auditLogTestService.getCampaignInstance(advertiser);
            List<AuditLogPropertyConfig> campaignAuditLogPropertyConfig = auditLogConfig.getAuditLogPropertyConfig(Campaign.class.getName());
            currentValuesByPK.put(Campaign.class.getName(), auditLogJpaListener.getEntityPropertyValues(campaign, campaignAuditLogPropertyConfig));
    
            // Segment entity
            segment = auditLogTestService.getSegmentInstance(advertiser);
            List<AuditLogPropertyConfig> segmentAuditLogPropertyConfig = auditLogConfig.getAuditLogPropertyConfig(Segment.class.getName());
            currentValuesByPK.put(Segment.class.getName(), auditLogJpaListener.getEntityPropertyValues(segment, segmentAuditLogPropertyConfig));
    
            // Advertiser entity
            List<AuditLogPropertyConfig> advetiserAuditLogPropertyConfig = auditLogConfig.getAuditLogPropertyConfig(Advertiser.class.getName());
            currentValuesByPK.put(Advertiser.class.getName(), auditLogJpaListener.getEntityPropertyValues(advertiser, advetiserAuditLogPropertyConfig));
    
            // Creative entity
            Creative creative = auditLogTestService.getCreativeInstance(campaign, segment);
            creatives = new ArrayList<Creative>();
            creatives.add(creative);
            List<AuditLogPropertyConfig> creativeAuditLogPropertyConfig = auditLogConfig.getAuditLogPropertyConfig(Creative.class.getName());
            currentValuesByPK.put(Creative.class.getName(), auditLogJpaListener.getEntityPropertyValues(creative, creativeAuditLogPropertyConfig));
    
            // Show stored values
            showStoredPropertyValues(currentValuesByPK);
        }finally{
            // Delete the created new entities by this test
            try{
                auditLogTestService.deleteNewlyCreatedTestEntities(campaign, segment, creatives);
            }catch(Throwable t){
                t.printStackTrace();
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void showStoredPropertyValues(LRUMap currentValuesByPK) {
        StringBuffer sbOut = new StringBuffer();
        MapIterator mapIterator = currentValuesByPK.mapIterator();
        while (mapIterator.hasNext()) {
            mapIterator.next();
            Object key = mapIterator.getKey();
            sbOut.append("\n\t" + key + ":");
            Map<String, Object> propertiesValue = (Map<String, Object>) mapIterator.getValue();
            for (String propKey : propertiesValue.keySet()) {
                sbOut.append("\n\t\t" + propKey + " = " + propertiesValue.get(propKey));
            }
        }
        LOG.info(sbOut.toString());
    }

}
