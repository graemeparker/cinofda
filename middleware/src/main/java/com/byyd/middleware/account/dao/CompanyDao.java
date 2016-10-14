package com.byyd.middleware.account.dao;

import java.math.BigDecimal;

import com.adfonic.domain.Company;
import com.byyd.middleware.iface.dao.BusinessKeyDao;

public interface CompanyDao extends BusinessKeyDao<Company> {

    BigDecimal getTotalAdvertiserBalance(Company company);
    BigDecimal getSpendForCompany(Company company);
    BigDecimal getTotalPublisherBalance(Company company);

}
