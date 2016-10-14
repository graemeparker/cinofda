package com.byyd.middleware.campaign.dao.jpa;

import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.stereotype.Repository;

import com.adfonic.domain.Country_;
import com.adfonic.domain.GeotargetType;
import com.adfonic.domain.GeotargetType_;
import com.byyd.middleware.campaign.dao.GeotargetTypeDao;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.Pagination;
import com.byyd.middleware.iface.dao.Sorting;
import com.byyd.middleware.iface.dao.jpa.BusinessKeyDaoJpaImpl;

@Repository
public class GeotargetTypeDaoJpaImpl extends BusinessKeyDaoJpaImpl<GeotargetType> implements GeotargetTypeDao {

    @Override
    public GeotargetType getByNameAndType(String name, String type) {
        CriteriaQueryContainer container = createCriteriaQuery();
        CriteriaQuery<GeotargetType> criteriaQuery = container.getQuery();
        Root<GeotargetType> root = container.getRoot();
        
        CriteriaBuilder criteriaBuilder = getTransactionalEntityManager().getCriteriaBuilder();
        Predicate namePredicate = criteriaBuilder.equal(root.get(GeotargetType_.name), name);
        Predicate typePredicate = criteriaBuilder.equal(root.get(GeotargetType_.type), type);
        
        criteriaQuery = criteriaQuery.where(and(namePredicate, typePredicate));
        criteriaQuery = criteriaQuery.select(root);

        return this.find(criteriaQuery);
    }
    
    //-------------------------------------------------------------------------------------------------------------------
    
    protected Predicate getCountryIsoCodePredicate(Root<GeotargetType> root, String isoCode, Boolean isRadiusType) {
        CriteriaBuilder criteriaBuilder = getTransactionalEntityManager().getCriteriaBuilder();
        Predicate isoCodepredicate = criteriaBuilder.equal(root.join(GeotargetType_.countries).get(Country_.isoCode), isoCode);
        Predicate isRadiusTypePredicate = null;
        if(isRadiusType != null) {
            if(isRadiusType) {
                isRadiusTypePredicate = criteriaBuilder.equal(root.get(GeotargetType_.type), GeotargetType.RADIUS_TYPE);
            } else {
                isRadiusTypePredicate = criteriaBuilder.notEqual(root.get(GeotargetType_.type), GeotargetType.RADIUS_TYPE);
            }
        }
        return and(isoCodepredicate, isRadiusTypePredicate);
    }
    
    @Override
    public Long countAllForCountryIsoCode(String isoCode, Boolean isRadiusType) {
        CriteriaBuilder criteriaBuilder = getTransactionalEntityManager().getCriteriaBuilder();
        CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
        Root<GeotargetType> root = criteriaQuery.from(GeotargetType.class);

        criteriaQuery = criteriaQuery.where(getCountryIsoCodePredicate(root, isoCode, isRadiusType));

        criteriaQuery = criteriaQuery.select(criteriaBuilder.count(root));

        return executeLongAggregateFunction(criteriaQuery);
    }
    
    @Override
    public List<GeotargetType> getAllForCountryIsoCode(String isoCode, Boolean isRadiusType, FetchStrategy... fetchStrategy) {
        return this.getAllForCountryIsoCode(isoCode, isRadiusType, null, null, fetchStrategy);
    }
    
    @Override
    public List<GeotargetType> getAllForCountryIsoCode(String isoCode, Boolean isRadiusType, Sorting sort, FetchStrategy... fetchStrategy) {
        return this.getAllForCountryIsoCode(isoCode, isRadiusType, null, sort, fetchStrategy);
    }
    
    @Override
    public List<GeotargetType> getAllForCountryIsoCode(String isoCode, Boolean isRadiusType, Pagination page, FetchStrategy... fetchStrategy) {
        return this.getAllForCountryIsoCode(isoCode, isRadiusType, page, page.getSorting(), fetchStrategy);
    }
    
    protected List<GeotargetType> getAllForCountryIsoCode(String isoCode, Boolean isRadiusType, Pagination page, Sorting sort, FetchStrategy... fetchStrategy) {
        CriteriaQueryContainer container = createCriteriaQuery(fetchStrategy);
        CriteriaQuery<GeotargetType> criteriaQuery = container.getQuery();
        Root<GeotargetType> root = container.getRoot();
        
        criteriaQuery = criteriaQuery.where(getCountryIsoCodePredicate(root, isoCode, isRadiusType));

        criteriaQuery = criteriaQuery.select(root);

        criteriaQuery = processOrderBy(criteriaQuery, root, sort);
        return findAll(criteriaQuery, page);
    }
}
