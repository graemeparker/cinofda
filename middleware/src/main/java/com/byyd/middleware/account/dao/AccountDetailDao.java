package com.byyd.middleware.account.dao;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import com.adfonic.domain.Account;
import com.adfonic.domain.AccountDetail;
import com.byyd.middleware.account.filter.AccountDetailFilter;
import com.byyd.middleware.iface.dao.BusinessKeyDao;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.Pagination;
import com.byyd.middleware.iface.dao.Sorting;

public interface AccountDetailDao extends BusinessKeyDao<AccountDetail> {

    Long countAll(AccountDetailFilter filter);
    List<AccountDetail> getAll(AccountDetailFilter filter, FetchStrategy... fetchStrategy);
    List<AccountDetail> getAll(AccountDetailFilter filter, Sorting sort, FetchStrategy... fetchStrategy);
    List<AccountDetail> getAll(AccountDetailFilter filter, Pagination page, FetchStrategy... fetchStrategy);
    List<AccountDetail> getAll(AccountDetailFilter filter, Pagination page, Sorting sort, FetchStrategy... fetchStrategy);

    BigDecimal getBalanceAsOfDate(Account account, boolean postPay, Date date);

}
