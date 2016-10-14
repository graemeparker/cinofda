package com.adfonic.data.cache.ecpm.loader;

import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import javax.sql.DataSource;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.adfonic.data.cache.AdserverDataCache;
import com.adfonic.data.cache.AdserverDataCacheImpl;
import com.adfonic.data.cache.ecpm.api.EcpmData;
import com.adfonic.data.cache.ecpm.api.EcpmDataRepository;
import com.adfonic.data.cache.ecpm.repository.EcpmRepositoryIncremental;
import com.adfonic.domain.cache.AdserverDomainCacheManager;
import com.adfonic.domain.cache.DomainCache;
import com.adfonic.domain.cache.DomainCacheLoader;
import com.adfonic.domain.cache.dto.adserver.PlatformDto;
import com.adfonic.domain.cache.dto.adserver.adspace.AdSpaceDto;
import com.adfonic.domain.cache.dto.adserver.creative.CreativeDto;
import com.adfonic.domain.cache.ext.AdserverDomainCache;
import com.adfonic.domain.cache.ext.util.AdfonicStopWatch;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:/test-ecpm-cache-loader-context.xml"})
public class EcpmDataCacheLoaderTest {

	private static final transient Logger LOG = Logger.getLogger(EcpmDataCacheLoaderTest.class.getName());

	@Autowired
    private EcpmDataCacheLoader ecpmDataCacheLoader;
	private AdserverDataCache ecpmDataCache;
	
	@Autowired
    private DataSource dataSource;
	
    @Autowired
    private DomainCacheLoader domainCacheLoader;
	
    @Autowired
    AdserverDomainCacheManager adserverDomainCacheManager;
   
    @Ignore
    @Test
    public void compareEcpmTest() throws Exception {
    	
    	DomainCache domainCache = domainCacheLoader.loadDomainCache();
    	AdserverDomainCache adserverDomainCache = adserverDomainCacheManager.getCache();
    	
    	
		EcpmDataRepository repositoryEcpm = new EcpmRepositoryIncremental();
		AdfonicStopWatch adfonicStopWatch = new AdfonicStopWatch();
		// This map has to contain all the AdSpaces we want to load in the cache
		Map<Long, CreativeDto> creatives = new HashMap<>();
		for (CreativeDto creativeDto : adserverDomainCache.getAllCreatives())
			creatives.put(creativeDto.getId(), creativeDto);
		Map<Long, AdSpaceDto> adSpaces = new HashMap<>();
		for (AdSpaceDto adSpaceDto : adserverDomainCache.getAllAdSpaces())
			adSpaces.put(adSpaceDto.getId(),adSpaceDto);  
		ecpmDataCacheLoader.loadECPMData(repositoryEcpm, adfonicStopWatch, creatives, adSpaces);
		ecpmDataCache = new AdserverDataCacheImpl(repositoryEcpm);
    	
		int i = 1;
    	for (AdSpaceDto adSpace : adserverDomainCache.getAllAdSpaces()){
    		for (CreativeDto creativeDto : adserverDomainCache.getAllCreatives()){
    			for (PlatformDto platformDto : domainCache.getPlatforms()){
    	
    				LOG.info("Iteration: " +i);
    				i++;
    				
    				if (i==74521){
    					System.out.println("STOP");
    				}
    				
    				// Old Computation
    				EcpmData result1 = new EcpmData();
    				adserverDomainCache.computeEcpmInfo(adSpace, creativeDto, platformDto, 1, BigDecimal.ZERO, result1.getEcpmInfo());
    				
			        // New Computation					
					EcpmData result2 = new EcpmData();
					ecpmDataCache.computeEcpmInfo(adSpace, creativeDto, platformDto, 1, BigDecimal.ZERO, result2.getEcpmInfo());

					// Comparison
					assertEquals(result1.getEcpmInfo().getExpectedRevenue(), result2.getEcpmInfo().getExpectedRevenue(), 0);
					assertEquals(result1.getEcpmInfo().getExpectedSettlementPrice(), result2.getEcpmInfo().getExpectedSettlementPrice(), 0);
					assertEquals(result1.getEcpmInfo().getExpectedProfit(), result2.getEcpmInfo().getExpectedProfit(), 0);
					assertEquals(result1.getEcpmInfo().getBidPrice(), result2.getEcpmInfo().getBidPrice(), 0);
					assertEquals(result1.getEcpmInfo().getWeight(), result2.getEcpmInfo().getWeight(), 0);
					assertEquals(result1.getEcpmInfo().getWinningProbability(), result2.getEcpmInfo().getWinningProbability(), 0);
    			}
    		}
    	}
    	
    }
    
}
