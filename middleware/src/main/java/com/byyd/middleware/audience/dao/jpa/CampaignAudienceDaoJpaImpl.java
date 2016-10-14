package com.byyd.middleware.audience.dao.jpa;

import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.stereotype.Repository;

import com.adfonic.domain.CampaignAudience;
import com.adfonic.domain.CampaignAudience_;
import com.byyd.middleware.audience.dao.CampaignAudienceDao;
import com.byyd.middleware.audience.filter.CampaignAudienceFilter;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.Pagination;
import com.byyd.middleware.iface.dao.Sorting;
import com.byyd.middleware.iface.dao.jpa.BusinessKeyDaoJpaImpl;

@Repository
public class CampaignAudienceDaoJpaImpl extends BusinessKeyDaoJpaImpl<CampaignAudience> implements CampaignAudienceDao {

    protected Predicate getPredicate(Root<CampaignAudience> root, CampaignAudienceFilter filter) {
        CriteriaBuilder criteriaBuilder = getTransactionalEntityManager().getCriteriaBuilder();

        Predicate audiencePredicate = null;
        Predicate includePredictate = null;
        Predicate deletedPredictate = null;
        Predicate campaignPredictate = null;
        
        if(filter.getAudience() != null) {
            audiencePredicate = criteriaBuilder.equal(root.get(CampaignAudience_.audience), filter.getAudience());
        }

        if(filter.getInclude() != null) {
            if(filter.getInclude()) {
                includePredictate = criteriaBuilder.isTrue(root.get(CampaignAudience_.include));
            } else {
                includePredictate = criteriaBuilder.isFalse(root.get(CampaignAudience_.include));
            }
        }
        
        if(filter.getDeleted() != null) {
            if(filter.getDeleted()) {
                deletedPredictate = criteriaBuilder.isTrue(root.get(CampaignAudience_.deleted));
            } else {
                deletedPredictate = criteriaBuilder.isFalse(root.get(CampaignAudience_.deleted));
            }
        }
        
        if(filter.getCampaign() != null) {
            campaignPredictate = criteriaBuilder.equal(root.get(CampaignAudience_.campaign), filter.getCampaign());
        }
        
        return and(audiencePredicate, includePredictate, deletedPredictate, campaignPredictate);
    }
    
    @Override
    public Long countAll(CampaignAudienceFilter filter) {
        CriteriaBuilder criteriaBuilder = getTransactionalEntityManager().getCriteriaBuilder();
        CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
        Root<CampaignAudience> root = criteriaQuery.from(CampaignAudience.class);

        Predicate predicate = getPredicate(root, filter);
        criteriaQuery = criteriaQuery.where(predicate);

        criteriaQuery = criteriaQuery.select(criteriaBuilder.count(root));

        return executeLongAggregateFunction(criteriaQuery);
    }

    @Override
    public List<CampaignAudience> getAll(CampaignAudienceFilter filter, FetchStrategy ... fetchStrategy) {
        return getAll(filter, null, null, fetchStrategy);
    }

    @Override
    public List<CampaignAudience> getAll(CampaignAudienceFilter filter, Pagination page, FetchStrategy ... fetchStrategy) {
        return getAll(filter, page, page.getSorting(), fetchStrategy);
    }

    @Override
    public List<CampaignAudience> getAll(CampaignAudienceFilter filter, Sorting sort, FetchStrategy ... fetchStrategy) {
        return getAll(filter, null, sort, fetchStrategy);
    }

    protected List<CampaignAudience> getAll(CampaignAudienceFilter filter, Pagination page, Sorting sort, FetchStrategy ... fetchStrategy) {
        CriteriaQueryContainer container = createCriteriaQuery(fetchStrategy);
        CriteriaQuery<CampaignAudience> criteriaQuery = container.getQuery();
        Root<CampaignAudience> root = container.getRoot();

        Predicate predicate = getPredicate(root, filter);
        if(predicate != null) {
            criteriaQuery = criteriaQuery.where(predicate);
        }

        criteriaQuery = criteriaQuery.select(root);

        criteriaQuery = processOrderBy(criteriaQuery, root, sort);
        return findAll(criteriaQuery, page);
    }
}
