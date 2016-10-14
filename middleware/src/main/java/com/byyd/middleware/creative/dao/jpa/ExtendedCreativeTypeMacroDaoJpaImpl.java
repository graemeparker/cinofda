package com.byyd.middleware.creative.dao.jpa;

import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.stereotype.Repository;

import com.adfonic.domain.ExtendedCreativeType;
import com.adfonic.domain.ExtendedCreativeTypeMacro;
import com.adfonic.domain.ExtendedCreativeTypeMacro_;
import com.byyd.middleware.creative.dao.ExtendedCreativeTypeMacroDao;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.Pagination;
import com.byyd.middleware.iface.dao.Sorting;
import com.byyd.middleware.iface.dao.jpa.BusinessKeyDaoJpaImpl;

@Repository
public class ExtendedCreativeTypeMacroDaoJpaImpl extends BusinessKeyDaoJpaImpl<ExtendedCreativeTypeMacro> implements ExtendedCreativeTypeMacroDao {

    @Override
    public Long countAllForExtendedCreativeType(ExtendedCreativeType extendedCreativeType) {
        CriteriaBuilder criteriaBuilder = getTransactionalEntityManager().getCriteriaBuilder();
        CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
        Root<ExtendedCreativeTypeMacro> root = criteriaQuery.from(ExtendedCreativeTypeMacro.class);

        Predicate predicate = criteriaBuilder.equal(root.get(ExtendedCreativeTypeMacro_.extendedCreativeType), extendedCreativeType);
        criteriaQuery = criteriaQuery.where(predicate);

        criteriaQuery = criteriaQuery.select(criteriaBuilder.count(root));

        return executeLongAggregateFunction(criteriaQuery);
    }
    
    @Override
    public List<ExtendedCreativeTypeMacro> getAllForExtendedCreativeType(ExtendedCreativeType extendedCreativeType, FetchStrategy... fetchStrategy) {
        return this.getAllForExtendedCreativeType(extendedCreativeType, null, null, fetchStrategy);
    }

    @Override
    public List<ExtendedCreativeTypeMacro> getAllForExtendedCreativeType(ExtendedCreativeType extendedCreativeType, Sorting sort, FetchStrategy... fetchStrategy) {
           return this.getAllForExtendedCreativeType(extendedCreativeType, null, sort, fetchStrategy);
    }

    @Override
    public List<ExtendedCreativeTypeMacro> getAllForExtendedCreativeType(ExtendedCreativeType extendedCreativeType, Pagination page, FetchStrategy... fetchStrategy) {
        return this.getAllForExtendedCreativeType(extendedCreativeType, page, page.getSorting(), fetchStrategy);
    }

    protected List<ExtendedCreativeTypeMacro> getAllForExtendedCreativeType(ExtendedCreativeType extendedCreativeType, Pagination page, Sorting sort, FetchStrategy... fetchStrategy) {
        CriteriaQueryContainer container = createCriteriaQuery(fetchStrategy);
        CriteriaQuery<ExtendedCreativeTypeMacro> criteriaQuery = container.getQuery();
        Root<ExtendedCreativeTypeMacro> root = container.getRoot();
        CriteriaBuilder criteriaBuilder = getTransactionalEntityManager().getCriteriaBuilder();
        
        Predicate predicate = criteriaBuilder.equal(root.get(ExtendedCreativeTypeMacro_.extendedCreativeType), extendedCreativeType);
        criteriaQuery = criteriaQuery.where(predicate);

        criteriaQuery = criteriaQuery.select(root);

        criteriaQuery = processOrderBy(criteriaBuilder, criteriaQuery, root, sort);
        return findAll(criteriaQuery, page);

    }

}
