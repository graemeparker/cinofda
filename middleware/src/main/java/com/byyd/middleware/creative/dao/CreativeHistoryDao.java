package com.byyd.middleware.creative.dao;

import java.util.List;

import com.adfonic.domain.Creative;
import com.adfonic.domain.CreativeHistory;
import com.byyd.middleware.iface.dao.BusinessKeyDao;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.Pagination;
import com.byyd.middleware.iface.dao.Sorting;

public interface CreativeHistoryDao extends BusinessKeyDao<CreativeHistory> {
    List<CreativeHistory> getAll(Creative creative, FetchStrategy ... fetchStrategy);
    List<CreativeHistory> getAll(Creative creative, Sorting sort, FetchStrategy ... fetchStrategy);
    List<CreativeHistory> getAll(Creative creative, Pagination page, FetchStrategy ... fetchStrategy);
}
