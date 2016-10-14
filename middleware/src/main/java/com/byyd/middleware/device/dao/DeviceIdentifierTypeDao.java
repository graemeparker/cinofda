package com.byyd.middleware.device.dao;

import java.util.List;

import com.adfonic.domain.DeviceIdentifierType;
import com.byyd.middleware.iface.dao.BusinessKeyDao;
import com.byyd.middleware.iface.dao.Sorting;

public interface DeviceIdentifierTypeDao extends BusinessKeyDao<DeviceIdentifierType> {
    List<DeviceIdentifierType> getAllNonHidden();
    List<DeviceIdentifierType> getAllNonHidden(Sorting sort);
}
