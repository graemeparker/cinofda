package com.byyd.middleware.campaign.dao.jpa;

import java.util.Date;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.stereotype.Repository;

import com.adfonic.domain.Campaign;
import com.adfonic.domain.CampaignStoppage;
import com.adfonic.domain.CampaignStoppage_;
import com.byyd.middleware.campaign.dao.CampaignStoppageDao;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.jpa.BusinessKeyDaoJpaImpl;

@Repository
public class CampaignStoppageDaoJpaImpl extends BusinessKeyDaoJpaImpl<CampaignStoppage> implements CampaignStoppageDao {

    @Override
    @SuppressWarnings("unchecked")
    public List<Object[]> getFieldsForNullOrFutureReactivateDate() {
        return super.findByNativeQueryPositionalParameters(
                "SELECT CAMPAIGN_ID, TIMESTAMP, REACTIVATE_DATE"
                + " FROM CAMPAIGN_STOPPAGE"
                + " WHERE REACTIVATE_DATE IS NULL OR REACTIVATE_DATE > CURRENT_TIMESTAMP");
    }

    @Override
    public List<CampaignStoppage> getAllForReactivateDateIsNullOrReactivateDateGreaterThan(Date reactivateDate, FetchStrategy... fetchStrategy) {
        CriteriaQueryContainer container = createCriteriaQuery(fetchStrategy);
        CriteriaQuery<CampaignStoppage> criteriaQuery = container.getQuery();
        Root<CampaignStoppage> root = container.getRoot();
        CriteriaBuilder criteriaBuilder = getTransactionalEntityManager().getCriteriaBuilder();

        Predicate isNullPredicate = criteriaBuilder.isNull(root.get(CampaignStoppage_.reactivateDate));
        Predicate isGreaterThanPredicate = criteriaBuilder.greaterThan(root.get(CampaignStoppage_.reactivateDate), reactivateDate);
        criteriaQuery = criteriaQuery.where(or(isNullPredicate, isGreaterThanPredicate));

        criteriaQuery = criteriaQuery.select(root);

        return findAll(criteriaQuery);

    }

    @Override
    public List<CampaignStoppage> getAllForCampaignAndReactivateDateIsNullOrReactivateDateGreaterThan(Campaign campaign, Date reactivateDate, FetchStrategy... fetchStrategy) {
        CriteriaQueryContainer container = createCriteriaQuery(fetchStrategy);
        CriteriaQuery<CampaignStoppage> criteriaQuery = container.getQuery();
        Root<CampaignStoppage> root = container.getRoot();
        CriteriaBuilder criteriaBuilder = getTransactionalEntityManager().getCriteriaBuilder();

        Predicate campaignPredicate = criteriaBuilder.equal(root.get(CampaignStoppage_.campaign), campaign);
        Predicate isNullPredicate = criteriaBuilder.isNull(root.get(CampaignStoppage_.reactivateDate));
        Predicate isGreaterThanPredicate = criteriaBuilder.greaterThan(root.get(CampaignStoppage_.reactivateDate), reactivateDate);
        criteriaQuery = criteriaQuery.where(
                and(
                    campaignPredicate,
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
