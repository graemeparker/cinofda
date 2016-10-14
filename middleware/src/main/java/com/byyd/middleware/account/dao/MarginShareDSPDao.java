package com.byyd.middleware.account.dao;

import java.util.List;

import com.adfonic.domain.Company;
import com.adfonic.domain.MarginShareDSP;
import com.byyd.middleware.iface.dao.BusinessKeyDao;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.Pagination;
import com.byyd.middleware.iface.dao.Sorting;

public interface MarginShareDSPDao extends BusinessKeyDao<MarginShareDSP> {

    Long countAllForCompany(Company company);
    List<MarginShareDSP> getAllForCompany(Company company, FetchStrategy ... fetchStrategy);
    List<MarginShareDSP> getAllForCompany(Company company, Pagination page, FetchStrategy ... fetchStrategy);
    List<MarginShareDSP> getAllForCompany(Company company, Sorting sort, FetchStrategy ... fetchStrategy);
}
