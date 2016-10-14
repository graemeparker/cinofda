package com.byyd.middleware.account.dao.jpa;

import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.stereotype.Repository;

import com.adfonic.domain.PrivateMarketPlaceDeal;
import com.adfonic.domain.PrivateMarketPlaceDeal_;
import com.adfonic.domain.Publisher;
import com.byyd.middleware.account.dao.PrivateMarketPlaceDealDao;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.Pagination;
import com.byyd.middleware.iface.dao.Sorting;
import com.byyd.middleware.iface.dao.jpa.BusinessKeyDaoJpaImpl;

@Repository
public class PrivateMarketPlaceDealDaoJpaImpl extends BusinessKeyDaoJpaImpl<PrivateMarketPlaceDeal> implements PrivateMarketPlaceDealDao {

    @Override
    public Long countAllForPublisher(Publisher publisher) {
        CriteriaBuilder criteriaBuilder = getTransactionalEntityManager().getCriteriaBuilder();
        CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
        Root<PrivateMarketPlaceDeal> root = criteriaQuery.from(PrivateMarketPlaceDeal.class);
        
        Predicate publisherExpression = criteriaBuilder.equal(root.get(PrivateMarketPlaceDeal_.publisher), publisher);
        criteriaQuery = criteriaQuery.where(publisherExpression);
        
        criteriaQuery = criteriaQuery.select(criteriaBuilder.count(root));
        
        return executeLongAggregateFunction(criteriaQuery);
    }
    
    @Override
    public List<PrivateMarketPlaceDeal> getAllForPublisher(Publisher publisher, FetchStrategy... fetchStrategy) {
        return this.getAllForPublisher(publisher, null, null, fetchStrategy);
    }
    
    @Override
    public List<PrivateMarketPlaceDeal> getAllForPublisher(Publisher publisher, Sorting sort, FetchStrategy... fetchStrategy) {
        return this.getAllForPublisher(publisher, null, sort, fetchStrategy);
    }
    
    @Override
    public List<PrivateMarketPlaceDeal> getAllForPublisher(Publisher publisher, Pagination page, FetchStrategy... fetchStrategy) {
        return this.getAllForPublisher(publisher, page, page.getSorting(), fetchStrategy);
    }

    protected List<PrivateMarketPlaceDeal> getAllForPublisher(Publisher publisher, Pagination page, Sorting sort, FetchStrategy... fetchStrategy) {
        CriteriaQueryContainer container = createCriteriaQuery(fetchStrategy);
        CriteriaQuery<PrivateMarketPlaceDeal> criteriaQuery = container.getQuery();
        Root<PrivateMarketPlaceDeal> root = container.getRoot();
        CriteriaBuilder criteriaBuilder = getTransactionalEntityManager().getCriteriaBuilder();

        Predicate publisherExpression = criteriaBuilder.equal(root.get(PrivateMarketPlaceDeal_.publisher), publisher);
        criteriaQuery = criteriaQuery.where(publisherExpression);

        criteriaQuery = criteriaQuery.select(root);

        criteriaQuery = processOrderBy(criteriaBuilder, criteriaQuery, root, sort);
        return findAll(criteriaQuery, page);
    }
    
    //---------------------------------------------------------------------------------------------------------------------------
    
    @Override
    public PrivateMarketPlaceDeal getByPublisherAndDealId(Publisher publisher, String dealId, FetchStrategy... fetchStrategy) {
        CriteriaQueryContainer container = createCriteriaQuery(fetchStrategy);
        CriteriaQuery<PrivateMarketPlaceDeal> criteriaQuery = container.getQuery();
        Root<PrivateMarketPlaceDeal> root = container.getRoot();
        CriteriaBuilder criteriaBuilder = getTransactionalEntityManager().getCriteriaBuilder();

        Predicate publisherPredicate = criteriaBuilder.equal(root.get(PrivateMarketPlaceDeal_.publisher), publisher);
        Predicate dealIdPredicate = criteriaBuilder.equal(root.get(PrivateMarketPlaceDeal_.dealId), dealId);

        criteriaQuery = criteriaQuery.where(and(publisherPredicate, dealIdPredicate));

        criteriaQuery = criteriaQuery.select(root);
        return find(criteriaQuery);

    }

}
