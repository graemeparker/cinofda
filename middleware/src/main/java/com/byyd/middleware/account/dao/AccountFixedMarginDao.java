package com.byyd.middleware.account.dao;

import java.util.List;

import com.adfonic.domain.AccountFixedMargin;
import com.adfonic.domain.Company;
import com.byyd.middleware.iface.dao.BusinessKeyDao;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.Pagination;
import com.byyd.middleware.iface.dao.Sorting;

public interface AccountFixedMarginDao extends BusinessKeyDao<AccountFixedMargin> {

    List<AccountFixedMargin> getAllForCompany(Company company, FetchStrategy ... fetchStrategy);
    List<AccountFixedMargin> getAllForCompany(Company company, Pagination page, FetchStrategy ... fetchStrategy);
    List<AccountFixedMargin> getAllForCompany(Company company, Sorting sort, FetchStrategy ... fetchStrategy);

}
