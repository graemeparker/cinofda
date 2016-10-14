package com.byyd.middleware.common.dao;

import java.util.List;

import com.adfonic.domain.ContentType;
import com.byyd.middleware.iface.dao.BusinessKeyDao;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.Pagination;
import com.byyd.middleware.iface.dao.Sorting;

public interface ContentTypeDao extends BusinessKeyDao<ContentType> {

    Long countAllForMimeType(String mimeType);
    List<ContentType> getAllForMimeType(String mimeType, FetchStrategy... fetchStrategy);
    List<ContentType> getAllForMimeType(String mimeType, Sorting sort, FetchStrategy... fetchStrategy);
    List<ContentType> getAllForMimeType(String mimeType, Pagination page, FetchStrategy... fetchStrategy);

    Long countAllForMimeTypeLike(String mimeType);
    List<ContentType> getAllForMimeTypeLike(String mimeType, FetchStrategy... fetchStrategy);
    List<ContentType> getAllForMimeTypeLike(String mimeType, Sorting sort, FetchStrategy... fetchStrategy);
    List<ContentType> getAllForMimeTypeLike(String mimeType, Pagination page, FetchStrategy... fetchStrategy);

    ContentType getOneForMimeType(String mimeType, FetchStrategy... fetchStrategy);
    ContentType getOneForMimeType(String mimeType, boolean animated, FetchStrategy... fetchStrategy);

}
