package com.byyd.middleware.device.dao.jpa;

import org.springframework.stereotype.Repository;

import com.adfonic.domain.DeviceGroup;
import com.byyd.middleware.device.dao.DeviceGroupDao;
import com.byyd.middleware.iface.dao.jpa.BusinessKeyDaoJpaImpl;

@Repository
public class DeviceGroupDaoJpaImpl extends BusinessKeyDaoJpaImpl<DeviceGroup> implements DeviceGroupDao {
}
