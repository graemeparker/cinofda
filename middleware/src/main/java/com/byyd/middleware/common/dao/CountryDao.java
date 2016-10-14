package com.byyd.middleware.common.dao;

import java.util.List;

import com.adfonic.domain.Country;
import com.byyd.middleware.iface.dao.BusinessKeyDao;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.LikeSpec;
import com.byyd.middleware.iface.dao.Pagination;
import com.byyd.middleware.iface.dao.Sorting;

public interface CountryDao extends BusinessKeyDao<Country> {

    Country getByIsoCode(String isoCode, FetchStrategy... fetchStrategy);

    Long countCountriesByName(String name, LikeSpec like, boolean caseSensitive, Boolean hidden);
    List<Country> getCountriesByName(String name, LikeSpec like, boolean caseSensitive, Boolean hidden, FetchStrategy... fetchStrategy);
    List<Country> getCountriesByName(String name, LikeSpec like, boolean caseSensitive, Boolean hidden, Sorting sort, FetchStrategy... fetchStrategy);
    List<Country> getCountriesByName(String name, LikeSpec like, boolean caseSensitive, Boolean hidden, Pagination page, FetchStrategy... fetchStrategy);
    
    Long countAllCountries(boolean includeHidden);
    List<Country> getAllCountries(boolean includeHidden, FetchStrategy... fetchStrategy);
    List<Country> getAllCountries(boolean includeHidden, Sorting sort, FetchStrategy... fetchStrategy);
    List<Country> getAllCountries(boolean includeHidden, Pagination page, FetchStrategy... fetchStrategy);

}
