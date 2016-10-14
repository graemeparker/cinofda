package com.byyd.middleware.creative.dao.jpa;

import org.springframework.stereotype.Repository;

import com.adfonic.domain.CreativeRemovedPublicationHistory;
import com.byyd.middleware.creative.dao.CreativeRemovedPublicationHistoryDao;
import com.byyd.middleware.iface.dao.jpa.BusinessKeyDaoJpaImpl;

@Repository
public class CreativeRemovedPublicationHistoryDaoJpaImpl extends BusinessKeyDaoJpaImpl<CreativeRemovedPublicationHistory> implements CreativeRemovedPublicationHistoryDao {

}
