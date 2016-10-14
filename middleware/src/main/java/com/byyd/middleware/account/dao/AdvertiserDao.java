package com.byyd.middleware.account.dao;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import com.adfonic.domain.Advertiser;
import com.adfonic.domain.Company;
import com.byyd.middleware.account.filter.AdvertiserFilter;
import com.byyd.middleware.iface.dao.BusinessKeyDao;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.Pagination;
import com.byyd.middleware.iface.dao.Sorting;

public interface AdvertiserDao extends BusinessKeyDao<Advertiser> {
    
    static final String SPEND_BY_ADVERTISER_ID = "__SpendByAdvertiserId__";
    static final String BALANCE_BY_ADVERTISER_ID = "__BalanceByAdvertiserId__";

    Long countAllForCompany(Company company);
    List<Advertiser> findAllByCompany(Company company, FetchStrategy... fetchStrategy);
    List<Advertiser> findAllByCompany(Company company, Sorting sort, FetchStrategy... fetchStrategy);
    List<Advertiser> findAllByCompany(Company company, Pagination page, FetchStrategy... fetchStrategy);

    Long countAll(AdvertiserFilter filter);
    List<Advertiser> findAll(AdvertiserFilter filter, FetchStrategy... fetchStrategy);
    List<Advertiser> findAll(AdvertiserFilter filter, Sorting sort, FetchStrategy... fetchStrategy);
    List<Advertiser> findAll(AdvertiserFilter filter, Pagination page, FetchStrategy... fetchStrategy);

    Advertiser getByName(String name, Company company, FetchStrategy... fetchStrategy);

    Map<String, Map<Long, BigDecimal>> getSpendAndBalanceForAdvertisersAndDateIds(List<Advertiser> advertisers, int startDateId, int endDateId);

}
