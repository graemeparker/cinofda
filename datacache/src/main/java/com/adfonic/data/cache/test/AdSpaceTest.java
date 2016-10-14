package com.adfonic.data.cache.test;

import org.springframework.beans.factory.annotation.Autowired;

import com.adfonic.data.cache.AdserverDataCache;
import com.adfonic.data.cache.AdserverDataCacheManager;
import com.adfonic.domain.cache.AdserverDomainCacheManager;
import com.adfonic.domain.cache.dto.adserver.adspace.AdSpaceDto;
import com.adfonic.domain.cache.ext.AdserverDomainCache;

public class AdSpaceTest { 

    @Autowired
    private AdserverDomainCacheManager adserverDomainCacheManager;
    
    @Autowired
    private AdserverDataCacheManager adserverDataCacheManager;
	
	public String adSpaceTest(){
    	StringBuilder testResult = new StringBuilder();
		
		AdserverDomainCache adserverDomainCache = adserverDomainCacheManager.getCache();
    	AdserverDataCache adserverDataCache = adserverDataCacheManager.getCache();
    	
		AdSpaceDto[] adSpaceDtos1 =  adserverDomainCache.getAllAdSpaces();
		AdSpaceDto[] adSpaceDtos2 =  adserverDataCache.getAllAdSpaces();
		
		if(adSpaceDtos1.length == adSpaceDtos2.length) testResult.append("ADSPACES WORKING");
		else testResult.append("There is a problem: adserverDomainCache = " + adSpaceDtos1.length + " adserverDataCache = " + adSpaceDtos2.length);

		
		return testResult.toString();
		
	}
	
}
