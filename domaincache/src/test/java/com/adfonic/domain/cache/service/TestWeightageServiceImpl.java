package com.adfonic.domain.cache.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.Before;
import org.junit.Test;
import org.springframework.util.StopWatch;

import com.adfonic.domain.BidType;
import com.adfonic.domain.RtbConfig.RtbAuctionType;
import com.adfonic.domain.cache.dto.SystemVariable;
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
import com.adfonic.test.AbstractAdfonicTest;
import com.adfonic.util.DaemonThreadFactory;

public class TestWeightageServiceImpl extends AbstractAdfonicTest{

	WeightageServicesImpl weightageServices;
	@Before
	public void initTests(){
		weightageServices = new WeightageServicesImpl();
	}
	/*
	@Test
	public void testGetEcpmPerformanceMock(){
		final AdSpaceDto adspace = mock(AdSpaceDto.class, "adspace");
		final long adSpaceId = randomLong();
		final CreativeDto creative = mock(CreativeDto.class, "creative");
		final long creativeId = randomLong();
		final PlatformDto platform = mock(PlatformDto.class, "platform");
		final long platformId = randomLong();
		final long countryId = randomLong();
		final CampaignBidDto campaignBidDto = mock(CampaignBidDto.class, "campaignBidDto");
		final CampaignDto campaignDto = mock(CampaignDto.class, "campaignDto");
		final long campaignId = randomLong();
		final double amount = 2.5;
		final BidType bidType = BidType.CPA;
		final PublicationDto publicationDto = mock(PublicationDto.class, "publicationDto");
		final AdvertiserDto advertiserDto = mock(AdvertiserDto.class, "advertiserDto");
		final CompanyDto companyDto = mock(CompanyDto.class, "companyDto");
		final double discount = .1;
		final double buyerPremium = .1;
		final PublisherDto publisherDto = mock(PublisherDto.class, "publisherDto");
		final double revShare = .7;
		final RtbConfigDto rtbConfig = mock(RtbConfigDto.class, "RtbConfig");
		
		expect(new Expectations() {{
    		
			allowing (creative).getCampaign();will(returnValue(campaignDto));
			allowing (creative).getId();will(returnValue(creativeId));
			allowing (campaignDto).getCurrentBid();will(returnValue(campaignBidDto));
			allowing (campaignBidDto).getBidType();will(returnValue(bidType));
			allowing (campaignBidDto).getAmount();will(returnValue(amount));
			
			allowing (adspace).getId();will(returnValue(adSpaceId));
			allowing (adspace).getPublication();will(returnValue(publicationDto));
			allowing (publicationDto).getPublisher();will(returnValue(publisherDto));
			allowing (publisherDto).getCurrentRevShare();will(returnValue(revShare));
			allowing (publisherDto).getBuyerPremium();will(returnValue(buyerPremium));
			allowing (publisherDto).getRtbConfig();will(returnValue(rtbConfig));
			allowing (platform).getId();will(returnValue(platformId));
			allowing (campaignDto).getId();will(returnValue(campaignId));
			allowing (campaignDto).getAdvertiser();will(returnValue(advertiserDto));
			allowing (advertiserDto).getCompany();will(returnValue(companyDto));
			allowing (companyDto).getDiscount();will(returnValue(discount));
			
				
		}});			
		
		
		StopWatch stopWatch = new StopWatch("ECPM test");
		
		final int numberOfThrads = 10;
		final int totalCalculations = 10000;
		stopWatch.start("Running " + totalCalculations+ " calc in "+numberOfThrads+" threads");
		final ExecutorService futuresExecutorService = Executors.newFixedThreadPool(numberOfThrads, DaemonThreadFactory.getInstance());
		
		EcpmTask ecpmTask;
		List<EcpmTask> allTasks = new ArrayList<TestWeightageServiceImpl.EcpmTask>(numberOfThrads + 20);
		List<Future<Boolean>> allFutures = new ArrayList<Future<Boolean>>(numberOfThrads + 20);
		for(int i=0;i<numberOfThrads;i++){
			ecpmTask = new EcpmTask(adspace, creative, platform, countryId, totalCalculations);
			allTasks.add(ecpmTask);
		}
		for(EcpmTask oneEcpmTask:allTasks){
			allFutures.add(futuresExecutorService.submit(oneEcpmTask));
		}
		try {
			System.out.println("Waiting for thread to finish");
			int total = 0;
			for(Future<Boolean> oneFuture:allFutures){
				total++;
				System.out.println("Thread Done "+total +" "+ oneFuture.get());
			}
			System.out.println("All Threads finished");
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		stopWatch.stop();
		System.out.println(stopWatch.prettyPrint());
		
		//weightageServices.getEcpm(adspace, creative, platform, countryId);
	}
	*/
	
