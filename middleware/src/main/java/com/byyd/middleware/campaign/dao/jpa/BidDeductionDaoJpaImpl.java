package com.byyd.middleware.campaign.dao.jpa;

import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.stereotype.Repository;

import com.adfonic.domain.BidDeduction;
import com.adfonic.domain.BidDeduction_;
import com.byyd.middleware.campaign.dao.BidDeductionDao;
import com.byyd.middleware.campaign.filter.BidDeductionFilter;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.Pagination;
import com.byyd.middleware.iface.dao.Sorting;
import com.byyd.middleware.iface.dao.jpa.BusinessKeyDaoJpaImpl;

@Repository
public class BidDeductionDaoJpaImpl extends BusinessKeyDaoJpaImpl<BidDeduction> implements BidDeductionDao {

    @Override
    public List<BidDeduction> getAll(BidDeductionFilter filter, FetchStrategy ... fetchStrategy) {
        return getAll(filter, null, null, fetchStrategy);
    }

    @Override
    public List<BidDeduction> getAll(BidDeductionFilter filter, Pagination page, FetchStrategy ... fetchStrategy) {
        return getAll(filter, page, page.getSorting(), fetchStrategy);
    }

    @Override
    public List<BidDeduction> getAll(BidDeductionFilter filter, Sorting sort, FetchStrategy ... fetchStrategy) {
        return getAll(filter, null, sort, fetchStrategy);
    }

    protected List<BidDeduction> getAll(BidDeductionFilter filter, Pagination page, Sorting sort, FetchStrategy ... fetchStrategy) {
        CriteriaQueryContainer container = createCriteriaQuery(fetchStrategy);
        CriteriaQuery<BidDeduction> criteriaQuery = container.getQuery();
        Root<BidDeduction> root = container.getRoot();

        Predicate predicate = getPredicate(root, filter);
        if(predicate != null) {
            criteriaQuery = criteriaQuery.where(predicate);
        }

        criteriaQuery = criteriaQuery.select(root);

        criteriaQuery = processOrderBy(criteriaQuery, root, sort);
        return findAll(criteriaQuery, page);
    }
    
    protected Predicate getPredicate(Root<BidDeduction> root, BidDeductionFilter filter) {
        CriteriaBuilder criteriaBuilder = getTransactionalEntityManager().getCriteriaBuilder();

        Predicate campaignPredictate = null;
        
        if(filter.getCampaign() != null) {
            campaignPredictate = criteriaBuilder.equal(root.get(BidDeduction_.campaign), filter.getCampaign());
        }
        
        return and(campaignPredictate);
    }
	
}
