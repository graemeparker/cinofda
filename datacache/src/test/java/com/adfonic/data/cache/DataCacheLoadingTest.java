package com.adfonic.data.cache;

import java.math.BigDecimal;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.adfonic.domain.BidType;
import com.adfonic.domain.cache.dto.adserver.EcpmInfo;
import com.adfonic.domain.cache.dto.adserver.ExpectedStatsDto;
import com.adfonic.domain.cache.dto.adserver.PlatformDto;
import com.adfonic.domain.cache.dto.adserver.adspace.AdSpaceDto;
import com.adfonic.domain.cache.dto.adserver.adspace.PublicationDto;
import com.adfonic.domain.cache.dto.adserver.adspace.PublisherDto;
import com.adfonic.domain.cache.dto.adserver.adspace.RtbConfigDto;
import com.adfonic.domain.cache.dto.adserver.creative.AdvertiserDto;
import com.adfonic.domain.cache.dto.adserver.creative.CampaignBidDto;
import com.adfonic.domain.cache.dto.adserver.creative.CampaignDto;
import com.adfonic.domain.cache.dto.adserver.creative.CompanyDto;
import com.adfonic.domain.cache.dto.adserver.creative.CreativeDto;

@ContextConfiguration(locations={"classpath:/test-cache-scheduling-context.xml"})
@RunWith(SpringJUnit4ClassRunner.class)
public class DataCacheLoadingTest {
		
	@Autowired
	private AdserverDataCacheManager adserverDataCacheManager;

	@Ignore
    @Test
	public void test() throws Exception {
		while(true){
			AdserverDataCache cache = adserverDataCacheManager.getCache();
			// This call will be routed to the right cache (domain/data)
			// upon the settings in cacheDelegator.properties
			AdSpaceDto adSpaceDto = new AdSpaceDto();
			adSpaceDto.setId(1191L);
			PublicationDto publicationDto = new PublicationDto();
			PublisherDto publisher = new PublisherDto();
			RtbConfigDto rtbConfigDto = new RtbConfigDto();
			publisher.setRtbConfig(rtbConfigDto);
			publicationDto.setPublisher(publisher);
			publicationDto.setId(1L);
			adSpaceDto.setPublication(publicationDto);
			
			CreativeDto creativeDto = new CreativeDto();
			creativeDto.setId(1851L);
			CampaignDto campaign = new CampaignDto();
			CampaignBidDto currentBid = new CampaignBidDto();
			currentBid.setBidType(BidType.CPC);
			campaign.setCurrentBid(currentBid);	
			campaign.setId(1L);
			AdvertiserDto advertiserDto = new AdvertiserDto();
			CompanyDto companyDto = new CompanyDto();
			companyDto.setMarginShareDSP(0);
			advertiserDto.setCompany(companyDto);
			campaign.setAdvertiser(advertiserDto);
			creativeDto.setCampaign(campaign);
			
			PlatformDto platformDto = new PlatformDto();
			platformDto.setId(1L);
			
			EcpmInfo ecpmInfo = new EcpmInfo();
			
			cache.computeEcpmInfo(adSpaceDto, creativeDto, platformDto, 1, BigDecimal.ZERO, ecpmInfo);
			Thread.sleep(10000);
		}
	}
	
}
