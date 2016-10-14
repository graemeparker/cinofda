package com.byyd.middleware.publication.dao.jpa;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.stereotype.Repository;

import com.adfonic.domain.BidType;
import com.adfonic.domain.DefaultRateCard;
import com.adfonic.domain.DefaultRateCard_;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.jpa.BusinessKeyDaoJpaImpl;
import com.byyd.middleware.publication.dao.DefaultRateCardDao;

@Repository
public class DefaultRateCardDaoJpaImpl extends BusinessKeyDaoJpaImpl<DefaultRateCard> implements DefaultRateCardDao {

    @Override
    public DefaultRateCard getByBidType(BidType bidType, FetchStrategy... fetchStrategy) {
        CriteriaQueryContainer container = createCriteriaQuery(fetchStrategy);
        CriteriaQuery<DefaultRateCard> criteriaQuery = container.getQuery();
        Root<DefaultRateCard> root = container.getRoot();
        CriteriaBuilder criteriaBuilder = getTransactionalEntityManager().getCriteriaBuilder();

        Predicate bidTypeExpression = criteriaBuilder.equal(root.get(DefaultRateCard_.bidType), bidType);
        criteriaQuery = criteriaQuery.where(bidTypeExpression);

        criteriaQuery = criteriaQuery.select(root);

        return find(criteriaQuery);
    }
}
