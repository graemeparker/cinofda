package com.byyd.middleware.creative.dao.jpa;

import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.stereotype.Repository;

import com.adfonic.domain.Asset;
import com.adfonic.domain.Asset_;
import com.adfonic.domain.ContentType;
import com.adfonic.domain.Creative;
import com.byyd.middleware.creative.dao.AssetDao;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.Pagination;
import com.byyd.middleware.iface.dao.Sorting;
import com.byyd.middleware.iface.dao.jpa.BusinessKeyDaoJpaImpl;

@Repository
public class AssetDaoJpaImpl extends BusinessKeyDaoJpaImpl<Asset> implements AssetDao {

    @Override
    public Long countAllForCreative(Creative creative) {
        CriteriaBuilder criteriaBuilder = getTransactionalEntityManager().getCriteriaBuilder();
        CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
        Root<Asset> root = criteriaQuery.from(Asset.class);

        Predicate creativeExpression = criteriaBuilder.equal(root.get(Asset_.creative), creative);
        criteriaQuery = criteriaQuery.where(creativeExpression);

        criteriaQuery = criteriaQuery.select(criteriaBuilder.count(root));

        return executeLongAggregateFunction(criteriaQuery);
    }

    @Override
    public List<Asset> findAllByCreative(Creative creative, FetchStrategy... fetchStrategy) {
        return findAllByCreative(creative, null, null, fetchStrategy);
    }

    @Override
    public List<Asset> findAllByCreative(Creative creative, Sorting sort, FetchStrategy... fetchStrategy) {
        return findAllByCreative(creative, null, sort, fetchStrategy);
    }

    @Override
    public List<Asset> findAllByCreative(Creative creative, Pagination page, FetchStrategy... fetchStrategy) {
        return findAllByCreative(creative, page, page.getSorting(), fetchStrategy);
    }

    protected List<Asset> findAllByCreative(Creative creative, Pagination page, Sorting sort, FetchStrategy... fetchStrategy) {
        CriteriaQueryContainer container = createCriteriaQuery(fetchStrategy);
        CriteriaQuery<Asset> criteriaQuery = container.getQuery();
        Root<Asset> root = container.getRoot();
        CriteriaBuilder criteriaBuilder = getTransactionalEntityManager().getCriteriaBuilder();

        Predicate creativeExpression = criteriaBuilder.equal(root.get(Asset_.creative), creative);
        criteriaQuery = criteriaQuery.where(creativeExpression);

        criteriaQuery = criteriaQuery.select(root);

        criteriaQuery = processOrderBy(criteriaBuilder, criteriaQuery, root, sort);
        return findAll(criteriaQuery, page);
    }

    @Override
    public Long countAllForCreativeAndContentType(Creative creative, ContentType contentType) {
        CriteriaBuilder criteriaBuilder = getTransactionalEntityManager().getCriteriaBuilder();
        CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
        Root<Asset> root = criteriaQuery.from(Asset.class);

        Predicate creativeExpression = criteriaBuilder.equal(root.get(Asset_.creative), creative);
        Predicate contentTypeExpression = criteriaBuilder.equal(root.get(Asset_.contentType), contentType);
        criteriaQuery = criteriaQuery.where(and(creativeExpression, contentTypeExpression));

        criteriaQuery = criteriaQuery.select(criteriaBuilder.count(root));

        return executeLongAggregateFunction(criteriaQuery);
    }

    @Override
    public List<Asset> findAllByCreativeAndContentType(Creative creative, ContentType contentType, FetchStrategy... fetchStrategy) {
        return findAllByCreativeAndContentType(creative, contentType, null, null, fetchStrategy);
    }

    @Override
    public List<Asset> findAllByCreativeAndContentType(Creative creative, ContentType contentType, Sorting sort, FetchStrategy... fetchStrategy) {
        return findAllByCreativeAndContentType(creative, contentType, null, sort, fetchStrategy);
    }

    @Override
    public List<Asset> findAllByCreativeAndContentType(Creative creative, ContentType contentType, Pagination page, FetchStrategy... fetchStrategy) {
        return findAllByCreativeAndContentType(creative, contentType, page, page.getSorting(), fetchStrategy);
    }

    protected List<Asset> findAllByCreativeAndContentType(Creative creative, ContentType contentType, Pagination page, Sorting sort, FetchStrategy... fetchStrategy) {
        CriteriaQueryContainer container = createCriteriaQuery(fetchStrategy);
        CriteriaQuery<Asset> criteriaQuery = container.getQuery();
        Root<Asset> root = container.getRoot();
        CriteriaBuilder criteriaBuilder = getTransactionalEntityManager().getCriteriaBuilder();

        Predicate creativeExpression = criteriaBuilder.equal(root.get(Asset_.creative), creative);
        Predicate contentTypeExpression = criteriaBuilder.equal(root.get(Asset_.contentType), contentType);
        criteriaQuery = criteriaQuery.where(and(creativeExpression, contentTypeExpression));

        criteriaQuery = criteriaQuery.select(root);

        criteriaQuery = processOrderBy(criteriaBuilder, criteriaQuery, root, sort);
        return findAll(criteriaQuery, page);
    }
}
