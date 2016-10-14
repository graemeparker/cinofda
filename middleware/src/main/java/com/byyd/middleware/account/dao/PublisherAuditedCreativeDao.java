package com.byyd.middleware.account.dao;

import java.util.List;

import com.adfonic.domain.Creative;
import com.adfonic.domain.Publisher;
import com.adfonic.domain.PublisherAuditedCreative;
import com.byyd.middleware.iface.dao.BusinessKeyDao;
import com.byyd.middleware.iface.dao.FetchStrategy;

public interface PublisherAuditedCreativeDao extends BusinessKeyDao<PublisherAuditedCreative>{

    PublisherAuditedCreative getByPublisherAndCreative(Publisher publisher, Creative creative, FetchStrategy ... fetchStrategy);

    List<PublisherAuditedCreative> getByCreative(Creative creative, FetchStrategy ... fetchStrategy);
    
}
