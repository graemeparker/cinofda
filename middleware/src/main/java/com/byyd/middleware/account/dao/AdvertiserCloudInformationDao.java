package com.byyd.middleware.account.dao;

import com.adfonic.domain.Advertiser;
import com.adfonic.domain.AdvertiserCloudInformation;
import com.byyd.middleware.iface.dao.BusinessKeyDao;
import com.byyd.middleware.iface.dao.FetchStrategy;

public interface AdvertiserCloudInformationDao extends BusinessKeyDao<AdvertiserCloudInformation> {
    
    AdvertiserCloudInformation getByAdvertiser(Advertiser advertiser, FetchStrategy... fetchStrategy);
}
