package com.byyd.middleware.publication.dao.jpa;

import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.stereotype.Repository;

import com.adfonic.domain.Publication;
import com.adfonic.domain.PublicationHistory;
import com.adfonic.domain.PublicationHistory_;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.Pagination;
import com.byyd.middleware.iface.dao.Sorting;
import com.byyd.middleware.iface.dao.jpa.BusinessKeyDaoJpaImpl;
import com.byyd.middleware.publication.dao.PublicationHistoryDao;

@Repository
public class PublicationHistoryDaoJpaImpl extends BusinessKeyDaoJpaImpl<PublicationHistory> implements PublicationHistoryDao {
    @Override
    public List<PublicationHistory> getAll(Publication publication, FetchStrategy ... fetchStrategy) {
        return getAll(publication, null, null, fetchStrategy);
    }

    @Override
    public List<PublicationHistory> getAll(Publication publication, Sorting sort, FetchStrategy ... fetchStrategy) {
        return getAll(publication, null, sort, fetchStrategy);
    }

    @Override
    public List<PublicationHistory> getAll(Publication publication, Pagination page, FetchStrategy ... fetchStrategy) {
        return getAll(publication, page, page.getSorting(), fetchStrategy);
    }

    protected List<PublicationHistory> getAll(Publication publication, Pagination page, Sorting sort, FetchStrategy... fetchStrategy) {
        CriteriaQueryContainer container = createCriteriaQuery(fetchStrategy);
        CriteriaQuery<PublicationHistory> criteriaQuery = container.getQuery();
        Root<PublicationHistory> root = container.getRoot();
        CriteriaBuilder criteriaBuilder = getTransactionalEntityManager().getCriteriaBuilder();

        Predicate publicationExpression = criteriaBuilder.equal(root.get(PublicationHistory_.publication), publication);
        criteriaQuery = criteriaQuery.where(publicationExpression);

        criteriaQuery = criteriaQuery.select(root);

        criteriaQuery = processOrderBy(criteriaBuilder, criteriaQuery, root, sort);
        return findAll(criteriaQuery, page);
    }
}
