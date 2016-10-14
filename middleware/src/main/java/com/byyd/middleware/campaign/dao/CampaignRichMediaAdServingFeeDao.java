package com.byyd.middleware.campaign.dao;

import java.util.List;

import com.adfonic.domain.Campaign;
import com.adfonic.domain.CampaignRichMediaAdServingFee;
import com.byyd.middleware.iface.dao.BusinessKeyDao;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.Pagination;
import com.byyd.middleware.iface.dao.Sorting;

public interface CampaignRichMediaAdServingFeeDao extends BusinessKeyDao<CampaignRichMediaAdServingFee> {
    
    Long countAllForCampaign(Campaign campaign);
    List<CampaignRichMediaAdServingFee> getAllForCampaign(Campaign campaign, FetchStrategy ... fetchStrategy);
    List<CampaignRichMediaAdServingFee> getAllForCampaign(Campaign campaign, Pagination page, FetchStrategy ... fetchStrategy);
    List<CampaignRichMediaAdServingFee> getAllForCampaign(Campaign campaign, Sorting sort, FetchStrategy ... fetchStrategy);

}
