package com.adfonic.adserver;

import org.junit.Before;
import org.junit.Test;

public class TestTargetingEventAdapter extends BaseAdserverTest {

	private TargetingEventAdapter targetingEventAdapter;
	
	@Before
	public void initTest(){
		targetingEventAdapter = new TargetingEventAdapter();
	}
	
	@Test
	public void testTargetingEventAdapter1(){
		targetingEventAdapter.attributesDerived(null, null);
		targetingEventAdapter.creativeEliminated(null, null, null,null, null);
		targetingEventAdapter.creativeSelected(null, null, null);
		targetingEventAdapter.creativesEligible(null, null, null);
		targetingEventAdapter.creativesTargeted(null, null, 0,null);
		targetingEventAdapter.unfilledRequest(null, null);
		targetingEventAdapter.timeLimitExpired(null, null, null);
	}
}
