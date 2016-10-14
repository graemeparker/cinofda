package com.adfonic.adserver;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.time.DateUtils;
import org.junit.Test;

public class TestStoppage  extends BaseAdserverTest{

	private Stoppage stoppage;
	
	@Test
	public void testStoppage1() throws InterruptedException {
		int stoppageDurationMs = 500;
        long now = System.currentTimeMillis();
        Long reactivateDate = now + stoppageDurationMs;
        
		stoppage = new Stoppage(now, reactivateDate);
		assertEquals(now, stoppage.getTimestamp());
		assertEquals(reactivateDate, stoppage.getReactivateDate());
		assertTrue(stoppage.isStillInEffect());
        
		TimeUnit.MILLISECONDS.sleep(stoppageDurationMs + 25);
		assertFalse(stoppage.isStillInEffect());
	}
	
	@Test
	public void testStoppage2() throws InterruptedException {
		int stoppageDurationMs = 500;
        Date now = new Date();
        Date reactivateDate = DateUtils.addMilliseconds(now, stoppageDurationMs);
        
		stoppage = new Stoppage(now, reactivateDate);
		assertEquals(now.getTime(), stoppage.getTimestamp());
		assertEquals(Long.valueOf(reactivateDate.getTime()), stoppage.getReactivateDate());
		assertTrue(stoppage.isStillInEffect());

		TimeUnit.MILLISECONDS.sleep(stoppageDurationMs + 25);
		assertFalse(stoppage.isStillInEffect());
	}
	
	@Test
	public void testStoppage3() throws InterruptedException {
		int stoppageDurationMs = 500;
        long now = System.currentTimeMillis();
        
		stoppage = new Stoppage(now, null);
		assertEquals(now, stoppage.getTimestamp());
		assertNull(stoppage.getReactivateDate());
		assertTrue(stoppage.isStillInEffect());
        
		TimeUnit.MILLISECONDS.sleep(stoppageDurationMs + 25);
		assertTrue(stoppage.isStillInEffect());
	}
}
