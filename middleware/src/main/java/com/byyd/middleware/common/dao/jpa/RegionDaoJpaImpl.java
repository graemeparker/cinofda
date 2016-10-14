package com.byyd.middleware.common.dao.jpa;

import org.springframework.stereotype.Repository;

import com.adfonic.domain.Region;
import com.byyd.middleware.common.dao.RegionDao;
import com.byyd.middleware.iface.dao.jpa.BusinessKeyDaoJpaImpl;

@Repository
public class RegionDaoJpaImpl extends BusinessKeyDaoJpaImpl<Region> implements RegionDao {

}
