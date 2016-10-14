package com.byyd.middleware.common.dao.jpa;

import org.springframework.stereotype.Repository;

import com.adfonic.domain.RemovalInfo;
import com.byyd.middleware.common.dao.RemovalInfoDao;
import com.byyd.middleware.iface.dao.jpa.BusinessKeyDaoJpaImpl;

@Repository
public class RemovalInfoDaoJpaImpl extends BusinessKeyDaoJpaImpl<RemovalInfo> implements RemovalInfoDao {

}
