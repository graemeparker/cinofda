package com.byyd.middleware.account.dao.jpa;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Repository;

import com.adfonic.domain.Account;
import com.byyd.middleware.account.dao.AccountDao;
import com.byyd.middleware.iface.dao.jpa.BusinessKeyDaoJpaImpl;
import com.byyd.middleware.iface.dao.jpa.QueryParameter;

@Repository
public class AccountDaoJpaImpl extends BusinessKeyDaoJpaImpl<Account> implements AccountDao {

    @Override
    public void addToBalance(Account account, BigDecimal amount) {
        String update = "UPDATE ACCOUNT SET BALANCE=BALANCE+? WHERE ID=?";
        executeUpdateNativeQueryPositionalParameters(update, new QueryParameter(amount), new QueryParameter(account.getId()));
    }
    
    protected StringBuilder getAccountAdvertisersBalanceForCompanyQuery() {
        return new StringBuilder("SELECT")
            .append(" SUM(BALANCE)")
            .append(" FROM COMPANY c")
            .append(" JOIN ADVERTISER ad ON (c.ID = ad.COMPANY_ID)")
            .append(" JOIN ACCOUNT ac ON (ad.ACCOUNT_ID = ac.ID)")
            .append(" WHERE c.ID = ?")
            .append(" AND ACCOUNT_TYPE = 'ADVERTISER'")
            .append(" GROUP BY c.ID");
    }

    @Override
    public Double getAccountAdvertisersBalanceForCompany(Long companyId) {
        StringBuilder query = getAccountAdvertisersBalanceForCompanyQuery();
        List<QueryParameter> list = new ArrayList<QueryParameter>();
        list.add(new QueryParameter(companyId));
        return this.executeAggregateFunctionByNativeQueryPositionalParameters(query.toString(), list).doubleValue();
    }
}
