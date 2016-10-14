package com.byyd.middleware.audience.dao;

import java.util.List;

import com.adfonic.domain.AudienceDataFee;
import com.byyd.middleware.audience.filter.AudienceDataFeeFilter;
import com.byyd.middleware.iface.dao.BusinessKeyDao;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.Pagination;
import com.byyd.middleware.iface.dao.Sorting;

public interface AudienceDataFeeDao extends BusinessKeyDao<AudienceDataFee> {

    Long countAll(AudienceDataFeeFilter filter);
    List< AudienceDataFee> getAll(AudienceDataFeeFilter filter, FetchStrategy ... fetchStrategy);
    List< AudienceDataFee> getAll(AudienceDataFeeFilter filter, Pagination page, FetchStrategy ... fetchStrategy);
    List< AudienceDataFee> getAll(AudienceDataFeeFilter filter, Sorting sort, FetchStrategy ... fetchStrategy);
}
