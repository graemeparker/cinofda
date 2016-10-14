package com.adfonic.adserver.plugin;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.SortedMap;

import org.junit.Before;
import org.junit.Test;

import com.adfonic.adserver.BaseAdserverTest;
import com.adfonic.adserver.plugin.PluginFillRateTracker.Outcome;
import com.adfonic.adserver.plugin.PluginFillRateTracker.PluginStats;

public class TestPluginFillRateTracker extends BaseAdserverTest {

	
	private PluginFillRateTracker pluginFillRateTracker;

	

	@Before
	public void initTests(){
		pluginFillRateTracker = new PluginFillRateTracker();
		
	}
	
	@Test
	public void testPluginFillRateTracker01_start(){
		inject(pluginFillRateTracker, "enabled", false);
		assertFalse(pluginFillRateTracker.isEnabled());
		pluginFillRateTracker.start();
		assertTrue(pluginFillRateTracker.isEnabled());
		assertNull(pluginFillRateTracker.getDateStopped());
		assertNotNull(pluginFillRateTracker.getDateStarted());
	}
	@Test
	public void testPluginFillRateTracker02_start(){
		inject(pluginFillRateTracker, "enabled", true);
		assertTrue(pluginFillRateTracker.isEnabled());
		pluginFillRateTracker.start();
		assertTrue(pluginFillRateTracker.isEnabled());
		assertNull(pluginFillRateTracker.getDateStopped());
		assertNull(pluginFillRateTracker.getDateStarted());
	}
	@Test
	public void testPluginFillRateTracker03_stop(){
		inject(pluginFillRateTracker, "enabled", false);
		assertFalse(pluginFillRateTracker.isEnabled());
		pluginFillRateTracker.stop();
		assertFalse(pluginFillRateTracker.isEnabled());
		assertNull(pluginFillRateTracker.getDateStopped());
		assertNull(pluginFillRateTracker.getDateStarted());
	}
	@Test
	public void testPluginFillRateTracker04_stop(){
		inject(pluginFillRateTracker, "enabled", true);
		assertTrue(pluginFillRateTracker.isEnabled());
		pluginFillRateTracker.stop();
		assertFalse(pluginFillRateTracker.isEnabled());
		assertNotNull(pluginFillRateTracker.getDateStopped());
		assertNull(pluginFillRateTracker.getDateStarted());
	}
	@Test
	public void testPluginFillRateTracker05_getPluginStats(){
		final String pluginName = randomAlphaString(20);
		pluginFillRateTracker.start();
		PluginStats pluginStats = pluginFillRateTracker.getPluginStats(pluginName);
		assertNotNull(pluginStats);
		PluginStats secondTimePluginStats = pluginFillRateTracker.getPluginStats(pluginName);
		assertNotNull(secondTimePluginStats);
		assertEquals(pluginStats, secondTimePluginStats);
		assertEquals(pluginName, pluginStats.getPluginName());
		
		int totalExpectedCount = 0;
		assertEquals(totalExpectedCount, pluginStats.getTotalCount());
		assertEquals(totalExpectedCount, pluginFillRateTracker.getTotalCount(pluginName));
		assertEquals(totalExpectedCount, pluginFillRateTracker.getCount(pluginName,PluginFillRateTracker.Outcome.FILLED));
	}
	@Test
	public void testPluginFillRateTracker06_getPluginStatsByPluginName(){
		final String pluginName = randomAlphaString(20);
		pluginFillRateTracker.start();
		PluginStats pluginStats = pluginFillRateTracker.getPluginStats(pluginName);
		assertNotNull(pluginStats);
		PluginStats secondTimePluginStats = pluginFillRateTracker.getPluginStats(pluginName);
		assertNotNull(secondTimePluginStats);
		assertEquals(pluginStats, secondTimePluginStats);
		SortedMap<String, PluginStats> sortedMap = pluginFillRateTracker.getPluginStatsByPluginName();
		assertNotNull(sortedMap);
		assertEquals(1, sortedMap.size());
	}
	/*
	 * When pluginFillRateTracker.start() has been called before doing any operation
	 */
	@Test
	public void testPluginFillRateTracker07_trackOutcome(){
		final String pluginName = randomAlphaString(20);
		pluginFillRateTracker.start();
		pluginFillRateTracker.trackOutcome(pluginName,Outcome.FILLED);
		pluginFillRateTracker.trackOutcome(pluginName,Outcome.TIMEOUT);
		pluginFillRateTracker.trackOutcome(pluginName,Outcome.TIMEOUT);
		pluginFillRateTracker.trackOutcome(pluginName,Outcome.UNFILLED);
		pluginFillRateTracker.trackOutcome(pluginName,Outcome.UNFILLED);
		pluginFillRateTracker.trackOutcome(pluginName,Outcome.UNFILLED);
		
		PluginStats pluginStats = pluginFillRateTracker.getPluginStats(pluginName);
		assertNotNull(pluginStats);
		assertEquals(pluginName, pluginStats.getPluginName());
		assertNotNull(pluginStats.getCountsByOutcome());
		
		int totalExpectedCount = 6;
		int totalFilledExpectedCount = 1;
		int totalTimeoutExpectedCount = 2;
		int totalUnFilledExpectedCount = 3;
		assertEquals(totalExpectedCount, pluginStats.getTotalCount());
		assertEquals(totalExpectedCount, pluginFillRateTracker.getTotalCount(pluginName));
		assertEquals(totalFilledExpectedCount, pluginFillRateTracker.getCount(pluginName,PluginFillRateTracker.Outcome.FILLED));
		assertEquals(totalUnFilledExpectedCount, pluginFillRateTracker.getCount(pluginName,PluginFillRateTracker.Outcome.UNFILLED));
		assertEquals(totalTimeoutExpectedCount, pluginFillRateTracker.getCount(pluginName,PluginFillRateTracker.Outcome.TIMEOUT));
		
		double expectedPercentageFilled = ((double)1/6);
		assertEquals(expectedPercentageFilled, pluginStats.getPercent(Outcome.FILLED),.01);
		double expectedPercentageTimeOut = ((double)2/6);
		assertEquals(expectedPercentageTimeOut, pluginStats.getPercent(Outcome.TIMEOUT),.01);
		double expectedPercentageUnFilled = ((double)3/6);
		assertEquals(expectedPercentageUnFilled, pluginStats.getPercent(Outcome.UNFILLED),.01);
	}
	/*
	 * When pluginFillRateTracker.start() has NOT been called before doing any operation
	 */
	@Test
	public void testPluginFillRateTracker08_trackOutcome(){
		final String pluginName = randomAlphaString(20);
		pluginFillRateTracker.trackOutcome(pluginName,Outcome.FILLED);
		pluginFillRateTracker.trackOutcome(pluginName,Outcome.TIMEOUT);
		pluginFillRateTracker.trackOutcome(pluginName,Outcome.TIMEOUT);
		pluginFillRateTracker.trackOutcome(pluginName,Outcome.UNFILLED);
		pluginFillRateTracker.trackOutcome(pluginName,Outcome.UNFILLED);
		pluginFillRateTracker.trackOutcome(pluginName,Outcome.UNFILLED);
		
		PluginStats pluginStats = pluginFillRateTracker.getPluginStats(pluginName);
		assertNotNull(pluginStats);
		assertEquals(pluginName, pluginStats.getPluginName());
		
		int totalExpectedCount = 0;
		int totalFilledExpectedCount = 0;
		int totalTimeoutExpectedCount = 0;
		int totalUnFilledExpectedCount = 0;
		assertEquals(totalExpectedCount, pluginStats.getTotalCount());
		assertEquals(totalExpectedCount, pluginFillRateTracker.getTotalCount(pluginName));
		assertEquals(totalFilledExpectedCount, pluginFillRateTracker.getCount(pluginName,PluginFillRateTracker.Outcome.FILLED));
		assertEquals(totalUnFilledExpectedCount, pluginFillRateTracker.getCount(pluginName,PluginFillRateTracker.Outcome.UNFILLED));
		assertEquals(totalTimeoutExpectedCount, pluginFillRateTracker.getCount(pluginName,PluginFillRateTracker.Outcome.TIMEOUT));
	}
}
