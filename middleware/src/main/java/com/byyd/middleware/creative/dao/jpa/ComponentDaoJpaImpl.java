package com.byyd.middleware.creative.dao.jpa;

import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.stereotype.Repository;

import com.adfonic.domain.Component;
import com.adfonic.domain.Component_;
import com.adfonic.domain.Format;
import com.byyd.middleware.creative.dao.ComponentDao;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.Pagination;
import com.byyd.middleware.iface.dao.Sorting;
import com.byyd.middleware.iface.dao.jpa.BusinessKeyDaoJpaImpl;

@Repository
public class ComponentDaoJpaImpl extends BusinessKeyDaoJpaImpl<Component> implements ComponentDao {

    @Override
    public Long countAllForFormat(Format format) {
        CriteriaBuilder criteriaBuilder = getTransactionalEntityManager().getCriteriaBuilder();
        CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
        Root<Component> root = criteriaQuery.from(Component.class);

        Predicate formatExpression = criteriaBuilder.equal(root.get(Component_.format), format);
        criteriaQuery = criteriaQuery.where(formatExpression);

        criteriaQuery = criteriaQuery.select(criteriaBuilder.count(root));

        return executeLongAggregateFunction(criteriaQuery);
    }

    @Override
    public List<Component> findAllByFormat(Format format, FetchStrategy... fetchStrategy) {
        return findAllByFormat(format, null, null, fetchStrategy);
    }

    @Override
    public List<Component> findAllByFormat(Format format, Sorting sort, FetchStrategy... fetchStrategy) {
        return findAllByFormat(format, null, sort, fetchStrategy);
    }

    @Override
    public List<Component> findAllByFormat(Format format, Pagination page, FetchStrategy... fetchStrategy) {
        return findAllByFormat(format, page, page.getSorting(), fetchStrategy);
    }

    protected List<Component> findAllByFormat(Format format, Pagination page, Sorting sort, FetchStrategy... fetchStrategy) {
        CriteriaQueryContainer container = createCriteriaQuery(fetchStrategy);
        CriteriaQuery<Component> criteriaQuery = container.getQuery();
        Root<Component> root = container.getRoot();
        CriteriaBuilder criteriaBuilder = getTransactionalEntityManager().getCriteriaBuilder();

        Predicate formatExpression = criteriaBuilder.equal(root.get(Component_.format), format);
        criteriaQuery = criteriaQuery.where(formatExpression);

        criteriaQuery = criteriaQuery.select(root);

        criteriaQuery = processOrderBy(criteriaBuilder, criteriaQuery, root, sort);
        return findAll(criteriaQuery, page);
    }

}
