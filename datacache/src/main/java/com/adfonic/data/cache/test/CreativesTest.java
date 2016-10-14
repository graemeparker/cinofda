package com.adfonic.data.cache.test;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import com.adfonic.data.cache.AdserverDataCache;
import com.adfonic.data.cache.AdserverDataCacheManager;
import com.adfonic.domain.cache.AdserverDomainCacheManager;
import com.adfonic.domain.cache.dto.adserver.creative.CreativeDto;
import com.adfonic.domain.cache.ext.AdserverDomainCache;

public class CreativesTest {

    @Autowired
    private AdserverDomainCacheManager adserverDomainCacheManager;
    
    @Autowired
    private AdserverDataCacheManager adserverDataCacheManager;
	
	public String creativesTest(){
    	StringBuilder testResult = new StringBuilder();
		
		try{
		AdserverDomainCache adserverDomainCache = adserverDomainCacheManager.getCache();
    	AdserverDataCache adserverDataCache = adserverDataCacheManager.getCache();
    	
		CreativeDto[] creativeDtos1 =  adserverDomainCache.getAllCreatives();
		CreativeDto[] creativeDtos2 =  adserverDataCache.getAllCreatives();
		
		Map<Long,CreativeDto> hashedCreatives2 = new HashMap<>();
		for (CreativeDto creativeDto : creativeDtos2){
			hashedCreatives2.put(creativeDto.getId(), creativeDto);
		}
		
		if (hashedCreatives2.size() == creativeDtos1.length){
			int count = 0;
			for (CreativeDto creativeDto : creativeDtos1){
				if(!compareCreatives(creativeDto, hashedCreatives2.get(creativeDto.getId()))){
					testResult.append("Mismatch : " + creativeDto.getId() + " - " + hashedCreatives2.get(creativeDto.getId()).getId());
				}else{
					count++;
				}
			}
			if (count == creativeDtos1.length){
				testResult.append("ALL THE CREATIVES MATCH \n");
				// TODO: test services
				boolean getCreativeByExternalID = true;
				boolean getCreativeById = true;
				boolean getPluginCreativeInfo = true;
				boolean getRecentlyStoppedCreativeById = true;
				boolean getPluginCreatives = true;
				
				for (CreativeDto creativeDto : creativeDtos1){
					if (getCreativeByExternalID && !compareCreatives(adserverDomainCache.getCreativeByExternalID(creativeDto.getExternalID()), adserverDataCache.getCreativeByExternalID(creativeDto.getExternalID()))){
						testResult.append(" ***** ERROR ***** getCreativeByExternalID INS'T WORKING! \n");
						testResult.append("getCreativeByExternalID result for ExternalID : " + creativeDto.getExternalID());
						testResult.append("Domain cache result: " + adserverDomainCache.getCreativeByExternalID(creativeDto.getExternalID()) + " ");
						testResult.append("Data   cache result: " + adserverDataCache.getCreativeByExternalID(creativeDto.getExternalID()) + "\n");
						getCreativeByExternalID = false;
					}
					if (getCreativeById && !compareCreatives(adserverDomainCache.getCreativeById(creativeDto.getId()), adserverDataCache.getCreativeById(creativeDto.getId()))){
						getCreativeById = false;
					}
					if (getPluginCreativeInfo && 
							adserverDomainCache.getPluginCreativeInfo(creativeDto.getId()) != null &&
							!adserverDomainCache.getPluginCreativeInfo(creativeDto.getId()).equals(adserverDataCache.getPluginCreativeInfo(creativeDto.getId()))){
						getPluginCreativeInfo = false;
					}
					if (getPluginCreativeInfo && 
							adserverDomainCache.getPluginCreativeInfo(creativeDto) != null &&
							!adserverDomainCache.getPluginCreativeInfo(creativeDto).equals(adserverDataCache.getPluginCreativeInfo(creativeDto))){
						getPluginCreativeInfo = false;
					}
					if (getRecentlyStoppedCreativeById && 
							adserverDomainCache.getRecentlyStoppedCreativeById(creativeDto.getId()) != null &&
							!compareCreatives(adserverDomainCache.getRecentlyStoppedCreativeById(creativeDto.getId()), adserverDataCache.getRecentlyStoppedCreativeById(creativeDto.getId()))){
						getRecentlyStoppedCreativeById = false;
					}
						
				}

				if (getPluginCreatives && adserverDomainCache.getPluginCreatives() != null && adserverDataCache.getPluginCreatives() != null){
					if (adserverDomainCache.getPluginCreatives().length != adserverDataCache.getPluginCreatives().length) getPluginCreatives = false;
				}else{
					getPluginCreatives = false;
				}
				

				if (!getCreativeById) testResult.append(" ***** ERROR ***** getCreativeById INS'T WORKING! \n");
				if (!getPluginCreativeInfo) testResult.append(" ***** ERROR ***** getPluginCreativeInfo INS'T WORKING! \n");
				if (!getRecentlyStoppedCreativeById) testResult.append(" ***** ERROR ***** getRecentlyStoppedCreativeById INS'T WORKING! \n");
				if (!getPluginCreatives) testResult.append(" ***** ERROR ***** getPluginCreatives INS'T WORKING! \n");
				
			} else testResult.append("ONE OR MORE CREATIVES DON'T MATCH!\n");
		}else{
			String msg = "THE NUMBER OF CREATIVES IN DOMAIN AND DATA CACHE IS DIFFERENT!  Domain >>> " + creativeDtos1.length + " Data   >>> " + creativeDtos2.length;
			testResult.append(msg);
			return msg;
		}
		}catch (Exception e){
		    StringWriter stringWriter = new StringWriter();
		    PrintWriter printWriter = new PrintWriter(stringWriter);
		    e.printStackTrace(printWriter);
		    StringBuffer error = stringWriter.getBuffer();
			testResult.append(error.toString());
		}
		
		return testResult.toString();
	}
	
    private boolean compareCreatives(CreativeDto creativeDto1, CreativeDto creativeDto2){
    	
    	if ((creativeDto1 != null && creativeDto2 == null) || (creativeDto1 == null && creativeDto2 != null)) return false;
    	
    	if (creativeDto1 != null && creativeDto2 != null){
	    	if(!creativeDto1.getId().equals(creativeDto2.getId())) return false;
	    	if (creativeDto1.getCampaign() != null && creativeDto2.getCampaign() != null){
		    	if(!creativeDto1.getCampaign().getId().equals(creativeDto2.getCampaign().getId())) return false;
		    	if (creativeDto1.getCampaign().getAdvertiser() != null && creativeDto2.getCampaign().getAdvertiser() != null){
		    		if(!creativeDto1.getCampaign().getAdvertiser().getId().equals(creativeDto2.getCampaign().getAdvertiser().getId())) return false;
		    	}
	    	}
    	}
    	
    	return true;
    }
}
