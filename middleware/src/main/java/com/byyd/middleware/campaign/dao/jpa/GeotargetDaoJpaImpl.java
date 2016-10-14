package com.byyd.middleware.campaign.dao.jpa;

import java.util.List;

import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.stereotype.Repository;

import com.adfonic.domain.Country;
import com.adfonic.domain.Country_;
import com.adfonic.domain.Geotarget;
import com.adfonic.domain.GeotargetType;
import com.adfonic.domain.Geotarget_;
import com.byyd.middleware.campaign.dao.GeotargetDao;
import com.byyd.middleware.campaign.filter.GeotargetFilter;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.LikeSpec;
import com.byyd.middleware.iface.dao.Pagination;
import com.byyd.middleware.iface.dao.Sorting;
import com.byyd.middleware.iface.dao.jpa.BusinessKeyDaoJpaImpl;

@Repository
public class GeotargetDaoJpaImpl extends BusinessKeyDaoJpaImpl<Geotarget> implements GeotargetDao {

    protected Predicate getGeotargetsByNameAndTypeAndIsoCodePredicate(Root<Geotarget> root, String isoCode, GeotargetType geotargetType, String name, boolean caseSensitive, LikeSpec like) {
        CriteriaBuilder criteriaBuilder = getTransactionalEntityManager().getCriteriaBuilder();
        Join<Geotarget, Country> countryJoin = root.join(Geotarget_.country, JoinType.INNER);

        Predicate namePredicate = null;
        if(caseSensitive) {
            namePredicate = criteriaBuilder.like(root.get(Geotarget_.name), like.getPattern(name));
        } else {
            namePredicate = criteriaBuilder.like(criteriaBuilder.lower(root.get(Geotarget_.name)), like.getPattern(name).toLowerCase());
        }
        Predicate typePredicate = criteriaBuilder.equal(root.get(Geotarget_.geotargetType), geotargetType);
        Predicate isoCodePredicate = criteriaBuilder.equal(countryJoin.get(Country_.isoCode), isoCode);

        return and(isoCodePredicate, typePredicate, namePredicate);
    }

    @Override
    public Long countGeotargetsByNameAndTypeAndIsoCode(String isoCode, GeotargetType geotargetType, String name, boolean caseSensitive, LikeSpec like) {
        CriteriaBuilder criteriaBuilder = getTransactionalEntityManager().getCriteriaBuilder();
        CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
        Root<Geotarget> root = criteriaQuery.from(Geotarget.class);

        Predicate predicate = this.getGeotargetsByNameAndTypeAndIsoCodePredicate(root, isoCode, geotargetType, name, caseSensitive, like);
        criteriaQuery = criteriaQuery.where(predicate);

        criteriaQuery = criteriaQuery.select(criteriaBuilder.count(root));

        return executeLongAggregateFunction(criteriaQuery);
    }

    @Override
    public List<Geotarget> getGeotargetsByNameAndTypeAndIsoCode(String isoCode, GeotargetType geotargetType, String name, boolean caseSensitive, LikeSpec like, FetchStrategy... fetchStrategy) {
        return this.getGeotargetsByNameAndTypeAndIsoCode(isoCode, geotargetType, name, caseSensitive, like, null, null, fetchStrategy);
    }

    @Override
    public List<Geotarget> getGeotargetsByNameAndTypeAndIsoCode(String isoCode, GeotargetType geotargetType, String name, boolean caseSensitive, LikeSpec like, Sorting sort, FetchStrategy... fetchStrategy) {
        return this.getGeotargetsByNameAndTypeAndIsoCode(isoCode, geotargetType, name, caseSensitive, like, null, sort, fetchStrategy);
    }

    @Override
    public List<Geotarget> getGeotargetsByNameAndTypeAndIsoCode(String isoCode, GeotargetType geotargetType, String name, boolean caseSensitive, LikeSpec like, Pagination page, FetchStrategy... fetchStrategy) {
        return this.getGeotargetsByNameAndTypeAndIsoCode(isoCode, geotargetType, name, caseSensitive, like, page, page.getSorting(), fetchStrategy);
    }

    protected List<Geotarget> getGeotargetsByNameAndTypeAndIsoCode(String isoCode, GeotargetType geotargetType, String name, boolean caseSensitive, LikeSpec like, Pagination page, Sorting sort, FetchStrategy... fetchStrategy) {
        CriteriaQueryContainer container = createCriteriaQuery(fetchStrategy);
        CriteriaQuery<Geotarget> criteriaQuery = container.getQuery();
        Root<Geotarget> root = container.getRoot();

        Predicate predicate = this.getGeotargetsByNameAndTypeAndIsoCodePredicate(root, isoCode, geotargetType, name, caseSensitive, like);
        criteriaQuery = criteriaQuery.where(predicate);

        criteriaQuery = criteriaQuery.select(root);

        criteriaQuery = processOrderBy(criteriaQuery, root, sort);
        return findAll(criteriaQuery, page);
    }

   // Select distinct Country.* from Country Inner Join Geotarget where Geotarget.Country_ID = Country.ID;
    @SuppressWarnings("rawtypes")
    public List getCountriesWhichAreGeotargetable(){
        Query query = getTransactionalEntityManager().createQuery("Select distinct c from Country as c Inner Join Geotarget as g where g.Country_ID = c.ID");
        return query.getResultList();
    }
    
    
    
    // ------------------------------------------------------------------------------------------

