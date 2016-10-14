package com.adfonic.tasks.combined;

import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.TemporalType;
import javax.persistence.TypedQuery;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.adfonic.domain.Campaign;
import com.adfonic.domain.CampaignNotificationFlag;
import com.adfonic.domain.CompanyMessage;
import com.byyd.middleware.account.service.CompanyManager;
import com.byyd.middleware.auditlog.listener.AuditLogJpaListener;
import com.byyd.middleware.campaign.service.CampaignManager;

/**
 * Various tasks pertaining to scheduled campaigns.
 */
@Component
public class ScheduledCampaignTasks {

    private final transient Logger LOG = LoggerFactory.getLogger(getClass().getName());

    @Autowired
    private CompanyManager companyManager;
    
    @Autowired
    private CampaignManager campaignManager;
    
    @Autowired
    @Qualifier("entityManagerFactory")
    private EntityManagerFactory emf;
    
    @Autowired
    private AuditLogJpaListener auditLogJpaListener;
    
    com.byyd.middleware.auditlog.listener.System system = new com.byyd.middleware.auditlog.listener.System(getClass().getName());

    /**
     * Updates each Campaign's status to COMPLETED once its endDate has passed,
     * and also creates a CompanyMessage as an indicator.
     */
    //@Scheduled(fixedRate=300800)
    public void doCompleteScheduledCampaigns() {
        LOG.debug("Completing scheduled campaigns");
        
        LOG.trace("Setting auditLog credentials");
        auditLogJpaListener.setContextInfo(system);
        
        completeScheduledCampaigns();
        
        LOG.trace("Removing auditLog credentials");
        auditLogJpaListener.cleanContextInfo();
        
        LOG.debug("Finished completing scheduled campaigns");
    }
    
    private void completeScheduledCampaigns() {

        EntityManager em = emf.createEntityManager();
        try {
            TypedQuery<Campaign> query = em.createQuery("select c from Campaign c where (c.status = ?1 or c.status = ?2) and c.endDate <= ?3 order by c.endDate asc",
                    Campaign.class);
            query.setParameter(1, Campaign.Status.ACTIVE);
            query.setParameter(2, Campaign.Status.PAUSED);
            query.setParameter(3, new Date(), TemporalType.TIMESTAMP);
            for (Campaign campaign : query.getResultList()) {
                LOG.info("Completing Campaign id={}, status={}, endDate={}", campaign.getId(), campaign.getStatus(), campaign.getEndDate());
                try {
                    // TODO: maybe move these into a manager as a single transactional unit
                    campaign.setStatus(Campaign.Status.COMPLETED);
                    campaign = campaignManager.update(campaign);

                    companyManager.newCompanyMessage(campaign, "campaign.completed");
                } catch (Exception e) {
                    LOG.error("Unable to complete Campaign id={} {}", campaign.getId(), e);
                }
            }
        } finally {
            em.close();
        }
        
    }

    /**
     * Notify advertisers about scheduled campaigns that have gone live.
     */
    //@Scheduled(fixedRate=300900)
    public void doNotifyLiveCampaigns() {
        LOG.debug("Notifying of live campaigns");
        
        LOG.trace("Setting auditLog credentials");
        auditLogJpaListener.setContextInfo(system);
        
        notifyLiveCampaigns();
        
        LOG.trace("Removing auditLog credentials");
        auditLogJpaListener.cleanContextInfo();
        
        LOG.debug("Finished notifying of live campaigns");
    }
    
    private void notifyLiveCampaigns() {
        
        EntityManager em = emf.createEntityManager();
        try {
            // Look for campaigns marked ACTIVE, whose startDate has passed,
            // for which we haven't yet sent the "campaign went live"
            // message -- which is determined by the absence of a
            // CampaignNotificationFlag of the respective type.
            for (Campaign campaign : (List<Campaign>) em.createNativeQuery(
                    "SELECT c.*" + " FROM CAMPAIGN c" + " WHERE c.STATUS='ACTIVE'" + " AND (c.START_DATE IS NULL OR c.START_DATE < CURRENT_TIMESTAMP)" + " AND NOT EXISTS ("
                            + "SELECT 1 FROM NOTIFICATION_FLAG nf" + " WHERE nf.DISCRIMINATOR='CAMPAIGN'" + " AND nf.CAMPAIGN_ID=c.ID" + " AND nf.TYPE='WENT_LIVE'" + ")",
                    Campaign.class).getResultList()) {
                LOG.info("Notifying for Campaign id={}", campaign.getId());

                try {
                    // TODO: maybe move these into a manager as a single transactional unit

                    // Create the NotificationFlag so it doesn't get picked up next time
                    campaignManager.newCampaignNotificationFlag(campaign, CampaignNotificationFlag.Type.WENT_LIVE, null); // never expire

                    // Make the message available to the company
                    CompanyMessage cm = new CompanyMessage(campaign, "campaign.live"); // This ought to be an enum?
                    cm.setArg0(campaign.getName());
                    companyManager.create(cm);
                } catch (Exception e) {
                    LOG.error("Exception caught while handling Campaign id={} {}", campaign.getId(), e);
                }
            }
        } finally {
            em.close();
        }
        
    }
}
