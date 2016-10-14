package com.byyd.middleware.account.dao.jpa;

import org.springframework.stereotype.Repository;

import com.adfonic.domain.AdvertiserNotificationFlag;
import com.byyd.middleware.account.dao.AdvertiserNotificationFlagDao;
import com.byyd.middleware.iface.dao.jpa.BusinessKeyDaoJpaImpl;

@Repository
public class AdvertiserNotificationFlagDaoJpaImpl extends BusinessKeyDaoJpaImpl<AdvertiserNotificationFlag> implements AdvertiserNotificationFlagDao {

}
