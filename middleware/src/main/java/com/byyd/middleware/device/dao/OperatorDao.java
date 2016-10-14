package com.byyd.middleware.device.dao;

import java.util.List;

import com.adfonic.domain.Country;
import com.adfonic.domain.Operator;
import com.adfonic.domain.OperatorAlias;
import com.byyd.middleware.device.filter.OperatorFilter;
import com.byyd.middleware.iface.dao.BusinessKeyDao;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.LikeSpec;
import com.byyd.middleware.iface.dao.Pagination;
import com.byyd.middleware.iface.dao.Sorting;

public interface OperatorDao extends BusinessKeyDao<Operator> {
    
    Long countOperators(OperatorFilter filter);
    List<Operator> getOperators(OperatorFilter filter, FetchStrategy... fetchStrategy);
    List<Operator> getOperators(OperatorFilter filter, Sorting sort, FetchStrategy... fetchStrategy);
    List<Operator> getOperators(OperatorFilter filter, Pagination page, FetchStrategy... fetchStrategy);


    Operator getOperatorForOperatorAliasAndCountry(OperatorAlias.Type operatorAliasType, Country country, String alias, FetchStrategy... fetchStrategy);
}
