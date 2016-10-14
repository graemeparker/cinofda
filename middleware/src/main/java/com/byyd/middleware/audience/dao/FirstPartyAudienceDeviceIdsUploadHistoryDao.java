package com.byyd.middleware.audience.dao;

import java.util.List;

import com.adfonic.domain.FirstPartyAudienceDeviceIdsUploadHistory;
import com.byyd.middleware.audience.filter.FirstPartyAudienceDeviceIdsUploadHistoryFilter;
import com.byyd.middleware.iface.dao.BusinessKeyDao;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.Pagination;
import com.byyd.middleware.iface.dao.Sorting;

public interface FirstPartyAudienceDeviceIdsUploadHistoryDao extends BusinessKeyDao<FirstPartyAudienceDeviceIdsUploadHistory> {

    Long countAll(FirstPartyAudienceDeviceIdsUploadHistoryFilter filter);
    List<FirstPartyAudienceDeviceIdsUploadHistory> getAll(FirstPartyAudienceDeviceIdsUploadHistoryFilter filter, FetchStrategy ... fetchStrategy);
    List<FirstPartyAudienceDeviceIdsUploadHistory> getAll(FirstPartyAudienceDeviceIdsUploadHistoryFilter filter, Pagination page, FetchStrategy ... fetchStrategy);
    List<FirstPartyAudienceDeviceIdsUploadHistory> getAll(FirstPartyAudienceDeviceIdsUploadHistoryFilter filter, Sorting sort, FetchStrategy ... fetchStrategy);
    
}
