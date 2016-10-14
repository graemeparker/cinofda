package com.byyd.middleware.common.dao;

import com.adfonic.domain.ContentSpec;
import com.byyd.middleware.iface.dao.BusinessKeyDao;
import com.byyd.middleware.iface.dao.FetchStrategy;

public interface ContentSpecDao extends BusinessKeyDao<ContentSpec> {
    @Override
    ContentSpec getByName(String name, FetchStrategy... fetchStrategy);
}
