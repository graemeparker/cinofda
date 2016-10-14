package com.byyd.middleware.account.dao.jpa;

import org.springframework.stereotype.Repository;

import com.adfonic.domain.CompanyDirectCost;
import com.byyd.middleware.account.dao.CompanyDirectCostDao;
import com.byyd.middleware.iface.dao.jpa.BusinessKeyDaoJpaImpl;

@Repository
public class CompanyDirectCostDaoJpaImpl extends BusinessKeyDaoJpaImpl<CompanyDirectCost> implements CompanyDirectCostDao {
	
}
