package com.byyd.middleware.audience.dao.jpa;

import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.stereotype.Repository;

import com.adfonic.domain.AudienceDataFee;
import com.adfonic.domain.AudienceDataFee_;
import com.byyd.middleware.audience.dao.AudienceDataFeeDao;
import com.byyd.middleware.audience.filter.AudienceDataFeeFilter;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.Pagination;
import com.byyd.middleware.iface.dao.Sorting;
import com.byyd.middleware.iface.dao.jpa.BusinessKeyDaoJpaImpl;

@Repository
public class AudienceDataFeeDaoJpaImpl extends BusinessKeyDaoJpaImpl<AudienceDataFee> implements AudienceDataFeeDao {

    protected Predicate getPredicate(Root<AudienceDataFee> root, AudienceDataFeeFilter filter) {
        CriteriaBuilder criteriaBuilder = getTransactionalEntityManager().getCriteriaBuilder();

        Predicate campaignPredictate = null;
        Predicate timePredictate = null;
        
        if(filter.getCampaignDataFee() != null) {
            campaignPredictate = criteriaBuilder.equal(root.get(AudienceDataFee_.campaignDataFee), filter.getCampaignDataFee());
        }
        
        if (filter.getTime() != null){
            timePredictate = and(
                                    criteriaBuilder.lessThan(root.get(AudienceDataFee_.startTime), filter.getTime()),
                                    or(
                                        criteriaBuilder.isNull(root.get(AudienceDataFee_.endTime)),
                                        criteriaBuilder.greaterThan(root.get(AudienceDataFee_.endTime), filter.getTime())
                                      )
                                );
        }
        
        return and(campaignPredictate, timePredictate);
    }
    
    @Override
    public Long countAll(AudienceDataFeeFilter filter) {
        CriteriaBuilder criteriaBuilder = getTransactionalEntityManager().getCriteriaBuilder();
        CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
        Root<AudienceDataFee> root = criteriaQuery.from(AudienceDataFee.class);

        Predicate predicate = getPredicate(root, filter);
        criteriaQuery = criteriaQuery.where(predicate);

        criteriaQuery = criteriaQuery.select(criteriaBuilder.count(root));

        return executeLongAggregateFunction(criteriaQuery);
    }

    @Override
    public List<AudienceDataFee> getAll(AudienceDataFeeFilter filter, FetchStrategy ... fetchStrategy) {
        return getAll(filter, null, null, fetchStrategy);
    }

    @Override
    public List<AudienceDataFee> getAll(AudienceDataFeeFilter filter, Pagination page, FetchStrategy ... fetchStrategy) {
        return getAll(filter, page, page.getSorting(), fetchStrategy);
    }

    @Override
    public List<AudienceDataFee> getAll(AudienceDataFeeFilter filter, Sorting sort, FetchStrategy ... fetchStrategy) {
        return getAll(filter, null, sort, fetchStrategy);
    }

    protected List<AudienceDataFee> getAll(AudienceDataFeeFilter filter, Pagination page, Sorting sort, FetchStrategy ... fetchStrategy) {
        CriteriaQueryContainer container = createCriteriaQuery(fetchStrategy);
        CriteriaQuery<AudienceDataFee> criteriaQuery = container.getQuery();
        Root<AudienceDataFee> root = container.getRoot();

        Predicate predicate = getPredicate(root, filter);
        if(predicate != null) {
            criteriaQuery = criteriaQuery.where(predicate);
        }

        criteriaQuery = criteriaQuery.select(root);

        criteriaQuery = processOrderBy(criteriaQuery, root, sort);
        return findAll(criteriaQuery, page);
    }
}
