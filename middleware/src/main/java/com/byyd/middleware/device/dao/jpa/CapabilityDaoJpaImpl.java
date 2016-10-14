package com.byyd.middleware.device.dao.jpa;

import org.springframework.stereotype.Repository;

import com.adfonic.domain.Capability;
import com.byyd.middleware.device.dao.CapabilityDao;
import com.byyd.middleware.iface.dao.jpa.BusinessKeyDaoJpaImpl;

@Repository
public class CapabilityDaoJpaImpl extends BusinessKeyDaoJpaImpl<Capability> implements CapabilityDao {

}
