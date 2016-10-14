package com.byyd.middleware.account.dao.jpa;

import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Repository;

import com.adfonic.domain.TargetPublisher;
import com.adfonic.domain.TargetPublisher_;
import com.byyd.middleware.account.dao.TargetPublisherDao;
import com.byyd.middleware.account.filter.TargetPublisherFilter;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.Sorting;
import com.byyd.middleware.iface.dao.jpa.BusinessKeyDaoJpaImpl;
@Repository
public class TargetPublisherDaoJpaImpl extends BusinessKeyDaoJpaImpl<TargetPublisher> implements TargetPublisherDao{

    @Override
    public Long countAll(TargetPublisherFilter filter) {
        CriteriaBuilder criteriaBuilder = getTransactionalEntityManager().getCriteriaBuilder();
        CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
        Root<TargetPublisher> root = criteriaQuery.from(TargetPublisher.class);

        Predicate predicate = getPredicate(root, filter);
        criteriaQuery = criteriaQuery.where(predicate);

        criteriaQuery = criteriaQuery.select(criteriaBuilder.count(root));

        return executeLongAggregateFunction(criteriaQuery);
    }

    @Override
    public List<TargetPublisher> getAll(TargetPublisherFilter filter,
            FetchStrategy... fetchStrategy) {
        return getAll(filter,null,fetchStrategy);
    }
    
    @Override
    public List<TargetPublisher> getAll(TargetPublisherFilter filter, Sorting sort,
            FetchStrategy... fetchStrategy) {
        CriteriaQueryContainer container = createCriteriaQuery(fetchStrategy);
        CriteriaQuery<TargetPublisher> criteriaQuery = container.getQuery();
        Root<TargetPublisher> root = container.getRoot();
        CriteriaBuilder criteriaBuilder = getTransactionalEntityManager().getCriteriaBuilder();

        Predicate predicate = getPredicate(root, filter);
        if(predicate != null) {
            // Careful. NPE if nothing is set! I'd have allowed for null, myself, but...
            criteriaQuery = criteriaQuery.where(predicate);
        }

        criteriaQuery = criteriaQuery.select(root);

        criteriaQuery = processOrderBy(criteriaBuilder, criteriaQuery, root, sort);
        return findAll(criteriaQuery);
    }    
    
    protected Predicate getPredicate(Root<TargetPublisher> root, TargetPublisherFilter filter) {
        CriteriaBuilder criteriaBuilder = getTransactionalEntityManager().getCriteriaBuilder();
        Predicate rtbPredicate = null;
        Predicate pmpAvailablePredicate = null;
        Predicate namePredicate = null;
        Predicate publisherPredicate = null;
        Predicate hiddenPredicate = null;
        Predicate enableRtbBidSeat = null;
        
        hiddenPredicate = criteriaBuilder.equal(root.get(TargetPublisher_.hidden), filter.isHidden());
        
        if (filter.isRtb() != null) {
            rtbPredicate = criteriaBuilder.equal(root.get(TargetPublisher_.rtb), filter.isRtb());
        }
        if (filter.isPmpAvailable() != null) {
            pmpAvailablePredicate = criteriaBuilder.equal(root.get(TargetPublisher_.pmpAvailable), filter.isPmpAvailable());
        }
        if (!StringUtils.isEmpty(filter.getName())) {
            namePredicate = criteriaBuilder.equal(root.get(TargetPublisher_.name), filter.getName());
        }
        if (filter.getPublisher() != null) {
            publisherPredicate = criteriaBuilder.equal(root.get(TargetPublisher_.publisher),filter.getPublisher());
        }
        if (filter.getRtbSeatIdAvailable() != null) {
            enableRtbBidSeat = criteriaBuilder.equal(root.get(TargetPublisher_.rtbSeatIdAvailable), filter.getRtbSeatIdAvailable());
        }
 
        return and(hiddenPredicate, rtbPredicate, pmpAvailablePredicate, namePredicate, publisherPredicate, enableRtbBidSeat);
    }
    
}
