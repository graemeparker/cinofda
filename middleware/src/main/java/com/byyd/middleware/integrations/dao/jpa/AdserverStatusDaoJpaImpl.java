package com.byyd.middleware.integrations.dao.jpa;

import org.springframework.stereotype.Repository;

import com.adfonic.domain.AdserverStatus;
import com.byyd.middleware.iface.dao.jpa.BusinessKeyDaoJpaImpl;
import com.byyd.middleware.integrations.dao.AdservserStatusDao;

/**
 * @author Anuj.Saboo
 * 
 * 
 */
@Repository
public class AdserverStatusDaoJpaImpl extends BusinessKeyDaoJpaImpl<AdserverStatus> implements AdservserStatusDao {

}
