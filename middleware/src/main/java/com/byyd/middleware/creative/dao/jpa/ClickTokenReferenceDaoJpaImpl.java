package com.byyd.middleware.creative.dao.jpa;

import org.springframework.stereotype.Repository;

import com.adfonic.domain.ClickTokenReference;
import com.byyd.middleware.creative.dao.ClickTokenReferenceDao;
import com.byyd.middleware.iface.dao.jpa.BusinessKeyDaoJpaImpl;

@Repository
public class ClickTokenReferenceDaoJpaImpl extends BusinessKeyDaoJpaImpl<ClickTokenReference> implements ClickTokenReferenceDao {

}
