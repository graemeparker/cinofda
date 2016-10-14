package com.byyd.middleware.campaign.dao.jpa;

import org.springframework.stereotype.Repository;

import com.adfonic.domain.CampaignTargetCTR;
import com.byyd.middleware.campaign.dao.CampaignTargetCTRDao;
import com.byyd.middleware.iface.dao.jpa.BusinessKeyDaoJpaImpl;

@Repository
public class CampaignTargetCTRDaoJpaImpl extends BusinessKeyDaoJpaImpl<CampaignTargetCTR> implements CampaignTargetCTRDao {

}
