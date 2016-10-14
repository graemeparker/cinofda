package com.byyd.middleware.campaign.dao.jpa;

import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.stereotype.Repository;

import com.adfonic.domain.Campaign;
import com.adfonic.domain.CampaignTradingDeskMargin;
import com.adfonic.domain.CampaignTradingDeskMargin_;
import com.byyd.middleware.campaign.dao.CampaignTradingDeskMarginDao;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.Pagination;
import com.byyd.middleware.iface.dao.Sorting;
import com.byyd.middleware.iface.dao.jpa.BusinessKeyDaoJpaImpl;

@Repository
public class CampaignTradingDeskMarginDaoJpaImpl extends BusinessKeyDaoJpaImpl<CampaignTradingDeskMargin> implements CampaignTradingDeskMarginDao {

    @Override
    public Long countAllForCampaign(Campaign campaign) {
        CriteriaBuilder criteriaBuilder = getTransactionalEntityManager().getCriteriaBuilder();
        CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
        Root<CampaignTradingDeskMargin> root = criteriaQuery.from(CampaignTradingDeskMargin.class);

        Predicate predicate = criteriaBuilder.equal(root.get(CampaignTradingDeskMargin_.campaign), campaign);
        criteriaQuery = criteriaQuery.where(predicate);

        criteriaQuery = criteriaQuery.select(criteriaBuilder.count(root));

        return executeLongAggregateFunction(criteriaQuery);
    }

    @Override
    public List<CampaignTradingDeskMargin> getAllForCampaign(Campaign campaign, FetchStrategy ... fetchStrategy) {
        return getAllForCampaign(campaign, null, null, fetchStrategy);
    }

    @Override
    public List<CampaignTradingDeskMargin> getAllForCampaign(Campaign campaign, Pagination page, FetchStrategy ... fetchStrategy) {
        return getAllForCampaign(campaign, page, page.getSorting(), fetchStrategy);
    }

    @Override
    public List<CampaignTradingDeskMargin> getAllForCampaign(Campaign campaign, Sorting sort, FetchStrategy ... fetchStrategy) {
        return getAllForCampaign(campaign, null, sort, fetchStrategy);
    }

    protected List<CampaignTradingDeskMargin> getAllForCampaign(Campaign campaign, Pagination page, Sorting sort, FetchStrategy ... fetchStrategy) {
        CriteriaBuilder criteriaBuilder = getTransactionalEntityManager().getCriteriaBuilder();
        CriteriaQueryContainer container = createCriteriaQuery(fetchStrategy);
        CriteriaQuery<CampaignTradingDeskMargin> criteriaQuery = container.getQuery();
        Root<CampaignTradingDeskMargin> root = container.getRoot();

        Predicate predicate = criteriaBuilder.equal(root.get(CampaignTradingDeskMargin_.campaign), campaign);
        criteriaQuery = criteriaQuery.where(predicate);

        criteriaQuery = criteriaQuery.select(root);

        criteriaQuery = processOrderBy(criteriaQuery, root, sort);
        return findAll(criteriaQuery, page);
    }

}
