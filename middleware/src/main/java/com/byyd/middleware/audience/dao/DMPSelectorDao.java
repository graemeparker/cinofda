package com.byyd.middleware.audience.dao;

import java.util.List;

import com.adfonic.domain.DMPAudience;
import com.adfonic.domain.DMPSelector;
import com.byyd.middleware.audience.filter.DMPSelectorFilter;
import com.byyd.middleware.iface.dao.BusinessKeyDao;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.Pagination;
import com.byyd.middleware.iface.dao.Sorting;

public interface DMPSelectorDao extends BusinessKeyDao<DMPSelector> {

    Long countAll(DMPSelectorFilter filter);
    List<DMPSelector> getAll(DMPSelectorFilter filter, FetchStrategy ... fetchStrategy);
    List<DMPSelector> getAll(DMPSelectorFilter filter, Pagination page, FetchStrategy ... fetchStrategy);
    List<DMPSelector> getAll(DMPSelectorFilter filter, Sorting sort, FetchStrategy ... fetchStrategy);
    
    Long countDMPSelectorsForDMPAudience(DMPAudience dmpAudience);
    List<DMPSelector> getDMPSelectorsForDMPAudience(DMPAudience dmpAudience, FetchStrategy... fetchStrategy);
    List<DMPSelector> getDMPSelectorsForDMPAudience(DMPAudience dmpAudience, Sorting sort, FetchStrategy... fetchStrategy);
    List<DMPSelector> getDMPSelectorsForDMPAudience(DMPAudience dmpAudience, Pagination page, FetchStrategy... fetchStrategy);
    
    DMPSelector getByExternalIdAndDmpVendorId(String externalId, Long dmpVendorId, FetchStrategy ... fetchStrategy);

}
