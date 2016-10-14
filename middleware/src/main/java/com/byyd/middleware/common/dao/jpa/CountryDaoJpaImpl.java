package com.byyd.middleware.common.dao.jpa;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.stereotype.Repository;

import com.adfonic.domain.Country;
import com.adfonic.domain.Country_;
import com.byyd.middleware.common.dao.CountryDao;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.LikeSpec;
import com.byyd.middleware.iface.dao.Pagination;
import com.byyd.middleware.iface.dao.Sorting;
import com.byyd.middleware.iface.dao.jpa.BusinessKeyDaoJpaImpl;
import com.byyd.middleware.iface.dao.jpa.QueryParameter;

@Repository
public class CountryDaoJpaImpl extends BusinessKeyDaoJpaImpl<Country> implements CountryDao {

    @Override
    public Country getByIsoCode(String isoCode, FetchStrategy... fetchStrategy) {
        CriteriaQueryContainer container = createCriteriaQuery(fetchStrategy);
        CriteriaQuery<Country> criteriaQuery = container.getQuery();
        Root<Country> root = container.getRoot();
        CriteriaBuilder criteriaBuilder = getTransactionalEntityManager().getCriteriaBuilder();

        Predicate creativeExpression = criteriaBuilder.equal(root.get(Country_.isoCode), isoCode);
        criteriaQuery = criteriaQuery.where(creativeExpression);

        criteriaQuery = criteriaQuery.select(root);

        return find(criteriaQuery);
    }

    //------------------------------------------------------------------------------------------

    @Override
    public Long countCountriesByName(String name, LikeSpec like, boolean caseSensitive, Boolean hidden) {
        if(like.isRegex()) {
            return countCountriesByRegexName(name, like, caseSensitive, hidden);
        }
        if(hidden == null) {
            return this.countAllForName(name, like, caseSensitive);
        }
        CriteriaBuilder criteriaBuilder = getTransactionalEntityManager().getCriteriaBuilder();
        CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
        Root<Country> root = criteriaQuery.from(Country.class);

        Predicate namePredicate = null;
        if(caseSensitive) {
            namePredicate = criteriaBuilder.like(root.get(Country_.name), like.getPattern(name));
        } else {
            namePredicate = criteriaBuilder.like(criteriaBuilder.lower(root.get(Country_.name)), like.getPattern(name.toLowerCase()));
        }
        Predicate hiddenPredicate = null;
        if(hidden) {
            hiddenPredicate = criteriaBuilder.isTrue(root.get(Country_.hidden));
        } else {
            hiddenPredicate = criteriaBuilder.isFalse(root.get(Country_.hidden));
        }

        criteriaQuery = criteriaQuery.where(and(namePredicate, hiddenPredicate));

        criteriaQuery = criteriaQuery.select(criteriaBuilder.count(root));

        return executeLongAggregateFunction(criteriaQuery);
    }

    @Override
    public List<Country> getCountriesByName(String name, LikeSpec like, boolean caseSensitive, Boolean hidden, FetchStrategy... fetchStrategy) {
        return getCountriesByName(name, like, caseSensitive, hidden, null, null, fetchStrategy);
    }

    @Override
    public List<Country> getCountriesByName(String name, LikeSpec like, boolean caseSensitive, Boolean hidden, Sorting sort, FetchStrategy... fetchStrategy) {
        return getCountriesByName(name, like, caseSensitive, hidden, null, sort, fetchStrategy);
    }

    @Override
    public List<Country> getCountriesByName(String name, LikeSpec like, boolean caseSensitive, Boolean hidden, Pagination page, FetchStrategy... fetchStrategy) {
        return getCountriesByName(name, like, caseSensitive, hidden, page, page.getSorting(), fetchStrategy);
    }

    public List<Country> getCountriesByName(String name, LikeSpec like, boolean caseSensitive, Boolean hidden, Pagination page, Sorting sort, FetchStrategy... fetchStrategy) {
        if(like.isRegex()) {
            return getCountriesByRegexName(name, like, caseSensitive, hidden, page, sort, fetchStrategy);
        }
        if(hidden == null) {
            return this.getAllForName(name, like, caseSensitive, page, sort, fetchStrategy);
        }
        CriteriaQueryContainer container = createCriteriaQuery(fetchStrategy);
        CriteriaQuery<Country> criteriaQuery = container.getQuery();
        Root<Country> root = container.getRoot();
        CriteriaBuilder criteriaBuilder = getTransactionalEntityManager().getCriteriaBuilder();

        Predicate namePredicate = null;
        if(caseSensitive) {
            namePredicate = criteriaBuilder.like(root.get(Country_.name), like.getPattern(name));
        } else {
            namePredicate = criteriaBuilder.like(criteriaBuilder.lower(root.get(Country_.name)), like.getPattern(name.toLowerCase()));
        }
        if(caseSensitive) {
            namePredicate = or(namePredicate,criteriaBuilder.like(root.get(Country_.name), like.getPattern(name)));
        } else {
            namePredicate = or(namePredicate,criteriaBuilder.like(criteriaBuilder.lower(root.get(Country_.name)), like.getPattern(name.toLowerCase())));
        }
        Predicate hiddenPredicate = null;
        if(hidden) {
            hiddenPredicate = criteriaBuilder.isTrue(root.get(Country_.hidden));
        } else {
            hiddenPredicate = criteriaBuilder.isFalse(root.get(Country_.hidden));
        }

        criteriaQuery = criteriaQuery.where(and(namePredicate, hiddenPredicate));

        criteriaQuery = criteriaQuery.select(root);

        criteriaQuery = processOrderBy(criteriaBuilder, criteriaQuery, root, sort);
        return findAll(criteriaQuery, page);
    }

