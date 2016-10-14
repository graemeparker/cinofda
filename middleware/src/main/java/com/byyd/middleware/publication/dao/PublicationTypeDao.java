package com.byyd.middleware.publication.dao;

import java.util.List;

import com.adfonic.domain.PublicationType;
import com.byyd.middleware.iface.dao.BusinessKeyDao;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.Pagination;
import com.byyd.middleware.iface.dao.Sorting;

public interface PublicationTypeDao extends BusinessKeyDao<PublicationType> {

    @Override
    PublicationType getBySystemName(String systemName, FetchStrategy ... fetchStrategy);
    
    Long countForSystemNames(List<String> systemNames);
    List<PublicationType> getForSystemNames(List<String> systemNames, FetchStrategy... fetchStrategy);
    List<PublicationType> getForSystemNames(List<String> systemNames, Sorting sort, FetchStrategy... fetchStrategy);
    List<PublicationType> getForSystemNames(List<String> systemNames, Pagination page, FetchStrategy... fetchStrategy);

}
