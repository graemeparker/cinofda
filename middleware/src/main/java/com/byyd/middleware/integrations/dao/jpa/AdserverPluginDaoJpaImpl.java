package com.byyd.middleware.integrations.dao.jpa;

import org.springframework.stereotype.Repository;

import com.adfonic.domain.AdserverPlugin;
import com.byyd.middleware.iface.dao.jpa.BusinessKeyDaoJpaImpl;
import com.byyd.middleware.integrations.dao.AdserverPluginDao;

@Repository
public class AdserverPluginDaoJpaImpl extends BusinessKeyDaoJpaImpl<AdserverPlugin> implements AdserverPluginDao {
}
