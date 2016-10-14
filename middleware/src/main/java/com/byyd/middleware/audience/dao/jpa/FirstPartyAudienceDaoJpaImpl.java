package com.byyd.middleware.audience.dao.jpa;

import org.springframework.stereotype.Repository;

import com.adfonic.domain.FirstPartyAudience;
import com.byyd.middleware.audience.dao.FirstPartyAudienceDao;
import com.byyd.middleware.iface.dao.jpa.BusinessKeyDaoJpaImpl;

@Repository
public class FirstPartyAudienceDaoJpaImpl extends BusinessKeyDaoJpaImpl<FirstPartyAudience> implements FirstPartyAudienceDao {

}
