package com.adfonic.adserver.deriver.impl;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.jmock.Expectations;
import org.junit.Before;
import org.junit.Test;

import com.adfonic.adserver.BaseAdserverTest;
import com.adfonic.adserver.Parameters;
import com.adfonic.adserver.TargetingContext;
import com.adfonic.adserver.deriver.DeriverManager;
import com.adfonic.domain.Medium;

public class TestMediumDeriver extends BaseAdserverTest {

	DeriverManager deriverManager;
	MediumDeriver mediumDeriver;
	private TargetingContext context;

	@Before
	public void initTests() {
		deriverManager = new DeriverManager();
		mediumDeriver = new MediumDeriver(deriverManager);
		context = mock(TargetingContext.class);
	}

	@Test
	public void testMediumDeriver01(){
		assertNull(mediumDeriver.getAttribute(TargetingContext.MARKUP_AVAILABLE, context));
	}
	
	@Test
	public void testMediumDeriver02(){
		expect(new Expectations() {{
			
			//1
			oneOf (context).getAttribute(Parameters.MEDIUM); will(returnValue(null));
			
			//2
			oneOf (context).getAttribute(Parameters.MEDIUM); will(returnValue("SITE"));
			
			//3
			oneOf (context).getAttribute(Parameters.MEDIUM); will(returnValue("APP"));
			
			//4
			oneOf (context).getAttribute(Parameters.MEDIUM); will(returnValue("APPLICATION"));
			
			//5
			oneOf (context).getAttribute(Parameters.MEDIUM); will(returnValue("something"));

            //6
            oneOf (context).getAttribute(Parameters.MEDIUM_DEPRECATED); will(returnValue("something"));

		}});
		
		assertNull(mediumDeriver.getAttribute(TargetingContext.MEDIUM, context));
		
		Medium medium = (Medium)mediumDeriver.getAttribute(TargetingContext.MEDIUM, context);
		assertNotNull(medium.equals(Medium.SITE));
		
		medium = (Medium)mediumDeriver.getAttribute(TargetingContext.MEDIUM, context);
		assertNotNull(medium.equals(Medium.APPLICATION));
		
		medium = (Medium)mediumDeriver.getAttribute(TargetingContext.MEDIUM, context);
		assertNotNull(medium.equals(Medium.APPLICATION));
		
		assertNull(mediumDeriver.getAttribute(TargetingContext.MEDIUM, context));
	}
	
}
