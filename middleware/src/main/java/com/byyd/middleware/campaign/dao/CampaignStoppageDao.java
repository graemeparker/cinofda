package com.byyd.middleware.campaign.dao;

import java.util.Date;
import java.util.List;

import com.adfonic.domain.Campaign;
import com.adfonic.domain.CampaignStoppage;
import com.byyd.middleware.iface.dao.BusinessKeyDao;
import com.byyd.middleware.iface.dao.FetchStrategy;

public interface CampaignStoppageDao extends BusinessKeyDao<CampaignStoppage> {

    List<Object[]> getFieldsForNullOrFutureReactivateDate();
    List<CampaignStoppage> getAllForReactivateDateIsNullOrReactivateDateGreaterThan(Date reactivateDate, FetchStrategy... fetchStrategy);
    List<CampaignStoppage> getAllForCampaignAndReactivateDateIsNullOrReactivateDateGreaterThan(Campaign campaign, Date reactivateDate, FetchStrategy... fetchStrategy);


}
