package com.adfonic.adserver.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import com.adfonic.adserver.BaseAdserverTest;
import com.adfonic.adserver.MutableWeightedCreative;
import com.adfonic.adserver.impl.BasicTargetingEngineImpl.AverageWeightedCampaign;
import com.adfonic.domain.cache.dto.adserver.creative.CampaignDto;
import com.adfonic.domain.cache.dto.adserver.creative.CreativeDto;

public class TestAverageWeightedCampaign extends BaseAdserverTest{

	private AverageWeightedCampaign averageWeightedCampaign;
	private CampaignDto campaign;
	private double creativeExponent;
	
	
	@Before
	public void init(){
		campaign = new CampaignDto();
		creativeExponent = 1.2;
		averageWeightedCampaign = new BasicTargetingEngineImpl.AverageWeightedCampaign(campaign,creativeExponent);
	}
	/*
	 * Test with one creative in one campaign
	 * and creative has ecpm weight more then 0
	 */
	@Test
	public void test01(){
		final long adSpaceId = randomLong();
		final CreativeDto creative = new CreativeDto();
		MutableWeightedCreative mutableWeightedCreative = new MutableWeightedCreative();
		mutableWeightedCreative.setAdSpaceId(adSpaceId);
		mutableWeightedCreative.setCreative(creative);
		mutableWeightedCreative.setEcpmWeight(3.4);
		
		averageWeightedCampaign.addMutableWeightedCreative(mutableWeightedCreative);
		
		double campaignWeight = averageWeightedCampaign.getWeight();
		
    	double creativeExponentForNumerator = 1.0 + creativeExponent;
    	double numerator =  Math.pow(mutableWeightedCreative.getWeight(), creativeExponentForNumerator);
    	double denominator =  Math.pow(mutableWeightedCreative.getWeight(), creativeExponent);
    	double expectedCampaignWeight;	
    		
    	if(denominator == 0){
    		expectedCampaignWeight = 0.0;
    	}else{
    		expectedCampaignWeight = numerator / denominator;	
    	}
		  
    	assertEquals(campaignWeight,expectedCampaignWeight,.01);
	}
	
	/*
	 * Test with two creative in one campaign
	 * and both have ecpmWeight > 0
	 */
	@Test
	public void test02(){
		final long adSpaceId = randomLong();
		final CreativeDto creative1 = new CreativeDto();
		final CreativeDto creative2 = new CreativeDto();
		MutableWeightedCreative mutableWeightedCreative1 = new MutableWeightedCreative();
		mutableWeightedCreative1.setAdSpaceId(adSpaceId);
		mutableWeightedCreative1.setCreative(creative1);
		mutableWeightedCreative1.setEcpmWeight(1.4);
		
		MutableWeightedCreative mutableWeightedCreative2 = new MutableWeightedCreative();
		mutableWeightedCreative2.setAdSpaceId(adSpaceId);
		mutableWeightedCreative2.setCreative(creative2);
		mutableWeightedCreative2.setEcpmWeight(3.4);

		averageWeightedCampaign.addMutableWeightedCreative(mutableWeightedCreative1);
		averageWeightedCampaign.addMutableWeightedCreative(mutableWeightedCreative2);
		
		double campaignWeight = averageWeightedCampaign.getWeight();
		
    	double creativeExponentForNumerator = 1.0 + creativeExponent;
    	double numerator =  Math.pow(mutableWeightedCreative1.getWeight(), creativeExponentForNumerator) + Math.pow(mutableWeightedCreative2.getWeight(), creativeExponentForNumerator);
    	double denominator =  Math.pow(mutableWeightedCreative1.getWeight(), creativeExponent) + Math.pow(mutableWeightedCreative2.getWeight(), creativeExponent);
    	double expectedCampaignWeight = numerator / denominator;		
    		
    	assertEquals(campaignWeight,expectedCampaignWeight,.01);
	}
	
	/*
	 * Test with one creative in one campaign
	 * and creative has ecpm weight is 0
	 */
	@Test
	public void test03(){
		final long adSpaceId = randomLong();
		final CreativeDto creative = new CreativeDto();
		MutableWeightedCreative mutableWeightedCreative = new MutableWeightedCreative();
		mutableWeightedCreative.setAdSpaceId(adSpaceId);
		mutableWeightedCreative.setCreative(creative);
		mutableWeightedCreative.setEcpmWeight(0.0);
		
		averageWeightedCampaign.addMutableWeightedCreative(mutableWeightedCreative);
		
		double campaignWeight = averageWeightedCampaign.getWeight();
		
    	double expectedCampaignWeight = 0;	
    	assertEquals(campaignWeight,expectedCampaignWeight,.01);
	}
	
