package com.byyd.middleware.account.dao.jpa;

import java.math.BigDecimal;

import org.springframework.stereotype.Repository;

import com.adfonic.domain.Company;
import com.byyd.middleware.account.dao.CompanyDao;
import com.byyd.middleware.iface.dao.jpa.BusinessKeyDaoJpaImpl;
import com.byyd.middleware.iface.dao.jpa.QueryParameter;

@Repository
public class CompanyDaoJpaImpl extends BusinessKeyDaoJpaImpl<Company> implements CompanyDao {

    /*
    * all advertiser balances for a company
    */
    @Override
    public BigDecimal getTotalAdvertiserBalance(Company company) {
        QueryParameter args = new QueryParameter(company.getId());
        return (BigDecimal) executeAggregateFunctionByNativeQueryPositionalParameters("SELECT SUM(ACCOUNT.BALANCE) FROM ACCOUNT INNER JOIN ADVERTISER ON ACCOUNT.ID=ADVERTISER.ACCOUNT_ID WHERE ADVERTISER.COMPANY_ID=?", args);
    }

    /*
     * all publisher balances for a company
     */
    @Override
    public BigDecimal getTotalPublisherBalance(Company company) {
        QueryParameter args = new QueryParameter(company.getId());
        return (BigDecimal) executeAggregateFunctionByNativeQueryPositionalParameters("SELECT SUM(ACCOUNT.BALANCE) FROM ACCOUNT INNER JOIN PUBLISHER ON ACCOUNT.ID=PUBLISHER.ACCOUNT_ID WHERE PUBLISHER.COMPANY_ID=?", args);
    }


    /*
     * sum of spend for a company
     */
     @Override
    public BigDecimal getSpendForCompany(Company company) {
         QueryParameter args = new QueryParameter(company.getId());
         return (BigDecimal) executeAggregateFunctionByNativeQueryPositionalParameters("select COALESCE(sum(AMOUNT),0) from CAMPAIGN_OVERALL_SPEND join CAMPAIGN on CAMPAIGN.ID=CAMPAIGN_OVERALL_SPEND.CAMPAIGN_ID join ADVERTISER on ADVERTISER.ID=CAMPAIGN.ADVERTISER_ID where ADVERTISER.COMPANY_ID=?", args);
     }
}
