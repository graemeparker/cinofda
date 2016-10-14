package com.byyd.middleware.common.dao.jpa;

import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.stereotype.Repository;

import com.adfonic.domain.ContentType;
import com.adfonic.domain.ContentType_;
import com.byyd.middleware.common.dao.ContentTypeDao;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.Pagination;
import com.byyd.middleware.iface.dao.Sorting;
import com.byyd.middleware.iface.dao.jpa.BusinessKeyDaoJpaImpl;

@Repository
public class ContentTypeDaoJpaImpl extends BusinessKeyDaoJpaImpl<ContentType> implements ContentTypeDao {

    @Override
    public Long countAllForMimeType(String mimeType) {
        CriteriaBuilder criteriaBuilder = getTransactionalEntityManager().getCriteriaBuilder();
        CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
        Root<ContentType> root = criteriaQuery.from(ContentType.class);

        Predicate creativeExpression = criteriaBuilder.equal(root.get(ContentType_.mimeType), mimeType);
        criteriaQuery = criteriaQuery.where(creativeExpression);

        criteriaQuery = criteriaQuery.select(criteriaBuilder.count(root));

        return executeLongAggregateFunction(criteriaQuery);
    }

    @Override
    public List<ContentType> getAllForMimeType(String mimeType, FetchStrategy... fetchStrategy) {
        return getAllForMimeType(mimeType, null, null, fetchStrategy);
    }

    @Override
    public List<ContentType> getAllForMimeType(String mimeType, Sorting sort, FetchStrategy... fetchStrategy) {
        return getAllForMimeType(mimeType, null, sort, fetchStrategy);
    }

    @Override
    public List<ContentType> getAllForMimeType(String mimeType, Pagination page, FetchStrategy... fetchStrategy) {
        return getAllForMimeType(mimeType, page, page.getSorting(), fetchStrategy);
    }

    public List<ContentType> getAllForMimeType(String mimeType, Pagination page, Sorting sort, FetchStrategy... fetchStrategy) {
        CriteriaQueryContainer container = createCriteriaQuery(fetchStrategy);
        CriteriaQuery<ContentType> criteriaQuery = container.getQuery();
        Root<ContentType> root = container.getRoot();
        CriteriaBuilder criteriaBuilder = getTransactionalEntityManager().getCriteriaBuilder();

        Predicate creativeExpression = criteriaBuilder.equal(root.get(ContentType_.mimeType), mimeType);
        criteriaQuery = criteriaQuery.where(creativeExpression);

        criteriaQuery = criteriaQuery.select(root);

        criteriaQuery = processOrderBy(criteriaBuilder, criteriaQuery, root, sort);
        return findAll(criteriaQuery, page);
    }

    //----------------------------------------------------------------------------------------------------------------------

    @Override
    public ContentType getOneForMimeType(String mimeType, FetchStrategy... fetchStrategy) {
        CriteriaQueryContainer container = createCriteriaQuery(fetchStrategy);
        CriteriaQuery<ContentType> criteriaQuery = container.getQuery();
        Root<ContentType> root = container.getRoot();
        CriteriaBuilder criteriaBuilder = getTransactionalEntityManager().getCriteriaBuilder();

        Predicate creativeExpression = criteriaBuilder.equal(root.get(ContentType_.mimeType), mimeType);
        criteriaQuery = criteriaQuery.where(creativeExpression);

        criteriaQuery = criteriaQuery.select(root);

        return find(criteriaQuery);
    }

    @Override
    public ContentType getOneForMimeType(String mimeType, boolean animated, FetchStrategy... fetchStrategy) {
        CriteriaQueryContainer container = createCriteriaQuery(fetchStrategy);
        CriteriaQuery<ContentType> criteriaQuery = container.getQuery();
        Root<ContentType> root = container.getRoot();
        CriteriaBuilder criteriaBuilder = getTransactionalEntityManager().getCriteriaBuilder();

        Predicate mimeTypeExpression = criteriaBuilder.equal(root.get(ContentType_.mimeType), mimeType);
        Predicate animatedExpression = criteriaBuilder.equal(root.get(ContentType_.animated), animated);
        criteriaQuery = criteriaQuery.where(criteriaBuilder.and(mimeTypeExpression, animatedExpression));

        criteriaQuery = criteriaQuery.select(root);

        return find(criteriaQuery);
    }

    //----------------------------------------------------------------------------------------------------------------------

    @Override
    public Long countAllForMimeTypeLike(String mimeType) {
        CriteriaBuilder criteriaBuilder = getTransactionalEntityManager().getCriteriaBuilder();
        CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
        Root<ContentType> root = criteriaQuery.from(ContentType.class);

        Predicate creativeExpression = criteriaBuilder.like(root.get(ContentType_.mimeType), mimeType);
        criteriaQuery = criteriaQuery.where(creativeExpression);

        criteriaQuery = criteriaQuery.select(criteriaBuilder.count(root));

        return executeLongAggregateFunction(criteriaQuery);
    }

    @Override
    public List<ContentType> getAllForMimeTypeLike(String mimeType, FetchStrategy... fetchStrategy) {
        return getAllForMimeTypeLike(mimeType, null, null, fetchStrategy);
    }

    @Override
    public List<ContentType> getAllForMimeTypeLike(String mimeType, Sorting sort, FetchStrategy... fetchStrategy) {
        return getAllForMimeTypeLike(mimeType, null, sort, fetchStrategy);
    }

    @Override
    public List<ContentType> getAllForMimeTypeLike(String mimeType, Pagination page, FetchStrategy... fetchStrategy) {
        return getAllForMimeTypeLike(mimeType, page, page.getSorting(), fetchStrategy);
    }

    protected List<ContentType> getAllForMimeTypeLike(String mimeType, Pagination page, Sorting sort, FetchStrategy... fetchStrategy) {
        CriteriaQueryContainer container = createCriteriaQuery(fetchStrategy);
        CriteriaQuery<ContentType> criteriaQuery = container.getQuery();
        Root<ContentType> root = container.getRoot();
        CriteriaBuilder criteriaBuilder = getTransactionalEntityManager().getCriteriaBuilder();

        Predicate creativeExpression = criteriaBuilder.like(root.get(ContentType_.mimeType), mimeType);
        criteriaQuery = criteriaQuery.where(creativeExpression);

        criteriaQuery = criteriaQuery.select(root);

        criteriaQuery = processOrderBy(criteriaBuilder, criteriaQuery, root, sort);
        return findAll(criteriaQuery, page);
    }


}
