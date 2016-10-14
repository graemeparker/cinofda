package com.byyd.middleware.account.dao;

import com.adfonic.domain.Company;
import com.adfonic.domain.OptimisationReportCompanyPreferences;
import com.byyd.middleware.iface.dao.BusinessKeyDao;
import com.byyd.middleware.iface.dao.FetchStrategy;

public interface OptimisationReportCompanyPreferencesDao extends  BusinessKeyDao<OptimisationReportCompanyPreferences> {

    OptimisationReportCompanyPreferences getForCompany(Company company, FetchStrategy... fetchStrategy);
}
