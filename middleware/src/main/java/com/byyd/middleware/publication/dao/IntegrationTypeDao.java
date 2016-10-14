package com.byyd.middleware.publication.dao;

import com.adfonic.domain.IntegrationType;
import com.byyd.middleware.iface.dao.BusinessKeyDao;
import com.byyd.middleware.iface.dao.FetchStrategy;

public interface IntegrationTypeDao extends BusinessKeyDao<IntegrationType> {
    @Override
    IntegrationType getBySystemName(String systemName, FetchStrategy ... fetchStrategy);
}
