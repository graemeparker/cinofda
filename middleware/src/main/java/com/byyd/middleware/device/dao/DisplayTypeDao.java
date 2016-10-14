package com.byyd.middleware.device.dao;

import com.adfonic.domain.DisplayType;
import com.byyd.middleware.iface.dao.BusinessKeyDao;

public interface DisplayTypeDao extends BusinessKeyDao<DisplayType> {

    DisplayType getBySystemName(String systemName);

}
