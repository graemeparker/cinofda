package com.byyd.middleware.campaign.dao;

import java.util.List;

import com.adfonic.domain.GeotargetType;
import com.byyd.middleware.iface.dao.BusinessKeyDao;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.Pagination;
import com.byyd.middleware.iface.dao.Sorting;

public interface GeotargetTypeDao extends BusinessKeyDao<GeotargetType> {    
    
    GeotargetType getByNameAndType(String name, String type);
    
    Long countAllForCountryIsoCode(String isoCode, Boolean isRadiusType);
    List<GeotargetType> getAllForCountryIsoCode(String isoCode, Boolean isRadiusType, FetchStrategy... fetchStrategy);
    List<GeotargetType> getAllForCountryIsoCode(String isoCode, Boolean isRadiusType, Sorting sort, FetchStrategy... fetchStrategy);
    List<GeotargetType> getAllForCountryIsoCode(String isoCode, Boolean isRadiusType, Pagination page, FetchStrategy... fetchStrategy);
}
