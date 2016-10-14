package com.byyd.middleware.device.dao.jpa;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.stereotype.Repository;

import com.adfonic.domain.Country;
import com.adfonic.domain.Operator;
import com.adfonic.domain.OperatorAlias;
import com.adfonic.domain.OperatorAlias_;
import com.adfonic.domain.Operator_;
import com.byyd.middleware.device.dao.OperatorDao;
import com.byyd.middleware.device.filter.OperatorFilter;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.Pagination;
import com.byyd.middleware.iface.dao.Sorting;
import com.byyd.middleware.iface.dao.jpa.BusinessKeyDaoJpaImpl;
import com.byyd.middleware.iface.dao.jpa.QueryParameter;

@Repository
public class OperatorDaoJpaImpl extends BusinessKeyDaoJpaImpl<Operator> implements OperatorDao {

    protected StringBuilder getOperatorsQuery(OperatorFilter filter, boolean countOnly) {
        String nameConstraint = " and ( " + (filter.isCaseSensitive() ? "OPERATOR.NAME" : "lower(OPERATOR.NAME)") + ") like ?"; 
        String mobileOperatorConstraint = " and OPERATOR.MOBILE_OPERATOR = ";
        StringBuilder sb = new StringBuilder("select " + (countOnly ? "count(distinct OPERATOR.ID)" : "distinct OPERATOR.ID as ID") + " from OPERATOR")
        .append(" left outer join MOBILE_IP_ADDRESS_RANGE m on m.OPERATOR_ID=OPERATOR.ID")
        .append(" left outer join OPERATOR_ALIAS a on (a.OPERATOR_ID=OPERATOR.ID" + (filter.isMandateQuova() ? " and a.TYPE='QUOVA'" : "") + ")")
        .append(" where (m.ID is not null or a.ID is not null)");
        if(filter.getName()!=null){
            sb.append(nameConstraint);
        }
        if(filter.isMobileOperator()!=null){
            if(filter.isMobileOperator().equals(true)){
                sb.append(mobileOperatorConstraint + "'1'");
            }
            else{
                sb.append(mobileOperatorConstraint + "'0'");
            }
        }
        if (filter.getCountries() != null && !filter.getCountries().isEmpty()) {
            sb.append(" and (OPERATOR.COUNTRY_ID IN (");
            int index = 0;
            for (Country c : filter.getCountries()) {
                sb.append(index > 0 ? ", " : "")
                .append(c.getId());
                index++;
            }
            sb.append("))");
        }
        return sb;
    }
    
    @Override
    public Long countOperators(OperatorFilter filter) {
        StringBuilder query = this.getOperatorsQuery(filter, true);
        List<QueryParameter> list = new ArrayList<QueryParameter>();
        if(filter.getName()!=null){
            list.add(new QueryParameter(filter.getLikeSpec().getPattern(filter.getName())));
        }
        Number count = this.executeAggregateFunctionByNativeQueryPositionalParameters(query.toString(), list);
        return count.longValue();
    }
    
    @Override
    public List<Operator> getOperators(OperatorFilter filter, FetchStrategy... fetchStrategy) {
        return this.getOperatorsForNameAndCountries(filter, null, null, fetchStrategy);
    }

    @Override
    public List<Operator> getOperators(OperatorFilter filter, Sorting sort, FetchStrategy... fetchStrategy) {
        return this.getOperatorsForNameAndCountries(filter, null, sort, fetchStrategy);
    }

    @Override
    public List<Operator> getOperators(OperatorFilter filter, Pagination page, FetchStrategy... fetchStrategy) {
        return this.getOperatorsForNameAndCountries(filter, page, page.getSorting(), fetchStrategy);
    }

    @SuppressWarnings("unchecked")
    protected List<Operator> getOperatorsForNameAndCountries(OperatorFilter filter, Pagination page, Sorting sort, FetchStrategy... fetchStrategy) {
        StringBuilder query = this.getOperatorsQuery(filter,false);
        List<QueryParameter> list = new ArrayList<QueryParameter>();
        if(filter.getName()!=null){
            list.add(new QueryParameter(filter.getLikeSpec().getPattern(filter.getName())));
        }
        if(sort != null) {
            query.append(" order by " + sort.toString().toUpperCase());
        }
        List<Number> ids = this.findByNativeQueryPositionalParameters(query.toString(), page, list);
        List<Operator> operators = new ArrayList<Operator>();
        if(ids != null && !ids.isEmpty()) {
            for(Number id : ids) {
                operators.add(this.getById(id.longValue(), fetchStrategy));
            }
        }
        return operators;
    }

    // ------------------------------------------------------------------------------------------
    
    @Override
    public Operator getOperatorForOperatorAliasAndCountry(OperatorAlias.Type operatorAliasType, Country country, String alias, FetchStrategy... fetchStrategy) {
        CriteriaQueryContainer container = createCriteriaQuery(fetchStrategy);
        CriteriaQuery<Operator> criteriaQuery = container.getQuery();
        Root<Operator> root = container.getRoot();

        CriteriaBuilder criteriaBuilder = getTransactionalEntityManager().getCriteriaBuilder();
        Join<Operator, OperatorAlias> operatorAliasJoin = root.join(Operator_.operatorAliases);

        Predicate typePredicate = criteriaBuilder.equal(operatorAliasJoin.get(OperatorAlias_.type), operatorAliasType);
        Predicate countryPredicate = criteriaBuilder.equal(root.get(Operator_.country), country);
        Predicate aliasPredicate = criteriaBuilder.equal(operatorAliasJoin.get(OperatorAlias_.alias), alias);
//        if(checkMobileOperator){
//        Predicate mobileOperatorPredicate = criteriaBuilder.equal(Operator_.mobileOperator, mobileOperator);
//        }
        
        Predicate predicate = and(typePredicate, countryPredicate, aliasPredicate);

        criteriaQuery = criteriaQuery.where(predicate);
        CriteriaQuery<Operator> select = criteriaQuery.select(root);

        return find(select);
    }

}
