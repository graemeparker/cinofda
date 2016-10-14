package com.byyd.middleware.account.dao;

import java.util.List;

import com.adfonic.domain.Publisher;
import com.adfonic.domain.PublisherRevShare;
import com.byyd.middleware.iface.dao.BusinessKeyDao;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.Pagination;
import com.byyd.middleware.iface.dao.Sorting;

public interface PublisherRevShareDao extends BusinessKeyDao<PublisherRevShare> {

    Long countAllForPublisher(Publisher publisher);
    List<PublisherRevShare> getAllForPublisher(Publisher publisher, FetchStrategy... fetchStrategy);
    List<PublisherRevShare> getAllForPublisher(Publisher publisher, Sorting sort, FetchStrategy... fetchStrategy);
    List<PublisherRevShare> getAllForPublisher(Publisher publisher, Pagination page, FetchStrategy... fetchStrategy);
}
