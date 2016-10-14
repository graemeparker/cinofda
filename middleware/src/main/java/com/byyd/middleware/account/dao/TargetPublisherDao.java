package com.byyd.middleware.account.dao;

import java.util.List;

import com.adfonic.domain.TargetPublisher;
import com.byyd.middleware.account.filter.TargetPublisherFilter;
import com.byyd.middleware.iface.dao.BusinessKeyDao;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.Sorting;

public interface TargetPublisherDao extends BusinessKeyDao<TargetPublisher> {

    Long countAll(TargetPublisherFilter filter);

    List<TargetPublisher> getAll(TargetPublisherFilter filter,
            FetchStrategy... fetchStrategy);

    List<TargetPublisher> getAll(TargetPublisherFilter filter, Sorting sort,
            FetchStrategy... fetchStrategy);
    
}
