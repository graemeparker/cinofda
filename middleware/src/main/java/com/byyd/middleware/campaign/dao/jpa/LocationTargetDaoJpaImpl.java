package com.byyd.middleware.campaign.dao.jpa;

import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Repository;

import com.adfonic.domain.LocationTarget;
import com.adfonic.domain.LocationTarget_;
import com.byyd.middleware.campaign.dao.LocationTargetDao;
import com.byyd.middleware.campaign.filter.LocationTargetFilter;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.Pagination;
import com.byyd.middleware.iface.dao.Sorting;
import com.byyd.middleware.iface.dao.jpa.BusinessKeyDaoJpaImpl;

@Repository
public class LocationTargetDaoJpaImpl extends BusinessKeyDaoJpaImpl<LocationTarget> implements LocationTargetDao {

    protected Predicate getPredicate(Root<LocationTarget> root, LocationTargetFilter filter) {
        CriteriaBuilder criteriaBuilder = getTransactionalEntityManager().getCriteriaBuilder();

        Predicate namePredicate = null;
        Predicate advertiserPredicate = null;
        
        if(filter.getAdvertiser() != null) {
            advertiserPredicate = criteriaBuilder.equal(root.get(LocationTarget_.advertiser), filter.getAdvertiser());
        }

        if(!StringUtils.isEmpty(filter.getName())) {
            if(filter.getNameLikeSpec() == null) {
                if (filter.isNameCaseSensitive()) {
                    namePredicate = criteriaBuilder.equal(
                            root.get(LocationTarget_.name), filter.getName());
                } else {
                    namePredicate = criteriaBuilder.equal(
                            criteriaBuilder.lower(root.get(LocationTarget_.name)),
                            filter.getName().toLowerCase());
                }
            } else {
                if (filter.isNameCaseSensitive()) {
                    namePredicate = criteriaBuilder.like(root.get(LocationTarget_.name), filter.getNameLikeSpec().getPattern(filter.getName()));
                } else {
                    namePredicate = criteriaBuilder.like(criteriaBuilder.lower(root.get(LocationTarget_.name)), filter.getNameLikeSpec().getPattern(filter.getName()).toLowerCase());
                }
            }
        }

        return and(namePredicate, advertiserPredicate);
    }
    
    @Override
    public Long countAll(LocationTargetFilter filter) {
        CriteriaBuilder criteriaBuilder = getTransactionalEntityManager().getCriteriaBuilder();
        CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
        Root<LocationTarget> root = criteriaQuery.from(LocationTarget.class);

        Predicate predicate = getPredicate(root, filter);
        criteriaQuery = criteriaQuery.where(predicate);

        criteriaQuery = criteriaQuery.select(criteriaBuilder.count(root));

        return executeLongAggregateFunction(criteriaQuery);
    }

    @Override
    public List<LocationTarget> getAll(LocationTargetFilter filter, FetchStrategy ... fetchStrategy) {
        return getAll(filter, null, null, fetchStrategy);
    }

    @Override
    public List<LocationTarget> getAll(LocationTargetFilter filter, Pagination page, FetchStrategy ... fetchStrategy) {
        return getAll(filter, page, page.getSorting(), fetchStrategy);
    }

    @Override
    public List<LocationTarget> getAll(LocationTargetFilter filter, Sorting sort, FetchStrategy ... fetchStrategy) {
        return getAll(filter, null, sort, fetchStrategy);
    }

    protected List<LocationTarget> getAll(LocationTargetFilter filter, Pagination page, Sorting sort, FetchStrategy ... fetchStrategy) {
        CriteriaQueryContainer container = createCriteriaQuery(fetchStrategy);
        CriteriaQuery<LocationTarget> criteriaQuery = container.getQuery();
        Root<LocationTarget> root = container.getRoot();

        Predicate predicate = getPredicate(root, filter);
        criteriaQuery = criteriaQuery.where(predicate);

        criteriaQuery = criteriaQuery.select(root);

        criteriaQuery = processOrderBy(criteriaQuery, root, sort);
        return findAll(criteriaQuery, page);
    }

}
