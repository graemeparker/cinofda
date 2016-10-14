package com.byyd.middleware.creative.dao.jpa;

import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.stereotype.Repository;

import com.adfonic.domain.ExtendedCreativeType;
import com.adfonic.domain.ExtendedCreativeType_;
import com.byyd.middleware.creative.dao.ExtendedCreativeTypeDao;
import com.byyd.middleware.creative.filter.ExtendedCreativeTypeFilter;
import com.byyd.middleware.creative.filter.ExtendedCreativeTypeFilter.VisibilityEnum;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.Pagination;
import com.byyd.middleware.iface.dao.Sorting;
import com.byyd.middleware.iface.dao.jpa.BusinessKeyDaoJpaImpl;

@Repository
public class ExtendedCreativeTypeDaoJpaImpl extends BusinessKeyDaoJpaImpl<ExtendedCreativeType> implements ExtendedCreativeTypeDao {
    
    protected Predicate getPredicate(Root<ExtendedCreativeType> root, ExtendedCreativeTypeFilter filter) {
        Predicate featuresMustHavePredicate = null;
        Predicate featuresMustNotHavePredicate = null;
        Predicate visibilityPredicate = null;
        Predicate mediaTypesMustHavePredicate = null;
        Predicate mediaTypesMustNotHavePredicate = null;

        CriteriaBuilder criteriaBuilder = getTransactionalEntityManager().getCriteriaBuilder();

        // Features
        if (filter.getFeaturesMustHave() != null) {
            featuresMustHavePredicate = root.join(ExtendedCreativeType_.features, JoinType.INNER).in(filter.getFeaturesMustHave());
        } else if (filter.getFeaturesMustNotHave() != null) {
            featuresMustNotHavePredicate = criteriaBuilder.not(root.join(ExtendedCreativeType_.features, JoinType.INNER).in(filter.getFeaturesMustNotHave()));
        }

        // MediaTypes
        if (filter.getMediaTypesMustHave() != null) {
            mediaTypesMustHavePredicate = root.get(ExtendedCreativeType_.mediaType).in(filter.getMediaTypesMustHave());
        } else if (filter.getMediaTypesMustNotHave() != null) {
            mediaTypesMustNotHavePredicate = criteriaBuilder.not(root.get(ExtendedCreativeType_.mediaType).in(filter.getMediaTypesMustNotHave()));
        }

        // Visibility (hidden field)
        if (VisibilityEnum.HIDDEN == filter.getVisibility()) {
            visibilityPredicate = criteriaBuilder.isTrue(root.get(ExtendedCreativeType_.hidden));
        } else if (VisibilityEnum.NOT_HIDDEN == filter.getVisibility()) {
            visibilityPredicate = criteriaBuilder.isFalse(root.get(ExtendedCreativeType_.hidden));
        }

        return and(featuresMustHavePredicate, featuresMustNotHavePredicate, mediaTypesMustHavePredicate, mediaTypesMustNotHavePredicate, visibilityPredicate);
    }
    
    
    @Override
    public Long countAll(ExtendedCreativeTypeFilter filter) {
        CriteriaBuilder criteriaBuilder = getTransactionalEntityManager().getCriteriaBuilder();
        CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
        Root<ExtendedCreativeType> root = criteriaQuery.from(ExtendedCreativeType.class);

        Predicate predicate = getPredicate(root, filter);
        criteriaQuery = criteriaQuery.where(predicate);

        criteriaQuery = criteriaQuery.select(criteriaBuilder.count(root));

        return executeLongAggregateFunction(criteriaQuery);
    }

    @Override
    public List<ExtendedCreativeType> findAll(ExtendedCreativeTypeFilter filter, FetchStrategy... fetchStrategy) {
        return findAll(filter, null, null, fetchStrategy);
    }

    @Override
    public List<ExtendedCreativeType> findAll(ExtendedCreativeTypeFilter filter, Sorting sort, FetchStrategy... fetchStrategy) {
        return findAll(filter, null, sort, fetchStrategy);
    }

    @Override
    public List<ExtendedCreativeType> findAll(ExtendedCreativeTypeFilter filter, Pagination page, FetchStrategy... fetchStrategy) {
        return findAll(filter, page, page.getSorting(), fetchStrategy);
    }

    @Override
    public List<ExtendedCreativeType> findAll(ExtendedCreativeTypeFilter filter, Pagination page, Sorting sort, FetchStrategy... fetchStrategy) {
        CriteriaQueryContainer container = createCriteriaQuery(fetchStrategy);
        CriteriaQuery<ExtendedCreativeType> criteriaQuery = container.getQuery();
        Root<ExtendedCreativeType> root = container.getRoot();
        CriteriaBuilder criteriaBuilder = getTransactionalEntityManager().getCriteriaBuilder();

        Predicate predicate = getPredicate(root, filter);
        if (predicate!=null){
            criteriaQuery = criteriaQuery.where(predicate);
        }

        criteriaQuery = criteriaQuery.select(root);

        criteriaQuery = processOrderBy(criteriaBuilder, criteriaQuery, root, sort);
        return findAll(criteriaQuery, page);
    }
}
