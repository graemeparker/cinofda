package com.byyd.middleware.audience.dao.jpa;

import org.springframework.stereotype.Repository;

import com.adfonic.domain.DMPAudience;
import com.byyd.middleware.audience.dao.DMPAudienceDao;
import com.byyd.middleware.iface.dao.jpa.BusinessKeyDaoJpaImpl;

@Repository
public class DMPAudienceDaoJpaImpl extends BusinessKeyDaoJpaImpl<DMPAudience> implements DMPAudienceDao {

}
