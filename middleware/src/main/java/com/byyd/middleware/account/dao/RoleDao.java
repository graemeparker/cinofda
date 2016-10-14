package com.byyd.middleware.account.dao;

import java.util.List;

import com.adfonic.domain.Role;
import com.adfonic.domain.Role.RoleType;
import com.byyd.middleware.iface.dao.BusinessKeyDao;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.Pagination;
import com.byyd.middleware.iface.dao.Sorting;

public interface RoleDao extends BusinessKeyDao<Role> {

    Long countAllRoles(RoleType roleType);
    List<Role> getAllRoles(RoleType roleType, FetchStrategy... fetchStrategy);
    List<Role> getAllRoles(RoleType roleType, Sorting sort, FetchStrategy... fetchStrategy);
    List<Role> getAllRoles(RoleType roleType, Pagination page, FetchStrategy... fetchStrategy);


}
