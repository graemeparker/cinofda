package com.byyd.middleware.campaign.dao.jpa;

import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.stereotype.Repository;

import com.adfonic.domain.Campaign;
import com.adfonic.domain.CampaignRichMediaAdServingFee;
import com.adfonic.domain.CampaignRichMediaAdServingFee_;
import com.byyd.middleware.campaign.dao.CampaignRichMediaAdServingFeeDao;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.Pagination;
import com.byyd.middleware.iface.dao.Sorting;
import com.byyd.middleware.iface.dao.jpa.BusinessKeyDaoJpaImpl;

@Repository
public class CampaignRichMediaAdServingFeeDaoJpaImpl extends BusinessKeyDaoJpaImpl<CampaignRichMediaAdServingFee> implements CampaignRichMediaAdServingFeeDao {

    @Override
    public Long countAllForCampaign(Campaign campaign) {
        CriteriaBuilder criteriaBuilder = getTransactionalEntityManager().getCriteriaBuilder();
        CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
        Root<CampaignRichMediaAdServingFee> root = criteriaQuery.from(CampaignRichMediaAdServingFee.class);

        Predicate predicate = criteriaBuilder.equal(root.get(CampaignRichMediaAdServingFee_.campaign), campaign);
        criteriaQuery = criteriaQuery.where(predicate);

        criteriaQuery = criteriaQuery.select(criteriaBuilder.count(root));

        return executeLongAggregateFunction(criteriaQuery);
    }

    @Override
    public List<CampaignRichMediaAdServingFee> getAllForCampaign(Campaign campaign, FetchStrategy ... fetchStrategy) {
        return getAllForCampaign(campaign, null, null, fetchStrategy);
    }

    @Override
    public List<CampaignRichMediaAdServingFee> getAllForCampaign(Campaign campaign, Pagination page, FetchStrategy ... fetchStrategy) {
        return getAllForCampaign(campaign, page, page.getSorting(), fetchStrategy);
    }

    @Override
    public List<CampaignRichMediaAdServingFee> getAllForCampaign(Campaign campaign, Sorting sort, FetchStrategy ... fetchStrategy) {
        return getAllForCampaign(campaign, null, sort, fetchStrategy);
    }

    protected List<CampaignRichMediaAdServingFee> getAllForCampaign(Campaign campaign, Pagination page, Sorting sort, FetchStrategy ... fetchStrategy) {
        CriteriaBuilder criteriaBuilder = getTransactionalEntityManager().getCriteriaBuilder();
        CriteriaQueryContainer container = createCriteriaQuery(fetchStrategy);
        CriteriaQuery<CampaignRichMediaAdServingFee> criteriaQuery = container.getQuery();
        Root<CampaignRichMediaAdServingFee> root = container.getRoot();

        Predicate predicate = criteriaBuilder.equal(root.get(CampaignRichMediaAdServingFee_.campaign), campaign);
        criteriaQuery = criteriaQuery.where(predicate);

        criteriaQuery = criteriaQuery.select(root);

        criteriaQuery = processOrderBy(criteriaQuery, root, sort);
        return findAll(criteriaQuery, page);
    }

}
