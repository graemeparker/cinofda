package com.byyd.middleware.campaign.dao;

import java.util.List;

import com.adfonic.domain.GeotargetList;
import com.byyd.middleware.iface.dao.BusinessKeyDao;
import com.byyd.middleware.iface.dao.FetchStrategy;

public interface GeotargetListDao extends BusinessKeyDao<GeotargetList>{

    public List<GeotargetList> getGeotargetListByGeotargetBy(long id,
            FetchStrategy fetchStrategy);
}
