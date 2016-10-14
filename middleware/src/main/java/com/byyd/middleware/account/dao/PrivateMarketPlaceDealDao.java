package com.byyd.middleware.account.dao;

import java.util.List;

import com.adfonic.domain.PrivateMarketPlaceDeal;
import com.adfonic.domain.Publisher;
import com.byyd.middleware.iface.dao.BusinessKeyDao;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.Pagination;
import com.byyd.middleware.iface.dao.Sorting;

public interface PrivateMarketPlaceDealDao extends BusinessKeyDao<PrivateMarketPlaceDeal> {

    Long countAllForPublisher(Publisher publisher);
    List<PrivateMarketPlaceDeal> getAllForPublisher(Publisher publisher, FetchStrategy... fetchStrategy);
    List<PrivateMarketPlaceDeal> getAllForPublisher(Publisher publisher, Sorting sort, FetchStrategy... fetchStrategy);
    List<PrivateMarketPlaceDeal> getAllForPublisher(Publisher publisher, Pagination page, FetchStrategy... fetchStrategy);
    
    PrivateMarketPlaceDeal getByPublisherAndDealId(Publisher publisher, String dealId, FetchStrategy... fetchStrategy);
}
