package com.byyd.middleware.account.dao.jpa;

import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.stereotype.Repository;

import com.adfonic.domain.AccountFixedMargin;
import com.adfonic.domain.AccountFixedMargin_;
import com.adfonic.domain.Company;
import com.byyd.middleware.account.dao.AccountFixedMarginDao;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.Pagination;
import com.byyd.middleware.iface.dao.Sorting;
import com.byyd.middleware.iface.dao.jpa.BusinessKeyDaoJpaImpl;

@Repository
public class AccountFixedMarginDaoJpaImpl extends BusinessKeyDaoJpaImpl<AccountFixedMargin> implements AccountFixedMarginDao {
    
    @Override
    public List<AccountFixedMargin> getAllForCompany(Company company, FetchStrategy ... fetchStrategy) {
        return getAllForCompany(company, null, null, fetchStrategy);
    }

    @Override
    public List<AccountFixedMargin> getAllForCompany(Company company, Pagination page, FetchStrategy ... fetchStrategy) {
        return getAllForCompany(company, page, page.getSorting(), fetchStrategy);
    }

    @Override
    public List<AccountFixedMargin> getAllForCompany(Company company, Sorting sort, FetchStrategy ... fetchStrategy) {
        return getAllForCompany(company, null, sort, fetchStrategy);
    }

    protected List<AccountFixedMargin> getAllForCompany(Company company, Pagination page, Sorting sort, FetchStrategy ... fetchStrategy) {
        CriteriaBuilder criteriaBuilder = getTransactionalEntityManager().getCriteriaBuilder();
        CriteriaQueryContainer container = createCriteriaQuery(fetchStrategy);
        CriteriaQuery<AccountFixedMargin> criteriaQuery = container.getQuery();
        Root<AccountFixedMargin> root = container.getRoot();

        Predicate predicate = criteriaBuilder.equal(root.get(AccountFixedMargin_.company), company);
        criteriaQuery = criteriaQuery.where(predicate);

        criteriaQuery = criteriaQuery.select(root);

        criteriaQuery = processOrderBy(criteriaQuery, root, sort);
        return findAll(criteriaQuery, page);
    }

}