	public void testGetEcpmPerformanceReal(){
		StopWatch stopWatch = new StopWatch("ECPM test");
		
		//Pre populate countryWeighting Map
		for(long i=0;i<30;i++){
			for(long j=0;j<10;j++){
				weightageServices.addCampaignCountryWeight(i, j, 2.3);
			}
		}
		
		weightageServices.addSystemVariable(new SystemVariable("network_default_ctr",null,2.3));
		weightageServices.addSystemVariable(new SystemVariable("network_max_expected_rgr",null,1.3));
		weightageServices.addSystemVariable(new SystemVariable("network_default_cvr",null,1.4));
		//weightageServices.addSystemVariable(new SystemVariable("network_max_expected_rgr",null,1.3));
		final ExecutorService futuresExecutorService = Executors.newFixedThreadPool(100, DaemonThreadFactory.getInstance());

		testEcpmPerformance(futuresExecutorService,10, 1000,200, stopWatch);
		testEcpmPerformance(futuresExecutorService,10, 10000,400, stopWatch);
		testEcpmPerformance(futuresExecutorService,100, 1000, 550,stopWatch);
		testEcpmPerformance(futuresExecutorService,100, 10000, 7000,stopWatch);
		//testEcpmPerformance(100, 10000, 7000,stopWatch);
		
		
		System.out.println(stopWatch.prettyPrint());
		//Test results
		
		//weightageServices.getEcpm(adspace, creative, platform, countryId);
	}
	private void testEcpmPerformance(ExecutorService futuresExecutorService,int numberOfThreads,int totalCalculations,int targetTime,StopWatch stopWatch){
		stopWatch.start("Running " + totalCalculations+ " calc in "+numberOfThreads+" threads");
		
		EcpmTask ecpmTask;
		List<EcpmTask> allTasks = new ArrayList<TestWeightageServiceImpl.EcpmTask>(numberOfThreads + 20);
		List<Future<Boolean>> allFutures = new ArrayList<Future<Boolean>>(numberOfThreads + 20);
		
		for(int i=0;i<numberOfThreads;i++){
			
			ecpmTask = new EcpmTask(totalCalculations);
			allTasks.add(ecpmTask);
		}
		for(EcpmTask oneEcpmTask:allTasks){
			allFutures.add(futuresExecutorService.submit(oneEcpmTask));
		}
		try {
			System.out.println("Waiting for threads to finish");
			int total = 0;
			for(Future<Boolean> oneFuture:allFutures){
				total++;
				oneFuture.get();
				//System.out.println("Thread Done "+total +" "+ oneFuture.get());
			}
			//System.out.println("All Threads finished");
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		stopWatch.stop();
		if(stopWatch.getLastTaskTimeMillis() > targetTime){
			fail("Performance Test for ECPM failed, for "+numberOfThreads+ " thread and "+totalCalculations+ " calculations on each thread should have taken less then "+targetTime+" ms");
		}
		

	}
	public class EcpmTask implements Callable<Boolean>{
		AdSpaceDto adspace;
		CreativeDto creative;
		PlatformDto platform;
		long countryId;
		int totalCalculations;

		public EcpmTask(final AdSpaceDto adspace,final CreativeDto creative,final PlatformDto platform,final long countryId,int totalCalculations){
			this.adspace = adspace;
			this.creative = creative;
			this.platform = platform;
			this.countryId = countryId;
			this.totalCalculations = totalCalculations;
		}
		public EcpmTask(int totalCalculations){
			this.totalCalculations = totalCalculations;
		}

		@Override
		public Boolean call() throws Exception {
			for(int i=0;i<totalCalculations;i++){
				final AdSpaceDto adspace = new AdSpaceDto();
				final long adSpaceId = randomLong();
				final CreativeDto creative = new CreativeDto();
				final long creativeId = randomLong();
				final PlatformDto platform = new PlatformDto();
				final long platformId = randomLong();
				final CampaignBidDto campaignBidDto = new CampaignBidDto();
				final CampaignDto campaignDto = new CampaignDto();
				final long campaignId = randomLong();
				final double amount = 2.5;
				final BidType bidType = BidType.CPA;
				final PublicationDto publicationDto = new PublicationDto();
				final AdvertiserDto advertiserDto = new AdvertiserDto();
				final CompanyDto companyDto = new CompanyDto();
				final double discount = .1;
				final double marginShareDsp = .1;
				final double buyerPremium = .1;
				final PublisherDto publisherDto = new PublisherDto();
				final double revShare = .7;
				final RtbConfigDto rtbConfig = new RtbConfigDto();
				
				creative.setCampaign(campaignDto);
				creative.setId(creativeId);
				campaignDto.setCurrentBid(campaignBidDto);
				campaignDto.setAgencyDiscount(discount);
				campaignBidDto.setBidType(bidType);
				campaignBidDto.setAmount(amount);
				adspace.setId(adSpaceId);
				adspace.setPublication(publicationDto);
				publicationDto.setPublisher(publisherDto);
				publisherDto.setCurrentRevShare(revShare);
				publisherDto.setBuyerPremium(buyerPremium);
				publisherDto.setRtbConfig(rtbConfig);
				platform.setId(platformId);
				campaignDto.setId(campaignId);
				campaignDto.setAdvertiser(advertiserDto);
				advertiserDto.setCompany(companyDto);
				companyDto.setMarginShareDSP(marginShareDsp);
				//TODO need to fix this
				//weightageServices.getEcpm(adspace, creative, platform, countryId);
			}
			return true;
		}

	}
}
