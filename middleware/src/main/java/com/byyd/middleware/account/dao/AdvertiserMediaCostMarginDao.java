package com.byyd.middleware.account.dao;

import java.util.List;

import com.adfonic.domain.AdvertiserMediaCostMargin;
import com.adfonic.domain.Company;
import com.byyd.middleware.iface.dao.BusinessKeyDao;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.Pagination;
import com.byyd.middleware.iface.dao.Sorting;

public interface AdvertiserMediaCostMarginDao extends BusinessKeyDao<AdvertiserMediaCostMargin> {
    
    Long countAllForCompany(Company company);
    List<AdvertiserMediaCostMargin> getAllForCompany(Company company, FetchStrategy ... fetchStrategy);
    List<AdvertiserMediaCostMargin> getAllForCompany(Company company, Pagination page, FetchStrategy ... fetchStrategy);
    List<AdvertiserMediaCostMargin> getAllForCompany(Company company, Sorting sort, FetchStrategy ... fetchStrategy);
}
