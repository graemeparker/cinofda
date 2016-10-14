package com.byyd.middleware.publication.dao.jpa;

import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.stereotype.Repository;

import com.adfonic.domain.Company;
import com.adfonic.domain.TransparentNetwork;
import com.adfonic.domain.TransparentNetwork_;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.Pagination;
import com.byyd.middleware.iface.dao.Sorting;
import com.byyd.middleware.iface.dao.jpa.BusinessKeyDaoJpaImpl;
import com.byyd.middleware.publication.dao.TransparentNetworkDao;

@Repository
public class TransparentNetworkDaoJpaImpl extends BusinessKeyDaoJpaImpl<TransparentNetwork> implements TransparentNetworkDao {

    protected Predicate getAvailableTransparentNetworksForCompanyPredicate(Root<TransparentNetwork> root, Company company, boolean includePremium) {
        CriteriaBuilder criteriaBuilder = getTransactionalEntityManager().getCriteriaBuilder();

        Predicate closedPredicate = criteriaBuilder.isFalse(root.get(TransparentNetwork_.closed));
        Predicate advertisersPredicate = criteriaBuilder.isMember(company, root.get(TransparentNetwork_.advertisers));
        Predicate premiumPredicate = null;
        if (!includePremium) {
            premiumPredicate = criteriaBuilder.notEqual(root.get(TransparentNetwork_.name), TransparentNetwork.PERFORMANCE_NETWORK_NAME);
        }
        return and(
                    or(closedPredicate, advertisersPredicate),
                    premiumPredicate
                );
    }

    @Override
    public Long countAvailableTransparentNetworksForCompany(Company company, boolean includePremium) {
        CriteriaBuilder criteriaBuilder = getTransactionalEntityManager().getCriteriaBuilder();
        CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
        Root<TransparentNetwork> root = criteriaQuery.from(TransparentNetwork.class);

        Predicate predicate = getAvailableTransparentNetworksForCompanyPredicate(root, company, includePremium);
        criteriaQuery = criteriaQuery.where(predicate);

        criteriaQuery = criteriaQuery.select(criteriaBuilder.count(root));

        return executeLongAggregateFunction(criteriaQuery);
    }

    @Override
    public List<TransparentNetwork> getAvailableTransparentNetworksForCompany(Company company, boolean includePremium, FetchStrategy... fetchStrategy) {
        return getAvailableTransparentNetworksForCompany(company, includePremium, null, null, fetchStrategy);
    }

    @Override
    public List<TransparentNetwork> getAvailableTransparentNetworksForCompany(Company company, boolean includePremium, Sorting sort, FetchStrategy... fetchStrategy) {
        return getAvailableTransparentNetworksForCompany(company, includePremium, null, sort, fetchStrategy);
    }

    @Override
    public List<TransparentNetwork> getAvailableTransparentNetworksForCompany(Company company, boolean includePremium, Pagination page, FetchStrategy... fetchStrategy) {
        return getAvailableTransparentNetworksForCompany(company, includePremium, page, page.getSorting(), fetchStrategy);
    }

    protected List<TransparentNetwork> getAvailableTransparentNetworksForCompany(Company company, boolean includePremium, Pagination page, Sorting sort, FetchStrategy... fetchStrategy) {
        CriteriaQueryContainer container = createCriteriaQuery(fetchStrategy);
        CriteriaQuery<TransparentNetwork> criteriaQuery = container.getQuery();
        Root<TransparentNetwork> root = container.getRoot();
        CriteriaBuilder criteriaBuilder = getTransactionalEntityManager().getCriteriaBuilder();

        Predicate predicate = getAvailableTransparentNetworksForCompanyPredicate(root, company, includePremium);
        criteriaQuery = criteriaQuery.where(predicate);

        criteriaQuery = criteriaQuery.select(root);

        criteriaQuery = processOrderBy(criteriaBuilder, criteriaQuery, root, sort);
        return findAll(criteriaQuery, page);
    }

}
