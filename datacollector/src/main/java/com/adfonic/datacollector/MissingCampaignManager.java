package com.adfonic.datacollector;

import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Resource;

import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.adfonic.datacollector.dao.ToolsDao;
import com.adfonic.domain.cache.dto.datacollector.campaign.CampaignDto;

/**
 * Manager of missing campaigns
 */
@Component
public class MissingCampaignManager {

    private static final transient Logger LOG = Logger.getLogger(MissingCampaignManager.class.getName());

    @Autowired
    private ToolsDao toolsDao;
    @Resource(name = "missingCampaignsCache")
    private Ehcache missingCampaignsCache;

    @Async(value = "missingCampaignExecutor")
    public void fetchMissingCampaign(long campaignId) {
        if (LOG.isLoggable(Level.INFO)) {
            LOG.info("Fetching a missing campaign with Campaign Id: " + campaignId);
        }
        if (missingCampaignsCache != null) {
            CampaignDto campaign = null;
            try {
                campaign = toolsDao.loadCampaign(campaignId);
                if (campaign != null) {
                    missingCampaignsCache.put(new Element(campaign.getId(), campaign));
                }
            } catch (SQLException e) {
                // TODO Auto-generated catch block
            }
        }
    }
}
