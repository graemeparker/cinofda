package com.byyd.middleware.account.dao;

import java.util.Date;
import java.util.List;

import com.adfonic.domain.Advertiser;
import com.adfonic.domain.AdvertiserStoppage;
import com.byyd.middleware.iface.dao.BusinessKeyDao;
import com.byyd.middleware.iface.dao.FetchStrategy;

public interface AdvertiserStoppageDao extends BusinessKeyDao<AdvertiserStoppage> {

    List<Object[]> getFieldsForNullOrFutureReactivateDate();
    List<AdvertiserStoppage> getAllForReactivateDateIsNullOrReactivateDateGreaterThan(Date reactivateDate, FetchStrategy... fetchStrategy);
    List<AdvertiserStoppage> getAllForAdvertiserAndReactivateDateIsNullOrReactivateDateGreaterThan(Advertiser advertiser, Date reactivateDate, FetchStrategy... fetchStrategy);
}
