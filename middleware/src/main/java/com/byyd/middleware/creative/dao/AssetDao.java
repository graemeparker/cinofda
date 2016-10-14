package com.byyd.middleware.creative.dao;

import java.util.List;

import com.adfonic.domain.Asset;
import com.adfonic.domain.ContentType;
import com.adfonic.domain.Creative;
import com.byyd.middleware.iface.dao.BusinessKeyDao;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.Pagination;
import com.byyd.middleware.iface.dao.Sorting;

public interface AssetDao extends BusinessKeyDao<Asset> {
    
    Long countAllForCreative(Creative creative);
    List<Asset> findAllByCreative(Creative creative, FetchStrategy... fetchStrategy);
    List<Asset> findAllByCreative(Creative creative, Sorting sort, FetchStrategy... fetchStrategy);
    List<Asset> findAllByCreative(Creative creative, Pagination page, FetchStrategy... fetchStrategy);
    
    Long countAllForCreativeAndContentType(Creative creative, ContentType contentType);
    List<Asset> findAllByCreativeAndContentType(Creative creative, ContentType contentType, FetchStrategy... fetchStrategy);
    List<Asset> findAllByCreativeAndContentType(Creative creative, ContentType contentType, Sorting sort, FetchStrategy... fetchStrategy);
    List<Asset> findAllByCreativeAndContentType(Creative creative, ContentType contentType, Pagination page, FetchStrategy... fetchStrategy);


}
