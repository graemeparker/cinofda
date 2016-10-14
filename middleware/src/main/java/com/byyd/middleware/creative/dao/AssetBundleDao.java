package com.byyd.middleware.creative.dao;

import java.util.List;

import com.adfonic.domain.AssetBundle;
import com.byyd.middleware.creative.filter.AssetBundleFilter;
import com.byyd.middleware.iface.dao.BusinessKeyDao;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.Pagination;
import com.byyd.middleware.iface.dao.Sorting;

public interface AssetBundleDao extends BusinessKeyDao<AssetBundle> {

    Long countAll(AssetBundleFilter filter);
    List<AssetBundle> getAll(AssetBundleFilter filter, FetchStrategy... fetchStrategy);
    List<AssetBundle> getAll(AssetBundleFilter filter, Sorting sort, FetchStrategy... fetchStrategy);
    List<AssetBundle> getAll(AssetBundleFilter filter, Pagination page, FetchStrategy... fetchStrategy);

}
