package com.byyd.middleware.creative.dao.jpa;

import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.stereotype.Repository;

import com.adfonic.domain.Creative;
import com.adfonic.domain.CreativeHistory;
import com.adfonic.domain.CreativeHistory_;
import com.byyd.middleware.creative.dao.CreativeHistoryDao;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.Pagination;
import com.byyd.middleware.iface.dao.Sorting;
import com.byyd.middleware.iface.dao.jpa.BusinessKeyDaoJpaImpl;

@Repository
public class CreativeHistoryDaoJpaImpl extends BusinessKeyDaoJpaImpl<CreativeHistory> implements CreativeHistoryDao {
    @Override
    public List<CreativeHistory> getAll(Creative creative, FetchStrategy ... fetchStrategy) {
        return getAll(creative, null, null, fetchStrategy);
    }

    @Override
    public List<CreativeHistory> getAll(Creative creative, Sorting sort, FetchStrategy ... fetchStrategy) {
        return getAll(creative, null, sort, fetchStrategy);
    }

    @Override
    public List<CreativeHistory> getAll(Creative creative, Pagination page, FetchStrategy ... fetchStrategy) {
        return getAll(creative, page, page.getSorting(), fetchStrategy);
    }

    protected List<CreativeHistory> getAll(Creative creative, Pagination page, Sorting sort, FetchStrategy... fetchStrategy) {
        CriteriaQueryContainer container = createCriteriaQuery(fetchStrategy);
        CriteriaQuery<CreativeHistory> criteriaQuery = container.getQuery();
        Root<CreativeHistory> root = container.getRoot();
        CriteriaBuilder criteriaBuilder = getTransactionalEntityManager().getCriteriaBuilder();

        Predicate creativeExpression = criteriaBuilder.equal(root.get(CreativeHistory_.creative), creative);
        criteriaQuery = criteriaQuery.where(creativeExpression);

        criteriaQuery = criteriaQuery.select(root);

        criteriaQuery = processOrderBy(criteriaBuilder, criteriaQuery, root, sort);
        return findAll(criteriaQuery, page);
    }
}
