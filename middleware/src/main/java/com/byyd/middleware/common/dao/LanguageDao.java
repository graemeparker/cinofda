package com.byyd.middleware.common.dao;

import com.adfonic.domain.Language;
import com.byyd.middleware.iface.dao.BusinessKeyDao;
import com.byyd.middleware.iface.dao.FetchStrategy;

public interface LanguageDao extends BusinessKeyDao<Language> {
    
    Language getByIsoCode(String isoCode, FetchStrategy... fetchStrategy);

}
