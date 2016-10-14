package com.byyd.middleware.device.dao.jpa;

import org.springframework.stereotype.Repository;

import com.adfonic.domain.Platform;
import com.byyd.middleware.device.dao.PlatformDao;
import com.byyd.middleware.iface.dao.jpa.BusinessKeyDaoJpaImpl;

@Repository
public class PlatformDaoJpaImpl extends BusinessKeyDaoJpaImpl<Platform> implements PlatformDao {

}
