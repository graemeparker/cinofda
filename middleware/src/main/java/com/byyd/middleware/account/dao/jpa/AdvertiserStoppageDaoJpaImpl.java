package com.byyd.middleware.account.dao.jpa;

import java.util.Date;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.stereotype.Repository;

import com.adfonic.domain.Advertiser;
import com.adfonic.domain.AdvertiserStoppage;
import com.adfonic.domain.AdvertiserStoppage_;
import com.byyd.middleware.account.dao.AdvertiserStoppageDao;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.jpa.BusinessKeyDaoJpaImpl;

@Repository
public class AdvertiserStoppageDaoJpaImpl extends BusinessKeyDaoJpaImpl<AdvertiserStoppage> implements AdvertiserStoppageDao {

    @Override
    @SuppressWarnings("unchecked")
    public List<Object[]> getFieldsForNullOrFutureReactivateDate() {
        return super.findByNativeQueryPositionalParameters(
                "SELECT ADVERTISER_ID, TIMESTAMP, REACTIVATE_DATE"
                + " FROM ADVERTISER_STOPPAGE"
                + " WHERE REACTIVATE_DATE IS NULL OR REACTIVATE_DATE > CURRENT_TIMESTAMP");
    }

    @Override
    public List<AdvertiserStoppage> getAllForReactivateDateIsNullOrReactivateDateGreaterThan(Date reactivateDate, FetchStrategy... fetchStrategy) {
        CriteriaQueryContainer container = createCriteriaQuery(fetchStrategy);
        CriteriaQuery<AdvertiserStoppage> criteriaQuery = container.getQuery();
        Root<AdvertiserStoppage> root = container.getRoot();
        CriteriaBuilder criteriaBuilder = getTransactionalEntityManager().getCriteriaBuilder();

        Predicate isNullPredicate = criteriaBuilder.isNull(root.get(AdvertiserStoppage_.reactivateDate));
        Predicate isGreaterThanPredicate = criteriaBuilder.greaterThan(root.get(AdvertiserStoppage_.reactivateDate), reactivateDate);
        criteriaQuery = criteriaQuery.where(or(isNullPredicate, isGreaterThanPredicate));

        criteriaQuery = criteriaQuery.select(root);

        return findAll(criteriaQuery);

    }

    @Override
    public List<AdvertiserStoppage> getAllForAdvertiserAndReactivateDateIsNullOrReactivateDateGreaterThan(Advertiser advertiser, Date reactivateDate, FetchStrategy... fetchStrategy) {
        CriteriaQueryContainer container = createCriteriaQuery(fetchStrategy);
        CriteriaQuery<AdvertiserStoppage> criteriaQuery = container.getQuery();
        Root<AdvertiserStoppage> root = container.getRoot();
        CriteriaBuilder criteriaBuilder = getTransactionalEntityManager().getCriteriaBuilder();

        Predicate advetiserPredicate = criteriaBuilder.equal(root.get(AdvertiserStoppage_.advertiser), advertiser);
        Predicate isNullPredicate = criteriaBuilder.isNull(root.get(AdvertiserStoppage_.reactivateDate));
        Predicate isGreaterThanPredicate = criteriaBuilder.greaterThan(root.get(AdvertiserStoppage_.reactivateDate), reactivateDate);

        criteriaQuery = criteriaQuery.where(
                and(
                    advetiserPredicate,
                    or(
                        isNullPredicate,
                        isGreaterThanPredicate
                    )
                )
        );

        criteriaQuery = criteriaQuery.select(root);

        return findAll(criteriaQuery);
    }
}
