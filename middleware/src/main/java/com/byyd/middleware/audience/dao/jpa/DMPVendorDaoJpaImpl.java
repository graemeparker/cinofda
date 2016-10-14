package com.byyd.middleware.audience.dao.jpa;

import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Repository;

import com.adfonic.domain.DMPVendor;
import com.adfonic.domain.DMPVendor_;
import com.byyd.middleware.audience.dao.DMPVendorDao;
import com.byyd.middleware.audience.filter.DMPVendorFilter;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.Pagination;
import com.byyd.middleware.iface.dao.Sorting;
import com.byyd.middleware.iface.dao.jpa.BusinessKeyDaoJpaImpl;

@Repository
public class DMPVendorDaoJpaImpl extends BusinessKeyDaoJpaImpl<DMPVendor> implements DMPVendorDao {

    protected Predicate getPredicate(Root<DMPVendor> root, DMPVendorFilter filter) {
        CriteriaBuilder criteriaBuilder = getTransactionalEntityManager().getCriteriaBuilder();

        Predicate namePredicate = null;
        Predicate restrictedPredicate = null;
        Predicate adminOnlyPredicate = null;
        
        if(!StringUtils.isEmpty(filter.getName())) {
            if(filter.getNameLikeSpec() == null) {
                if (filter.isNameCaseSensitive()) {
                    namePredicate = criteriaBuilder.equal(
                            root.get(DMPVendor_.name), filter.getName());
                } else {
                    namePredicate = criteriaBuilder.equal(
                            criteriaBuilder.lower(root.get(DMPVendor_.name)),
                            filter.getName().toLowerCase());
                }
            } else {
                if (filter.isNameCaseSensitive()) {
                    namePredicate = criteriaBuilder.like(root.get(DMPVendor_.name), filter.getNameLikeSpec().getPattern(filter.getName()));
                } else {
                    namePredicate = criteriaBuilder.like(criteriaBuilder.lower(root.get(DMPVendor_.name)), filter.getNameLikeSpec().getPattern(filter.getName()).toLowerCase());
                }
            }
        }

        if(filter.getRestricted() != null) {
            if(filter.getRestricted()) {
                restrictedPredicate = criteriaBuilder.isTrue(root.get(DMPVendor_.restricted));
            } else {
                restrictedPredicate = criteriaBuilder.isFalse(root.get(DMPVendor_.restricted));
            }
        }
        
        if(filter.getAdminOnly() != null) {
            if(filter.getAdminOnly()) {
            	adminOnlyPredicate = criteriaBuilder.isTrue(root.get(DMPVendor_.adminOnly));
            }
        }
        
        return or(and(namePredicate, restrictedPredicate), adminOnlyPredicate);
    }
    
    @Override
    public Long countAll(DMPVendorFilter filter) {
        CriteriaBuilder criteriaBuilder = getTransactionalEntityManager().getCriteriaBuilder();
        CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
        Root<DMPVendor> root = criteriaQuery.from(DMPVendor.class);

        Predicate predicate = getPredicate(root, filter);
        criteriaQuery = criteriaQuery.where(predicate);

        criteriaQuery = criteriaQuery.select(criteriaBuilder.count(root));

        return executeLongAggregateFunction(criteriaQuery);
    }

    @Override
    public List<DMPVendor> getAll(DMPVendorFilter filter, FetchStrategy ... fetchStrategy) {
        return getAll(filter, null, null, fetchStrategy);
    }

    @Override
    public List<DMPVendor> getAll(DMPVendorFilter filter, Pagination page, FetchStrategy ... fetchStrategy) {
        return getAll(filter, page, page.getSorting(), fetchStrategy);
    }

    @Override
    public List<DMPVendor> getAll(DMPVendorFilter filter, Sorting sort, FetchStrategy ... fetchStrategy) {
        return getAll(filter, null, sort, fetchStrategy);
    }

    protected List<DMPVendor> getAll(DMPVendorFilter filter, Pagination page, Sorting sort, FetchStrategy ... fetchStrategy) {
        CriteriaQueryContainer container = createCriteriaQuery(fetchStrategy);
        CriteriaQuery<DMPVendor> criteriaQuery = container.getQuery();
        Root<DMPVendor> root = container.getRoot();

        Predicate predicate = getPredicate(root, filter);
        if(predicate != null) {
            criteriaQuery = criteriaQuery.where(predicate);
        }
        criteriaQuery.distinct(true);
        criteriaQuery = criteriaQuery.select(root);

        criteriaQuery = processOrderBy(criteriaQuery, root, sort);
        return findAll(criteriaQuery, page);
    }


}
