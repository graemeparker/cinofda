package com.byyd.middleware.account.dao.jpa;

import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.stereotype.Repository;

import com.adfonic.domain.Company;
import com.adfonic.domain.MarginShareDSP;
import com.adfonic.domain.MarginShareDSP_;
import com.byyd.middleware.account.dao.MarginShareDSPDao;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.Pagination;
import com.byyd.middleware.iface.dao.Sorting;
import com.byyd.middleware.iface.dao.jpa.BusinessKeyDaoJpaImpl;

@Repository
public class MarginShareDSPDaoJpaImpl extends BusinessKeyDaoJpaImpl<MarginShareDSP> implements MarginShareDSPDao {

    @Override
    public Long countAllForCompany(Company company) {
        CriteriaBuilder criteriaBuilder = getTransactionalEntityManager().getCriteriaBuilder();
        CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
        Root<MarginShareDSP> root = criteriaQuery.from(MarginShareDSP.class);

        Predicate predicate = criteriaBuilder.equal(root.get(MarginShareDSP_.company), company);
        criteriaQuery = criteriaQuery.where(predicate);

        criteriaQuery = criteriaQuery.select(criteriaBuilder.count(root));

        return executeLongAggregateFunction(criteriaQuery);
    }

    @Override
    public List<MarginShareDSP> getAllForCompany(Company company, FetchStrategy ... fetchStrategy) {
        return getAllForCompany(company, null, null, fetchStrategy);
    }

    @Override
    public List<MarginShareDSP> getAllForCompany(Company company, Pagination page, FetchStrategy ... fetchStrategy) {
        return getAllForCompany(company, page, page.getSorting(), fetchStrategy);
    }

    @Override
    public List<MarginShareDSP> getAllForCompany(Company company, Sorting sort, FetchStrategy ... fetchStrategy) {
        return getAllForCompany(company, null, sort, fetchStrategy);
    }

    protected List<MarginShareDSP> getAllForCompany(Company company, Pagination page, Sorting sort, FetchStrategy ... fetchStrategy) {
        CriteriaBuilder criteriaBuilder = getTransactionalEntityManager().getCriteriaBuilder();
        CriteriaQueryContainer container = createCriteriaQuery(fetchStrategy);
        CriteriaQuery<MarginShareDSP> criteriaQuery = container.getQuery();
        Root<MarginShareDSP> root = container.getRoot();

        Predicate predicate = criteriaBuilder.equal(root.get(MarginShareDSP_.company), company);
        criteriaQuery = criteriaQuery.where(predicate);

        criteriaQuery = criteriaQuery.select(root);

        criteriaQuery = processOrderBy(criteriaQuery, root, sort);
        return findAll(criteriaQuery, page);
    }

}
