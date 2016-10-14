package com.adfonic.tasks.combined;

import java.util.LinkedHashSet;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.TypedQuery;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

import com.adfonic.domain.Advertiser;
import com.adfonic.domain.AdvertiserNotificationFlag;
import com.adfonic.domain.Campaign;
import com.adfonic.domain.CampaignNotificationFlag;
import com.adfonic.domain.NotificationFlag;
import com.adfonic.jms.JmsResource;
import com.adfonic.jms.JmsUtils;
import com.adfonic.jms.UnStopAdvertiserMessage;
import com.adfonic.jms.UnStopCampaignMessage;
import com.byyd.middleware.account.service.AdvertiserManager;
import com.byyd.middleware.campaign.service.CampaignManager;

/**
 * Deletes NotificationFlag entries that are past their expiration date.
 * These are harmless but tend to clog up the database.
 */
@Component
public class Unstopper {

    private final transient Logger LOG = LoggerFactory.getLogger(getClass().getName());

    @Autowired
    private AdvertiserManager advertiserManager;
    @Autowired
    private CampaignManager campaignManager;
    @Autowired
    @Qualifier("entityManagerFactory")
    private EntityManagerFactory emf;
    @Autowired
    private JmsUtils jmsUtils;
    @Autowired
    @Qualifier(JmsResource.CENTRAL_JMS_TEMPLATE)
    private JmsTemplate centralJmsTemplate;

    //@Scheduled(fixedRate=10000)
    public void handleUnstoppages() {
        LOG.debug("Handling unstoppages");

        // Build the unique set in case there are dups
        Set<Advertiser> advertisers = new LinkedHashSet<Advertiser>();
        Set<Campaign> campaigns = new LinkedHashSet<Campaign>();

        EntityManager em = emf.createEntityManager();
        try {
            // Look for unstoppages
            TypedQuery<AdvertiserNotificationFlag> query1 = em.createQuery("select n from AdvertiserNotificationFlag n where n.type = ?1 order by n.createDate asc",
                    AdvertiserNotificationFlag.class);
            query1.setParameter(1, NotificationFlag.Type.UNSTOP);
            for (AdvertiserNotificationFlag nf : query1.getResultList()) {
                advertisers.add(nf.getAdvertiser());
                advertiserManager.delete(nf);
            }

            TypedQuery<CampaignNotificationFlag> query2 = em.createQuery("select n from CampaignNotificationFlag n where n.type = ?1 order by n.createDate asc",
                    CampaignNotificationFlag.class);
            query2.setParameter(1, NotificationFlag.Type.UNSTOP);
            for (CampaignNotificationFlag nf : query2.getResultList()) {
                campaigns.add(nf.getCampaign());
                campaignManager.delete(nf);
            }
        } finally {
            em.close();
        }

        // Unstop 'em
        for (final Advertiser advertiser : advertisers) {
            LOG.info("Unstopping Advertiser id={}", advertiser.getId());
            jmsUtils.sendObject(centralJmsTemplate, JmsResource.UNSTOP_ADVERTISER_TOPIC, new UnStopAdvertiserMessage(advertiser.getId()));
        }
        for (final Campaign campaign : campaigns) {
            LOG.info("Unstopping Campaign id={}", campaign.getId());
            jmsUtils.sendObject(centralJmsTemplate, JmsResource.UNSTOP_CAMPAIGN_TOPIC, new UnStopCampaignMessage(campaign.getId()));
        }

        LOG.debug("Finished handling unstoppages");
    }
}
