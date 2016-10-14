package com.byyd.middleware.common.dao;

import java.util.List;

import com.adfonic.domain.Channel;
import com.byyd.middleware.campaign.filter.ChannelFilter;
import com.byyd.middleware.iface.dao.BusinessKeyDao;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.Pagination;
import com.byyd.middleware.iface.dao.Sorting;

public interface ChannelDao extends BusinessKeyDao<Channel> {

    Long countAll(ChannelFilter filter);
    List<Channel> getAll(ChannelFilter filter, FetchStrategy... fetchStrategy);
    List<Channel> getAll(ChannelFilter filter, Sorting sort, FetchStrategy... fetchStrategy);
    List<Channel> getAll(ChannelFilter filter, Pagination page, FetchStrategy... fetchStrategy);

}