	/*
	 * Test with two creative in one campaign
	 * one creative has ecpmWeight more then 0 and once creative has ecpmWeight == 0
	 */
	@Test
	public void test04(){
		final long adSpaceId = randomLong();
		final CreativeDto creative1 = new CreativeDto();
		final CreativeDto creative2 = new CreativeDto();
		MutableWeightedCreative mutableWeightedCreative1 = new MutableWeightedCreative();
		mutableWeightedCreative1.setAdSpaceId(adSpaceId);
		mutableWeightedCreative1.setCreative(creative1);
		mutableWeightedCreative1.setEcpmWeight(1.4);
		
		MutableWeightedCreative mutableWeightedCreative2 = new MutableWeightedCreative();
		mutableWeightedCreative2.setAdSpaceId(adSpaceId);
		mutableWeightedCreative2.setCreative(creative2);
		mutableWeightedCreative2.setEcpmWeight(0);

		averageWeightedCampaign.addMutableWeightedCreative(mutableWeightedCreative1);
		averageWeightedCampaign.addMutableWeightedCreative(mutableWeightedCreative2);
		
		double campaignWeight = averageWeightedCampaign.getWeight();
		
    	double creativeExponentForNumerator = 1.0 + creativeExponent;
    	double numerator =  Math.pow(mutableWeightedCreative1.getWeight(), creativeExponentForNumerator) + Math.pow(mutableWeightedCreative2.getWeight(), creativeExponentForNumerator);
    	double denominator =  Math.pow(mutableWeightedCreative1.getWeight(), creativeExponent) + Math.pow(mutableWeightedCreative2.getWeight(), creativeExponent);
    	double expectedCampaignWeight = numerator / denominator;	
    		
    	assertEquals(campaignWeight,expectedCampaignWeight,.01);
    	assertEquals(campaign,averageWeightedCampaign.getCampaign());
    	assertEquals(2,averageWeightedCampaign.getMutableWeightedCreatives().size());
    	
    	//call getWeight one more time and make sure it returns the same weight
    	
    	campaignWeight = averageWeightedCampaign.getWeight();
    	assertEquals(campaignWeight,expectedCampaignWeight,.01);
	}
	
	/*
	 * Test with two creative in one campaign
	 * first add both and check weights 
	 * and then remove one of then and then check weights
	 * and then remove other one and then check weights
	 * and both have ecpmWeight > 0
	 */
	@Test
	public void test05(){
		final long adSpaceId = randomLong();
		final CreativeDto creative1 = new CreativeDto();
		final CreativeDto creative2 = new CreativeDto();
		MutableWeightedCreative mutableWeightedCreative1 = new MutableWeightedCreative();
		mutableWeightedCreative1.setAdSpaceId(adSpaceId);
		mutableWeightedCreative1.setCreative(creative1);
		mutableWeightedCreative1.setEcpmWeight(1.4);
		
		MutableWeightedCreative mutableWeightedCreative2 = new MutableWeightedCreative();
		mutableWeightedCreative2.setAdSpaceId(adSpaceId);
		mutableWeightedCreative2.setCreative(creative2);
		mutableWeightedCreative2.setEcpmWeight(3.4);

		averageWeightedCampaign.addMutableWeightedCreative(mutableWeightedCreative1);
		averageWeightedCampaign.addMutableWeightedCreative(mutableWeightedCreative2);
		
		double campaignWeight = averageWeightedCampaign.getWeight();
		
    	double creativeExponentForNumerator = 1.0 + creativeExponent;
    	double numerator =  Math.pow(mutableWeightedCreative1.getWeight(), creativeExponentForNumerator) + Math.pow(mutableWeightedCreative2.getWeight(), creativeExponentForNumerator);
    	double denominator =  Math.pow(mutableWeightedCreative1.getWeight(), creativeExponent) + Math.pow(mutableWeightedCreative2.getWeight(), creativeExponent);
    	double expectedCampaignWeight = numerator / denominator;		
    		
    	assertEquals(campaignWeight,expectedCampaignWeight,.01);
    	
    	//Remove first creative
    	averageWeightedCampaign.removeMutableWeightedCreative(mutableWeightedCreative2);
    	
    	campaignWeight = averageWeightedCampaign.getWeight();
    	
    	numerator =  Math.pow(mutableWeightedCreative1.getWeight(), creativeExponentForNumerator);
    	denominator =  Math.pow(mutableWeightedCreative1.getWeight(), creativeExponent);
    	expectedCampaignWeight = numerator / denominator;		
    	
    	assertEquals(campaignWeight,expectedCampaignWeight,.01);
    	
    	//Remove second creative
    	averageWeightedCampaign.removeMutableWeightedCreative(mutableWeightedCreative1);
    	
    	campaignWeight = averageWeightedCampaign.getWeight();
    	
    	expectedCampaignWeight = 0.0;		
    	
    	assertEquals(campaignWeight,expectedCampaignWeight,.01);
    	assertTrue(averageWeightedCampaign.isEmpty());
	}
	
}
