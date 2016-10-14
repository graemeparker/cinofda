package com.byyd.middleware.integrations.dao.jpa;

import org.springframework.stereotype.Repository;

import com.adfonic.domain.AdserverShard;
import com.byyd.middleware.iface.dao.jpa.BusinessKeyDaoJpaImpl;
import com.byyd.middleware.integrations.dao.AdservserShardDao;

/**
 * @author Anuj.Saboo
 * 
 * 
 */
@Repository
public class AdserverShardDaoJpaImpl extends BusinessKeyDaoJpaImpl<AdserverShard> implements AdservserShardDao {

}
