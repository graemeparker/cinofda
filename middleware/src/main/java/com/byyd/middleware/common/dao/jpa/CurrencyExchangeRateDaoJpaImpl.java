package com.byyd.middleware.common.dao.jpa;

import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.stereotype.Repository;

import com.adfonic.domain.CurrencyExchangeRate;
import com.adfonic.domain.CurrencyExchangeRate_;
import com.byyd.middleware.common.dao.CurrencyExchangeRateDao;
import com.byyd.middleware.common.filter.CurrencyExchangeRatesFilter;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.Pagination;
import com.byyd.middleware.iface.dao.Sorting;
import com.byyd.middleware.iface.dao.jpa.BusinessKeyDaoJpaImpl;

@Repository
public class CurrencyExchangeRateDaoJpaImpl extends BusinessKeyDaoJpaImpl<CurrencyExchangeRate> implements CurrencyExchangeRateDao {

    
    @Override
    public Long countCurrencyExchangeRates(CurrencyExchangeRatesFilter filter){
        CriteriaBuilder criteriaBuilder = getTransactionalEntityManager().getCriteriaBuilder();
        CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
        Root<CurrencyExchangeRate> root = criteriaQuery.from(CurrencyExchangeRate.class);

        Predicate predicate = getPredicate(filter, root, criteriaBuilder);
        criteriaQuery = criteriaQuery.where(predicate);

        criteriaQuery = criteriaQuery.select(criteriaBuilder.count(root));

        return executeLongAggregateFunction(criteriaQuery);
    }
    
    @Override
    public List<CurrencyExchangeRate> getCurrencyExchangeRates(CurrencyExchangeRatesFilter filter, FetchStrategy ... fetchStrategy){
        return getCurrencyExchangeRates(filter, null, null, fetchStrategy);
    }
    
    @Override
    public List<CurrencyExchangeRate> getCurrencyExchangeRates(CurrencyExchangeRatesFilter filter, Sorting sort, FetchStrategy ... fetchStrategy){
        return getCurrencyExchangeRates(filter, null, sort, fetchStrategy);
    }
    
    @Override
    public List<CurrencyExchangeRate> getCurrencyExchangeRates(CurrencyExchangeRatesFilter filter, Pagination page, FetchStrategy ... fetchStrategy){
        return getCurrencyExchangeRates(filter, page, page.getSorting(), fetchStrategy);
    }
    
    protected List<CurrencyExchangeRate> getCurrencyExchangeRates(CurrencyExchangeRatesFilter filter, Pagination page, Sorting sort, FetchStrategy ... fetchStrategy) {
        CriteriaQueryContainer container = createCriteriaQuery(fetchStrategy);
        CriteriaQuery<CurrencyExchangeRate> criteriaQuery = container.getQuery();
        Root<CurrencyExchangeRate> root = container.getRoot();

        CriteriaBuilder criteriaBuilder = getTransactionalEntityManager().getCriteriaBuilder();
        Predicate predicate = getPredicate(filter, root, criteriaBuilder);
        
        criteriaQuery = criteriaQuery.where(predicate);

        criteriaQuery = criteriaQuery.select(root);

        criteriaQuery = processOrderBy(criteriaQuery, root, sort);
        return findAll(criteriaQuery, page);
    }

    private Predicate getPredicate(CurrencyExchangeRatesFilter filter, Root<CurrencyExchangeRate> root, CriteriaBuilder criteriaBuilder) {
        Predicate fromCurrencyCodePredicate = null;
        Predicate toCurrencyCodePredicate = null;
        Predicate isDefaultPredicate = null;
        
        if (filter.getFromCurrencyCode()!=null){
            fromCurrencyCodePredicate = criteriaBuilder.equal(root.get(CurrencyExchangeRate_.fromCurrencyCode), filter.getFromCurrencyCode());
        }
        
        if (filter.getToCurrencyCode()!=null){
            toCurrencyCodePredicate = criteriaBuilder.equal(root.get(CurrencyExchangeRate_.toCurrencyCode), filter.getToCurrencyCode());
        }
        
        if (filter.isDefaultConversion()!=null){
            isDefaultPredicate = criteriaBuilder.equal(root.get(CurrencyExchangeRate_.defaultConversion), filter.isDefaultConversion());
        }
        
        return and(fromCurrencyCodePredicate, toCurrencyCodePredicate, isDefaultPredicate);
    }

}
