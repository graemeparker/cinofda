package com.byyd.middleware.integrations.dao.jpa;

import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.stereotype.Repository;

import com.adfonic.domain.CampaignTrigger;
import com.adfonic.domain.CampaignTrigger.PluginType;
import com.adfonic.domain.CampaignTrigger_;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.Pagination;
import com.byyd.middleware.iface.dao.Sorting;
import com.byyd.middleware.iface.dao.jpa.BusinessKeyDaoJpaImpl;
import com.byyd.middleware.integrations.dao.CampaignTriggerDao;
import com.byyd.middleware.integrations.filter.CampaignTriggerFilter;

@Repository
public class CampaignTriggerDaoJpaImpl extends BusinessKeyDaoJpaImpl<CampaignTrigger> implements CampaignTriggerDao {
    
    protected Predicate getPredicate(Root<CampaignTrigger> root, CampaignTriggerFilter filter) {
        CriteriaBuilder criteriaBuilder = getTransactionalEntityManager().getCriteriaBuilder();

        Predicate campaignPredictate = null;
        Predicate pluginVendorPredictate = null;
        Predicate plugintTypesPredictate = null;
        Predicate deletedPredictate = null;
        
        if(filter.getCampaign() != null) {
            campaignPredictate = criteriaBuilder.equal(root.get(CampaignTrigger_.campaign), filter.getCampaign());
        }
        
        if(filter.getPluginVendor() != null) {
            pluginVendorPredictate = criteriaBuilder.equal(root.get(CampaignTrigger_.pluginVendor), filter.getCampaign());
        }
        
        if (filter.getPluginTypes() != null){
            for (PluginType pluginType : filter.getPluginTypes()){
                if (plugintTypesPredictate==null){
                    plugintTypesPredictate = criteriaBuilder.equal(root.get(CampaignTrigger_.pluginType), pluginType);
                }else{
                    plugintTypesPredictate = or(plugintTypesPredictate, criteriaBuilder.equal(root.get(CampaignTrigger_.pluginType), pluginType));
                }
            }
             
        }
        
        if(filter.getDeleted() != null) {
            if(filter.getDeleted()) {
                deletedPredictate = criteriaBuilder.isTrue(root.get(CampaignTrigger_.deleted));
            } else {
                deletedPredictate = criteriaBuilder.isFalse(root.get(CampaignTrigger_.deleted));
            }
        }
        
        return and(campaignPredictate, pluginVendorPredictate, plugintTypesPredictate, deletedPredictate);
    }
    
    @Override
    public Long countAll(CampaignTriggerFilter filter) {
        CriteriaBuilder criteriaBuilder = getTransactionalEntityManager().getCriteriaBuilder();
        CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
        Root<CampaignTrigger> root = criteriaQuery.from(CampaignTrigger.class);

        Predicate predicate = getPredicate(root, filter);
        criteriaQuery = criteriaQuery.where(predicate);

        criteriaQuery = criteriaQuery.select(criteriaBuilder.count(root));

        return executeLongAggregateFunction(criteriaQuery);
    }
    
    @Override
    public List<CampaignTrigger> getAll(CampaignTriggerFilter filter, FetchStrategy ... fetchStrategy) {
        return getAll(filter, null, null, fetchStrategy);
    }

    @Override
    public List<CampaignTrigger> getAll(CampaignTriggerFilter filter, Pagination page, FetchStrategy ... fetchStrategy) {
        return getAll(filter, page, page.getSorting(), fetchStrategy);
    }

    @Override
    public List<CampaignTrigger> getAll(CampaignTriggerFilter filter, Sorting sort, FetchStrategy ... fetchStrategy) {
        return getAll(filter, null, sort, fetchStrategy);
    }

    protected List<CampaignTrigger> getAll(CampaignTriggerFilter filter, Pagination page, Sorting sort, FetchStrategy ... fetchStrategy) {
        CriteriaQueryContainer container = createCriteriaQuery(fetchStrategy);
        CriteriaQuery<CampaignTrigger> criteriaQuery = container.getQuery();
        Root<CampaignTrigger> root = container.getRoot();

        Predicate predicate = getPredicate(root, filter);
        if(predicate != null) {
            criteriaQuery = criteriaQuery.where(predicate);
        }

        criteriaQuery = criteriaQuery.select(root);

        criteriaQuery = processOrderBy(criteriaQuery, root, sort);
        return findAll(criteriaQuery, page);
    }
}
