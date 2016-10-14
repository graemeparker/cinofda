package com.byyd.middleware.account.dao;

import java.math.BigDecimal;

import com.adfonic.domain.Account;
import com.byyd.middleware.iface.dao.BusinessKeyDao;

public interface AccountDao extends BusinessKeyDao<Account> {

    void addToBalance(Account account, BigDecimal amount);
    Double getAccountAdvertisersBalanceForCompany(Long companyId);

}
