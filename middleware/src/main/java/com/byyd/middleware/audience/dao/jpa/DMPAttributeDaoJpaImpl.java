package com.byyd.middleware.audience.dao.jpa;

import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Repository;

import com.adfonic.domain.DMPAttribute;
import com.adfonic.domain.DMPAttribute_;
import com.byyd.middleware.audience.dao.DMPAttributeDao;
import com.byyd.middleware.audience.filter.DMPAttributeFilter;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.Pagination;
import com.byyd.middleware.iface.dao.Sorting;
import com.byyd.middleware.iface.dao.jpa.BusinessKeyDaoJpaImpl;

@Repository
public class DMPAttributeDaoJpaImpl extends BusinessKeyDaoJpaImpl<DMPAttribute> implements DMPAttributeDao {

    protected Predicate getPredicate(Root<DMPAttribute> root, DMPAttributeFilter filter) {
        CriteriaBuilder criteriaBuilder = getTransactionalEntityManager().getCriteriaBuilder();

        Predicate namePredicate = null;
        Predicate dmpVendorPredicate = null;
        
        if(!StringUtils.isEmpty(filter.getName())) {
            if(filter.getNameLikeSpec() == null) {
                if (filter.isNameCaseSensitive()) {
                    namePredicate = criteriaBuilder.equal(
                            root.get(DMPAttribute_.name), filter.getName());
                } else {
                    namePredicate = criteriaBuilder.equal(
                            criteriaBuilder.lower(root.get(DMPAttribute_.name)),
                            filter.getName().toLowerCase());
                }
            } else {
                if (filter.isNameCaseSensitive()) {
                    namePredicate = criteriaBuilder.like(root.get(DMPAttribute_.name), filter.getNameLikeSpec().getPattern(filter.getName()));
                } else {
                    namePredicate = criteriaBuilder.like(criteriaBuilder.lower(root.get(DMPAttribute_.name)), filter.getNameLikeSpec().getPattern(filter.getName()).toLowerCase());
                }
            }
        }

        
        if(filter.getDMPVendor() != null) {
            dmpVendorPredicate = criteriaBuilder.equal(root.get(DMPAttribute_.dmpVendor), filter.getDMPVendor());
        }
        
        return and(dmpVendorPredicate, namePredicate);
    }
    
    @Override
    public Long countAll(DMPAttributeFilter filter) {
        CriteriaBuilder criteriaBuilder = getTransactionalEntityManager().getCriteriaBuilder();
        CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
        Root<DMPAttribute> root = criteriaQuery.from(DMPAttribute.class);

        Predicate predicate = getPredicate(root, filter);
        criteriaQuery = criteriaQuery.where(predicate);

        criteriaQuery = criteriaQuery.select(criteriaBuilder.count(root));

        return executeLongAggregateFunction(criteriaQuery);
    }

    @Override
    public List<DMPAttribute> getAll(DMPAttributeFilter filter, FetchStrategy ... fetchStrategy) {
        return getAll(filter, null, null, fetchStrategy);
    }

    @Override
    public List<DMPAttribute> getAll(DMPAttributeFilter filter, Pagination page, FetchStrategy ... fetchStrategy) {
        return getAll(filter, page, page.getSorting(), fetchStrategy);
    }

    @Override
    public List<DMPAttribute> getAll(DMPAttributeFilter filter, Sorting sort, FetchStrategy ... fetchStrategy) {
        return getAll(filter, null, sort, fetchStrategy);
    }

    protected List<DMPAttribute> getAll(DMPAttributeFilter filter, Pagination page, Sorting sort, FetchStrategy ... fetchStrategy) {
        CriteriaQueryContainer container = createCriteriaQuery(fetchStrategy);
        CriteriaQuery<DMPAttribute> criteriaQuery = container.getQuery();
        Root<DMPAttribute> root = container.getRoot();

        Predicate predicate = getPredicate(root, filter);
        if(predicate != null) {
            criteriaQuery = criteriaQuery.where(predicate);
        }

        criteriaQuery = criteriaQuery.select(root);

        criteriaQuery = processOrderBy(criteriaQuery, root, sort);
        return findAll(criteriaQuery, page);
    }

}
