package com.byyd.middleware.campaign.dao.jpa;

import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.stereotype.Repository;

import com.adfonic.domain.CampaignDataFee;
import com.adfonic.domain.CampaignDataFee_;
import com.byyd.middleware.campaign.dao.CampaignDataFeeDao;
import com.byyd.middleware.campaign.filter.CampaignDataFeeFilter;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.Pagination;
import com.byyd.middleware.iface.dao.Sorting;
import com.byyd.middleware.iface.dao.jpa.BusinessKeyDaoJpaImpl;

@Repository
public class CampaignDataFeeDaoJpaImpl extends BusinessKeyDaoJpaImpl<CampaignDataFee> implements CampaignDataFeeDao {
    
    protected Predicate getPredicate(Root<CampaignDataFee> root, CampaignDataFeeFilter filter) {
        CriteriaBuilder criteriaBuilder = getTransactionalEntityManager().getCriteriaBuilder();

        Predicate campaignPredictate = null;
        Predicate timePredictate = null;
        
        if(filter.getCampaign() != null) {
            campaignPredictate = criteriaBuilder.equal(root.get(CampaignDataFee_.campaign), filter.getCampaign());
        }
        
        if(filter.getTime() != null){
            timePredictate = and(
                                    criteriaBuilder.lessThan(root.get(CampaignDataFee_.startDate), filter.getTime()),
                                    or(
                                        criteriaBuilder.isNull(root.get(CampaignDataFee_.endDate)),
                                        criteriaBuilder.greaterThan(root.get(CampaignDataFee_.endDate), filter.getTime())
                                      )
                                );
        }
        
        return and(campaignPredictate, timePredictate);
    }

    @Override
    public Long countAll(CampaignDataFeeFilter filter) {
        CriteriaBuilder criteriaBuilder = getTransactionalEntityManager().getCriteriaBuilder();
        CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
        Root<CampaignDataFee> root = criteriaQuery.from(CampaignDataFee.class);

        Predicate predicate = getPredicate(root, filter);
        criteriaQuery = criteriaQuery.where(predicate);
        
        criteriaQuery = criteriaQuery.select(criteriaBuilder.count(root));

        return executeLongAggregateFunction(criteriaQuery);
    }

    @Override
    public List<CampaignDataFee> getAll(CampaignDataFeeFilter filter, FetchStrategy ... fetchStrategy) {
        return getAllForCampaign(filter, null, null, fetchStrategy);
    }

    @Override
    public List<CampaignDataFee> getAll(CampaignDataFeeFilter filter, Pagination page, FetchStrategy ... fetchStrategy) {
        return getAllForCampaign(filter, page, page.getSorting(), fetchStrategy);
    }

    @Override
    public List<CampaignDataFee> getAll(CampaignDataFeeFilter filter, Sorting sort, FetchStrategy ... fetchStrategy) {
        return getAllForCampaign(filter, null, sort, fetchStrategy);
    }

    protected List<CampaignDataFee> getAllForCampaign(CampaignDataFeeFilter filter, Pagination page, Sorting sort, FetchStrategy ... fetchStrategy) {
        CriteriaQueryContainer container = createCriteriaQuery(fetchStrategy);
        CriteriaQuery<CampaignDataFee> criteriaQuery = container.getQuery();
        Root<CampaignDataFee> root = container.getRoot();

        Predicate predicate = getPredicate(root, filter);
        criteriaQuery = criteriaQuery.where(predicate);

        criteriaQuery = criteriaQuery.select(root);

        criteriaQuery = processOrderBy(criteriaQuery, root, sort);
        return findAll(criteriaQuery, page);
    }

}
