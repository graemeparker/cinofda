package com.byyd.middleware.publication.dao;

import java.util.List;

import com.adfonic.domain.Company;
import com.adfonic.domain.TransparentNetwork;
import com.byyd.middleware.iface.dao.BusinessKeyDao;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.Pagination;
import com.byyd.middleware.iface.dao.Sorting;

public interface TransparentNetworkDao extends BusinessKeyDao<TransparentNetwork> {

    Long countAvailableTransparentNetworksForCompany(Company company, boolean includePremium);
    List<TransparentNetwork> getAvailableTransparentNetworksForCompany(Company company, boolean includePremium, FetchStrategy... fetchStrategy);
    List<TransparentNetwork> getAvailableTransparentNetworksForCompany(Company company, boolean includePremium, Sorting sort, FetchStrategy... fetchStrategy);
    List<TransparentNetwork> getAvailableTransparentNetworksForCompany(Company company, boolean includePremium, Pagination page, FetchStrategy... fetchStrategy);

}