    @Override
    public Long countGeotargetTypesForCountryIsoCode(String isoCode) {
        CriteriaBuilder criteriaBuilder = getTransactionalEntityManager().getCriteriaBuilder();
        CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
        Root<Geotarget> root = criteriaQuery.from(Geotarget.class);
        Join<Geotarget, Country> countryJoin = root.join(Geotarget_.country, JoinType.INNER);

        Predicate predicate = criteriaBuilder.equal(countryJoin.get(Country_.isoCode), isoCode);
        criteriaQuery = criteriaQuery.where(predicate);

        criteriaQuery = criteriaQuery.select(criteriaBuilder.countDistinct(root.get(Geotarget_.geotargetType)));

        return executeLongAggregateFunction(criteriaQuery);
    }

    @Override
    public List<GeotargetType> getGeotargetTypesForCountryIsoCode(String isoCode) {
        return this.getGeotargetTypesForCountryIsoCode(isoCode, null, null);
    }

    @Override
    public List<GeotargetType> getGeotargetTypesForCountryIsoCode(String isoCode, Sorting sort) {
        return this.getGeotargetTypesForCountryIsoCode(isoCode, null, sort);
    }

    @Override
    public List<GeotargetType> getGeotargetTypesForCountryIsoCode(String isoCode, Pagination page) {
        return this.getGeotargetTypesForCountryIsoCode(isoCode, page, page.getSorting());
    }

    @SuppressWarnings("unchecked")
    protected List<GeotargetType> getGeotargetTypesForCountryIsoCode(String isoCode, Pagination page, Sorting sort) {
        CriteriaBuilder criteriaBuilder = getTransactionalEntityManager().getCriteriaBuilder();
        CriteriaQuery<GeotargetType> criteriaQuery = criteriaBuilder.createQuery(GeotargetType.class);
        Root<Geotarget> root = criteriaQuery.from(Geotarget.class);
        Join<Geotarget, Country> countryJoin = root.join(Geotarget_.country, JoinType.INNER);

        Predicate predicate = criteriaBuilder.equal(countryJoin.get(Country_.isoCode), isoCode);
         criteriaQuery = criteriaQuery.where(predicate);

        criteriaQuery = criteriaQuery.select(root.get(Geotarget_.geotargetType)).distinct(true);

        criteriaQuery = processOrderByForObjects(criteriaQuery, root, sort);
        return findAllObjects(criteriaQuery, page);

    }

    @Override
    public Long countAll(GeotargetFilter filter) {
        CriteriaBuilder criteriaBuilder = getTransactionalEntityManager().getCriteriaBuilder();
        CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
        Root<Geotarget> root = criteriaQuery.from(Geotarget.class);

        Predicate predicate = getPredicate(root, filter);
        criteriaQuery = criteriaQuery.where(predicate);

        criteriaQuery = criteriaQuery.select(criteriaBuilder.count(root));

        return executeLongAggregateFunction(criteriaQuery);
    }
    
    @Override
    public List<Geotarget> getAll(GeotargetFilter filter, Sorting sort, FetchStrategy ... fetchStrategy) {
        return getAll(filter, null, sort, fetchStrategy);
    }
    
    @Override
    public List<Geotarget> getAll(GeotargetFilter filter, Pagination page, FetchStrategy ... fetchStrategy) {
        return getAll(filter, page, null, fetchStrategy);
    }
    
    protected List<Geotarget> getAll(GeotargetFilter filter, Pagination page, Sorting sort, FetchStrategy ... fetchStrategy) {
        CriteriaQueryContainer container = createCriteriaQuery(fetchStrategy);
        CriteriaQuery<Geotarget> criteriaQuery = container.getQuery();
        Root<Geotarget> root = container.getRoot();

        Predicate predicate = getPredicate(root, filter);
        criteriaQuery = criteriaQuery.where(predicate);

        criteriaQuery = criteriaQuery.select(root);

        criteriaQuery = processOrderBy(criteriaQuery, root, sort);
        return findAll(criteriaQuery, page);
    }

    protected Predicate getPredicate(Root<Geotarget> root, GeotargetFilter filter) {
        CriteriaBuilder criteriaBuilder = getTransactionalEntityManager().getCriteriaBuilder();

        Predicate typePredicate = null;
        Predicate countryPredicate = null;
        Predicate countryIsoCodePredicate = null;
        Predicate namesPredicate = null;
        Predicate nameLikePredicate = null;

        if (filter.getType() != null) {
            typePredicate = criteriaBuilder.equal(root.get(Geotarget_.geotargetType), filter.getType());
        }

        if (filter.getCountry() != null) {
            countryPredicate = criteriaBuilder.equal(root.get(Geotarget_.country), filter.getCountry());
        }

        if (filter.getCountryIsoCode() != null) {
            Join<Geotarget, Country> countryJoin = root.join(Geotarget_.country, JoinType.INNER);
            countryIsoCodePredicate = criteriaBuilder.equal(countryJoin.get(Country_.isoCode), filter.getCountryIsoCode());
        }
        
        if (filter.getNames() != null && !filter.getNames().isEmpty()) {
            for (String name : filter.getNames()) {
                Predicate p;
                if (filter.isNamesCaseSensitive()) {
                    p = criteriaBuilder.equal(root.get(Geotarget_.name), name);
                } else {
                    p = criteriaBuilder.equal(criteriaBuilder.lower(root.get(Geotarget_.name)), name.toLowerCase());
                }
                if (namesPredicate == null) {
                    namesPredicate = p;
                } else {
                    namesPredicate = or(namesPredicate, p);
                }
            }
        }

        if (filter.getNameLike() != null) {
            if (filter.isNameLikeCaseSensitive()) {
                nameLikePredicate = criteriaBuilder.like(root.get(Geotarget_.name), filter.getNameLike());
            } else {
                nameLikePredicate = criteriaBuilder.like(criteriaBuilder.lower(root.get(Geotarget_.name)), filter.getNameLike().toLowerCase());
            }
        }

        return and(typePredicate, countryPredicate, countryIsoCodePredicate, namesPredicate, nameLikePredicate);
    }

}
