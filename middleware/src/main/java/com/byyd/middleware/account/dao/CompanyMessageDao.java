package com.byyd.middleware.account.dao;

import java.util.Collection;
import java.util.List;

import com.adfonic.domain.Advertiser;
import com.adfonic.domain.CompanyMessage;
import com.adfonic.domain.Publisher;
import com.byyd.middleware.iface.dao.BusinessKeyDao;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.Pagination;
import com.byyd.middleware.iface.dao.Sorting;

public interface CompanyMessageDao extends BusinessKeyDao<CompanyMessage> {
    public List<CompanyMessage> getCompanyMessagesWithSystemNamesForAdvertiser(Advertiser advertiser, Collection<String> systemNames, FetchStrategy... fetchStrategy);
    public List<CompanyMessage> getCompanyMessagesWithSystemNamesForAdvertiser(Advertiser advertiser, Collection<String> systemNames, Sorting sort, FetchStrategy... fetchStrategy);
    public List<CompanyMessage> getCompanyMessagesWithSystemNamesForAdvertiser(Advertiser advertiser, Collection<String> systemNames, Pagination page, FetchStrategy... fetchStrategy);
    public Long countCompanyMessagesWithSystemNamesForAdvertiser(Advertiser advertiser, Collection<String> systemNames);

    public List<CompanyMessage> getCompanyMessagesWithSystemNamesForPublisher(Publisher publisher, Collection<String> systemNames, FetchStrategy... fetchStrategy);
    public List<CompanyMessage> getCompanyMessagesWithSystemNamesForPublisher(Publisher publisher, Collection<String> systemNames, Sorting sort, FetchStrategy... fetchStrategy);
    public List<CompanyMessage> getCompanyMessagesWithSystemNamesForPublisher(Publisher publisher, Collection<String> systemNames, Pagination page, FetchStrategy... fetchStrategy);
    public Long countCompanyMessagesWithSystemNamesForPublisher(Publisher publisher, Collection<String> systemNames);
}
