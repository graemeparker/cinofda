package com.byyd.middleware.account.service;

import com.adfonic.domain.Advertiser;
import com.adfonic.domain.AdvertiserCloudInformation;
import com.byyd.middleware.account.exception.AdvertiserCloudManagerException;
import com.byyd.middleware.iface.service.BaseManager;

public interface AdvertiserCloudManager extends BaseManager {
    
    //------------------------------------------------------------------------------------------
    // API to manage Cloud credentials 
    //------------------------------------------------------------------------------------------

    String getFileMoverBucketName();
    
    AdvertiserCloudInformation getAdvertiserCloudInformation(Advertiser advertiser);
    AdvertiserCloudInformation getAdvertiserCloudInformation(Long advertiserId);
    
    AdvertiserCloudInformation createAdvertiserCloudInformation(Advertiser advertiser) throws AdvertiserCloudManagerException;  
    void deleteAdvertiserCloudInformation(Advertiser advertiser) throws AdvertiserCloudManagerException;
    
}
