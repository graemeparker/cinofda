package com.byyd.middleware.campaign.dao.jpa;

import org.springframework.stereotype.Repository;

import com.adfonic.domain.CampaignNotificationFlag;
import com.byyd.middleware.campaign.dao.CampaignNotificationFlagDao;
import com.byyd.middleware.iface.dao.jpa.BusinessKeyDaoJpaImpl;

@Repository
public class CampaignNotificationFlagDaoJpaImpl extends BusinessKeyDaoJpaImpl<CampaignNotificationFlag> implements CampaignNotificationFlagDao {

}
