package com.byyd.middleware.publication.dao.jpa;

import org.springframework.stereotype.Repository;

import com.adfonic.domain.PublicationProvidedInfo;
import com.byyd.middleware.iface.dao.jpa.BusinessKeyDaoJpaImpl;
import com.byyd.middleware.publication.dao.PublicationProvidedInfoDao;

@Repository
public class PublicationProvidedInfoDaoJpaImpl extends BusinessKeyDaoJpaImpl<PublicationProvidedInfo> implements PublicationProvidedInfoDao {
}