    protected String getRegexNameWhereStatement(boolean caseSensitive, Boolean hidden) {
        StringBuilder buffer = new StringBuilder();
        if(caseSensitive) {
            buffer.append("NAME RLIKE ?");
        } else {
            buffer.append("LOWER(NAME) RLIKE ?");
        }
        if(hidden != null) {
            if(hidden) {
                buffer.append(" AND HIDDEN = 1");
            } else {
                buffer.append(" AND HIDDEN = 0");
            }
        }
        return buffer.toString();
    }

    protected Long countCountriesByRegexName(String name, LikeSpec like, boolean caseSensitive, Boolean hidden) {
        String query = "SELECT COUNT(*) FROM COUNTRY WHERE " + getRegexNameWhereStatement(caseSensitive, hidden);
        List<QueryParameter> params = new ArrayList<QueryParameter>();
        if(caseSensitive) {
            params.add(new QueryParameter(like.getRegex(name)));
        } else {
            params.add(new QueryParameter(like.getRegex(name.toLowerCase())));
        }
        return executeAggregateFunctionByNativeQueryPositionalParameters(query, params).longValue();
    }

    @SuppressWarnings("unchecked")
    protected List<Country> getCountriesByRegexName(String name, LikeSpec like, boolean caseSensitive, Boolean hidden, Pagination page, Sorting sort, FetchStrategy... fetchStrategy) {
        StringBuilder query = new StringBuilder();
        query.append("SELECT DISTINCT(COUNTRY.ID) AS ID FROM COUNTRY WHERE " + getRegexNameWhereStatement(caseSensitive, hidden));
        List<QueryParameter> params = new ArrayList<QueryParameter>();
        if(caseSensitive) {
            params.add(new QueryParameter(like.getRegex(name)));
        } else {
            params.add(new QueryParameter(like.getRegex(name.toLowerCase())));
        }
        if(sort != null) {
            query.append(" order by " + sort.toString());
        }
        List<Number> ids = this.findByNativeQueryPositionalParameters(query.toString(), page, params);
        List<Country> countries = new ArrayList<Country>();
        if(ids != null && !ids.isEmpty()) {
            for(Number id : ids) {
                countries.add(this.getById(id.longValue(), fetchStrategy));
            }
        }
        return countries;

    }
    
    //------------------------------------------------------------------------------------------

    @Override
    public Long countAllCountries(boolean includeHidden) {
        CriteriaBuilder criteriaBuilder = getTransactionalEntityManager().getCriteriaBuilder();
        CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
        Root<Country> root = criteriaQuery.from(Country.class);

        Predicate hiddenPredicate = null;
        if(!includeHidden) {
            hiddenPredicate = criteriaBuilder.isFalse(root.get(Country_.hidden));
        }

        if(hiddenPredicate != null) {
            criteriaQuery = criteriaQuery.where(hiddenPredicate);
        }

        criteriaQuery = criteriaQuery.select(criteriaBuilder.count(root));

        return executeLongAggregateFunction(criteriaQuery);
    }
    
    @Override
    public List<Country> getAllCountries(boolean includeHidden, FetchStrategy... fetchStrategy) {
        return getAllCountries(includeHidden, null, null, fetchStrategy);
    }

    @Override
    public List<Country> getAllCountries(boolean includeHidden, Sorting sort, FetchStrategy... fetchStrategy) {
        return getAllCountries(includeHidden, null, sort, fetchStrategy);
    }

    @Override
    public List<Country> getAllCountries(boolean includeHidden, Pagination page, FetchStrategy... fetchStrategy) {
        return getAllCountries(includeHidden, page, page.getSorting(), fetchStrategy);
    }
    
    public List<Country> getAllCountries(boolean includeHidden, Pagination page, Sorting sort, FetchStrategy... fetchStrategy) {
        CriteriaQueryContainer container = createCriteriaQuery(fetchStrategy);
        CriteriaQuery<Country> criteriaQuery = container.getQuery();
        Root<Country> root = container.getRoot();
        CriteriaBuilder criteriaBuilder = getTransactionalEntityManager().getCriteriaBuilder();

        Predicate hiddenPredicate = null;
        if(!includeHidden) {
            hiddenPredicate = criteriaBuilder.isFalse(root.get(Country_.hidden));
        }

        if(hiddenPredicate != null) {
            criteriaQuery = criteriaQuery.where(hiddenPredicate);
        }

        criteriaQuery = criteriaQuery.select(root);

        criteriaQuery = processOrderBy(criteriaBuilder, criteriaQuery, root, sort);
        return findAll(criteriaQuery, page);
    }

}
