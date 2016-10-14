package com.byyd.middleware.integrations.dao.jpa;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.stereotype.Repository;

import com.adfonic.domain.PluginVendor;
import com.adfonic.domain.PluginVendor_;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.jpa.BusinessKeyDaoJpaImpl;
import com.byyd.middleware.integrations.dao.PluginVendorDao;

@Repository
public class PluginVendorDaoJpaImpl extends BusinessKeyDaoJpaImpl<PluginVendor> implements PluginVendorDao {
    
    @Override
    public PluginVendor getByEmail(String emailAddress, FetchStrategy... fetchStrategy) {
        
        CriteriaQueryContainer container = createCriteriaQuery(fetchStrategy);
        CriteriaQuery<PluginVendor> criteriaQuery = container.getQuery();
        Root<PluginVendor> root = container.getRoot();
        CriteriaBuilder criteriaBuilder = getTransactionalEntityManager().getCriteriaBuilder();
        Predicate creativeExpression = criteriaBuilder.equal(root.get(PluginVendor_.apiUser), emailAddress);
        criteriaQuery = criteriaQuery.where(creativeExpression);
        criteriaQuery = criteriaQuery.select(root);
        return find(criteriaQuery);
        
    }
    
}
