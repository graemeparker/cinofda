package com.byyd.middleware.creative.dao.jpa;

import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.stereotype.Repository;

import com.adfonic.domain.Creative;
import com.adfonic.domain.ExtendedCreativeTemplate;
import com.adfonic.domain.ExtendedCreativeTemplate_;
import com.byyd.middleware.creative.dao.ExtendedCreativeTemplateDao;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.Pagination;
import com.byyd.middleware.iface.dao.Sorting;
import com.byyd.middleware.iface.dao.jpa.BusinessKeyDaoJpaImpl;

@Repository
public class ExtendedCreativeTemplateDaoJpaImpl extends BusinessKeyDaoJpaImpl<ExtendedCreativeTemplate> implements ExtendedCreativeTemplateDao {

    @Override
    public Long countAllForCreative(Creative creative) {
        CriteriaBuilder criteriaBuilder = getTransactionalEntityManager().getCriteriaBuilder();
        CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
        Root<ExtendedCreativeTemplate> root = criteriaQuery.from(ExtendedCreativeTemplate.class);
        
        Predicate creativeExpression = criteriaBuilder.equal(root.get(ExtendedCreativeTemplate_.creative), creative);
        criteriaQuery = criteriaQuery.where(creativeExpression);
        
        criteriaQuery = criteriaQuery.select(criteriaBuilder.count(root));
        
        return executeLongAggregateFunction(criteriaQuery);
    }
    
    @Override
    public List<ExtendedCreativeTemplate> getAllForCreative(Creative creative, FetchStrategy... fetchStrategy) {
        return this.getAllForCreative(creative, null, null, fetchStrategy);
    }
    
    @Override
    public List<ExtendedCreativeTemplate> getAllForCreative(Creative creative, Sorting sort, FetchStrategy... fetchStrategy) {
        return this.getAllForCreative(creative, null, sort, fetchStrategy);
    }
    
    @Override
    public List<ExtendedCreativeTemplate> getAllForCreative(Creative creative, Pagination page, FetchStrategy... fetchStrategy) {
        return this.getAllForCreative(creative, page, page.getSorting(), fetchStrategy);
    }

    protected List<ExtendedCreativeTemplate> getAllForCreative(Creative creative, Pagination page, Sorting sort, FetchStrategy... fetchStrategy) {
        CriteriaQueryContainer container = createCriteriaQuery(fetchStrategy);
        CriteriaQuery<ExtendedCreativeTemplate> criteriaQuery = container.getQuery();
        Root<ExtendedCreativeTemplate> root = container.getRoot();
        CriteriaBuilder criteriaBuilder = getTransactionalEntityManager().getCriteriaBuilder();

        Predicate creativeExpression = criteriaBuilder.equal(root.get(ExtendedCreativeTemplate_.creative), creative);
        criteriaQuery = criteriaQuery.where(creativeExpression);

        criteriaQuery = criteriaQuery.select(root);

        criteriaQuery = processOrderBy(criteriaBuilder, criteriaQuery, root, sort);
        return findAll(criteriaQuery, page);
    }
}
