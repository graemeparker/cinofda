package com.byyd.middleware.campaign.dao.jpa;

import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.stereotype.Repository;

import com.adfonic.domain.Campaign;
import com.adfonic.domain.CampaignTimePeriod;
import com.adfonic.domain.CampaignTimePeriod_;
import com.byyd.middleware.campaign.dao.CampaignTimePeriodDao;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.Pagination;
import com.byyd.middleware.iface.dao.Sorting;
import com.byyd.middleware.iface.dao.jpa.BusinessKeyDaoJpaImpl;

@Repository
public class CampaignTimePeriodDaoJpaImpl extends BusinessKeyDaoJpaImpl<CampaignTimePeriod> implements CampaignTimePeriodDao {

    protected Predicate getAllForCampaignPredicate(Root<CampaignTimePeriod> root, Campaign campaign) {
        CriteriaBuilder criteriaBuilder = getTransactionalEntityManager().getCriteriaBuilder();
        return criteriaBuilder.equal(root.get(CampaignTimePeriod_.campaign), campaign);
    }

    @Override
    public Long countAllForCampaign(Campaign campaign) {
        CriteriaBuilder criteriaBuilder = getTransactionalEntityManager().getCriteriaBuilder();
        CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
        Root<CampaignTimePeriod> root = criteriaQuery.from(CampaignTimePeriod.class);

        Predicate predicate = getAllForCampaignPredicate(root, campaign);
        criteriaQuery = criteriaQuery.where(predicate);

        criteriaQuery = criteriaQuery.select(criteriaBuilder.count(root));

        return executeLongAggregateFunction(criteriaQuery);
    }

    @Override
    public List<CampaignTimePeriod> getAllForCampaign(Campaign campaign, FetchStrategy... fetchStrategy) {
        return getAllForCampaign(campaign, null, null, fetchStrategy);
    }

    @Override
    public List<CampaignTimePeriod> getAllForCampaign(Campaign campaign, Sorting sort, FetchStrategy... fetchStrategy) {
        return getAllForCampaign(campaign, null, sort, fetchStrategy);
    }

    @Override
    public List<CampaignTimePeriod> getAllForCampaign(Campaign campaign, Pagination page, FetchStrategy... fetchStrategy) {
        return getAllForCampaign(campaign, page, page.getSorting(), fetchStrategy);
    }

    protected List<CampaignTimePeriod> getAllForCampaign(Campaign campaign, Pagination page, Sorting sort, FetchStrategy... fetchStrategy) {
        CriteriaQueryContainer container = createCriteriaQuery(fetchStrategy);
        CriteriaQuery<CampaignTimePeriod> criteriaQuery = container.getQuery();
        Root<CampaignTimePeriod> root = container.getRoot();

        Predicate predicate = getAllForCampaignPredicate(root, campaign);
        criteriaQuery = criteriaQuery.where(predicate);

        criteriaQuery = criteriaQuery.select(root);

        criteriaQuery = processOrderBy(criteriaQuery, root, sort);
        return findAll(criteriaQuery, page);
     }


}
