package com.byyd.middleware.campaign.dao;

import com.adfonic.domain.Campaign;
import com.adfonic.domain.CampaignInternalLog;
import com.byyd.middleware.iface.dao.BusinessKeyDao;
import com.byyd.middleware.iface.dao.FetchStrategy;

public interface CampaignInternalLogDao extends BusinessKeyDao<CampaignInternalLog>  {
    
    CampaignInternalLog getByCampaign(Campaign campaign, FetchStrategy... fetchStrategy);
}
