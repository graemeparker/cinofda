package com.byyd.middleware.account.dao.jpa;

import java.util.Collection;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.stereotype.Repository;

import com.adfonic.domain.BidSeat;
import com.adfonic.domain.BidSeat_;
import com.byyd.middleware.account.dao.BidSeatDao;
import com.byyd.middleware.account.filter.BidSeatFilter;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.Pagination;
import com.byyd.middleware.iface.dao.Sorting;
import com.byyd.middleware.iface.dao.jpa.BusinessKeyDaoJpaImpl;

@Repository
public class BidSeatDaoJpaImpl extends BusinessKeyDaoJpaImpl<BidSeat> implements BidSeatDao {

    @Override
    public void deleteAll(Collection<BidSeat> bidSeats){
        for(BidSeat bidSeat : bidSeats){
            delete(bidSeat);
        }
    }
    
    @Override
    public Long countAll(BidSeatFilter filter) {
        CriteriaBuilder criteriaBuilder = getTransactionalEntityManager().getCriteriaBuilder();
        CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
        Root<BidSeat> root = criteriaQuery.from(BidSeat.class);

        Predicate predicate = getPredicate(root, filter);
        criteriaQuery = criteriaQuery.where(predicate);

        criteriaQuery = criteriaQuery.select(criteriaBuilder.count(root));

        return executeLongAggregateFunction(criteriaQuery);
    }

    @Override
    public List<BidSeat> getAll(BidSeatFilter filter, FetchStrategy... fetchStrategy) {
        return getAll(filter, null, null, fetchStrategy);
    }

    @Override
    public List<BidSeat> getAll(BidSeatFilter filter, Sorting sort, FetchStrategy... fetchStrategy) {
        return getAll(filter, null, sort, fetchStrategy);
    }

    @Override
    public List<BidSeat> getAll(BidSeatFilter filter, Pagination page, FetchStrategy... fetchStrategy) {
        return getAll(filter, page, page.getSorting(), fetchStrategy);
    }

    @Override
    public List<BidSeat> getAll(BidSeatFilter filter, Pagination page, Sorting sort, FetchStrategy... fetchStrategy) {
        CriteriaQueryContainer container = createCriteriaQuery(fetchStrategy);
        CriteriaQuery<BidSeat> criteriaQuery = container.getQuery();
        Root<BidSeat> root = container.getRoot();
        CriteriaBuilder criteriaBuilder = getTransactionalEntityManager().getCriteriaBuilder();

        Predicate predicate = getPredicate(root, filter);
        criteriaQuery = criteriaQuery.where(predicate);

        criteriaQuery = criteriaQuery.select(root);

        criteriaQuery = processOrderBy(criteriaBuilder, criteriaQuery, root, sort);
        return findAll(criteriaQuery, page);
    }
    
    protected Predicate getPredicate(Root<BidSeat> root, BidSeatFilter filter) {
        CriteriaBuilder criteriaBuilder = getTransactionalEntityManager().getCriteriaBuilder();
        Predicate seatIdExpression = null;
        Predicate targetPublisherExpression = null;
        

        if(filter.getSeatId() != null) {
            seatIdExpression = criteriaBuilder.equal(root.get(BidSeat_.seatId), filter.getSeatId());
        }
        
        if(filter.getTargetPublisher() != null) {
            targetPublisherExpression = criteriaBuilder.equal(root.get(BidSeat_.targetPublisher), filter.getTargetPublisher());
        }


        return and(seatIdExpression, targetPublisherExpression);
    }
}
