package com.byyd.middleware.campaign.dao;

import java.util.List;

import com.adfonic.domain.Geotarget;
import com.adfonic.domain.GeotargetType;
import com.byyd.middleware.campaign.filter.GeotargetFilter;
import com.byyd.middleware.iface.dao.BusinessKeyDao;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.LikeSpec;
import com.byyd.middleware.iface.dao.Pagination;
import com.byyd.middleware.iface.dao.Sorting;

public interface GeotargetDao extends BusinessKeyDao<Geotarget> {

    Long countGeotargetsByNameAndTypeAndIsoCode(String isoCode, GeotargetType type, String name, boolean caseSensitive, LikeSpec like);
    List<Geotarget> getGeotargetsByNameAndTypeAndIsoCode(String isoCode, GeotargetType type, String name, boolean caseSensitive, LikeSpec like, FetchStrategy... fetchStrategy);
    List<Geotarget> getGeotargetsByNameAndTypeAndIsoCode(String isoCode, GeotargetType type, String name, boolean caseSensitive, LikeSpec like, Sorting sort, FetchStrategy... fetchStrategy);
    List<Geotarget> getGeotargetsByNameAndTypeAndIsoCode(String isoCode, GeotargetType type, String name, boolean caseSensitive, LikeSpec like, Pagination page, FetchStrategy... fetchStrategy);

    Long countAll(GeotargetFilter filter);
    List<Geotarget> getAll(GeotargetFilter filter, Sorting sort, FetchStrategy ... fetchStrategy);
    List<Geotarget> getAll(GeotargetFilter filter, Pagination page, FetchStrategy ... fetchStrategy);

    Long countGeotargetTypesForCountryIsoCode (String isoCode);
    List<GeotargetType> getGeotargetTypesForCountryIsoCode (String isoCode);
    List<GeotargetType> getGeotargetTypesForCountryIsoCode (String isoCode, Sorting sort);
    List<GeotargetType> getGeotargetTypesForCountryIsoCode (String isoCode, Pagination page);


}
