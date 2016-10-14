package com.byyd.middleware.account.dao;

import com.adfonic.domain.AffiliateProgram;
import com.byyd.middleware.iface.dao.BusinessKeyDao;
import com.byyd.middleware.iface.dao.FetchStrategy;

public interface AffiliateProgramDao extends BusinessKeyDao<AffiliateProgram> {

    AffiliateProgram getByAffiliateId(String affiliateId, FetchStrategy... fetchStrategy);
}
