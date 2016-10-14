package com.byyd.middleware.account.dao;

import java.util.List;

import com.adfonic.domain.AdfonicUser;
import com.byyd.middleware.account.filter.AdfonicUserFilter;
import com.byyd.middleware.iface.dao.BusinessKeyDao;
import com.byyd.middleware.iface.dao.FetchStrategy;

public interface AdfonicUserDao extends BusinessKeyDao<AdfonicUser> {

    AdfonicUser getByEmail(String emailAddress, FetchStrategy... fetchStrategy);

    AdfonicUser getByLoginName(String loginName, FetchStrategy... fetchStrategy);
    
    List<AdfonicUser> getAll(AdfonicUserFilter filter, FetchStrategy... fetchStrategy);

}
