package com.byyd.middleware.account.dao.jpa;

import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.stereotype.Repository;

import com.adfonic.domain.Publisher;
import com.adfonic.domain.PublisherRevShare;
import com.adfonic.domain.PublisherRevShare_;
import com.byyd.middleware.account.dao.PublisherRevShareDao;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.Pagination;
import com.byyd.middleware.iface.dao.Sorting;
import com.byyd.middleware.iface.dao.jpa.BusinessKeyDaoJpaImpl;

@Repository
public class PublisherRevShareDaoJpaImpl extends BusinessKeyDaoJpaImpl<PublisherRevShare> implements PublisherRevShareDao {

    @Override
    public Long countAllForPublisher(Publisher publisher) {
        CriteriaBuilder criteriaBuilder = getTransactionalEntityManager().getCriteriaBuilder();
        CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
        Root<PublisherRevShare> root = criteriaQuery.from(PublisherRevShare.class);

        Predicate publisherExpression = criteriaBuilder.equal(root.get(PublisherRevShare_.publisher), publisher);
        criteriaQuery = criteriaQuery.where(publisherExpression);

        criteriaQuery = criteriaQuery.select(criteriaBuilder.count(root));

        return executeLongAggregateFunction(criteriaQuery);
    }

    @Override
    public List<PublisherRevShare> getAllForPublisher(Publisher publisher, FetchStrategy... fetchStrategy) {
        return this.getAllForPublisher(publisher, null, null, fetchStrategy);
    }

    @Override
    public List<PublisherRevShare> getAllForPublisher(Publisher publisher, Sorting sort, FetchStrategy... fetchStrategy) {
        return this.getAllForPublisher(publisher, null, sort, fetchStrategy);
    }

    @Override
    public List<PublisherRevShare> getAllForPublisher(Publisher publisher, Pagination page, FetchStrategy... fetchStrategy) {
        return this.getAllForPublisher(publisher, page, page.getSorting(), fetchStrategy);
    }

    protected List<PublisherRevShare> getAllForPublisher(Publisher publisher, Pagination page, Sorting sort, FetchStrategy... fetchStrategy) {
        CriteriaQueryContainer container = createCriteriaQuery(fetchStrategy);
        CriteriaQuery<PublisherRevShare> criteriaQuery = container.getQuery();
        Root<PublisherRevShare> root = container.getRoot();
        CriteriaBuilder criteriaBuilder = getTransactionalEntityManager().getCriteriaBuilder();

        Predicate publisherExpression = criteriaBuilder.equal(root.get(PublisherRevShare_.publisher), publisher);
        criteriaQuery = criteriaQuery.where(publisherExpression);

        criteriaQuery = criteriaQuery.select(root);

        criteriaQuery = processOrderBy(criteriaBuilder, criteriaQuery, root, sort);
        return findAll(criteriaQuery, page);
    }


}
