package com.byyd.middleware.account.dao.jpa;

import org.springframework.stereotype.Repository;

import com.adfonic.domain.AdminRole;
import com.byyd.middleware.account.dao.AdminRoleDao;
import com.byyd.middleware.iface.dao.jpa.BusinessKeyDaoJpaImpl;

@Repository
public class AdminRoleDaoJpaImpl extends BusinessKeyDaoJpaImpl<AdminRole> implements AdminRoleDao {


}
