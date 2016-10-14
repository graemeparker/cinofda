package com.adfonic.adserver.deriver.impl;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.jmock.Expectations;
import org.junit.Before;
import org.junit.Test;

import com.adfonic.adserver.BaseAdserverTest;
import com.adfonic.adserver.Parameters;
import com.adfonic.adserver.TargetingContext;
import com.adfonic.adserver.deriver.DeriverManager;
import com.adfonic.domain.Gender;

public class TestGenderDeriver extends BaseAdserverTest {

	DeriverManager deriverManager;
	GenderDeriver genderDeriver;
	private TargetingContext context;

	@Before
	public void initTests() {
		deriverManager = new DeriverManager();
		genderDeriver = new GenderDeriver(deriverManager);
		context = mock(TargetingContext.class);
	}

	@Test
	public void testGenderDeriver01(){
		
		assertNull(genderDeriver.getAttribute(TargetingContext.MARKUP_AVAILABLE, context));
	}
	
	@Test
	public void testGenderDeriver02(){
		
		expect(new Expectations() {{
			oneOf (context).getAttribute(Parameters.GENDER); will(returnValue(null));
			oneOf (context).getAttribute(Parameters.GENDER); will(returnValue("m"));
			oneOf (context).getAttribute(Parameters.GENDER); will(returnValue("M"));
			oneOf (context).getAttribute(Parameters.GENDER); will(returnValue("f"));
			oneOf (context).getAttribute(Parameters.GENDER); will(returnValue("F"));
			oneOf (context).getAttribute(Parameters.GENDER); will(returnValue("l"));
			oneOf (context).getAttribute(Parameters.GENDER); will(returnValue(" lo"));
			oneOf (context).getAttribute(Parameters.GENDER); will(returnValue("2"));
		}});
		assertNull(genderDeriver.getAttribute(TargetingContext.GENDER, context));
		
		Gender gender = (Gender)genderDeriver.getAttribute(TargetingContext.GENDER, context);
		assertTrue(gender.equals(Gender.MALE));
		
		gender = (Gender)genderDeriver.getAttribute(TargetingContext.GENDER, context);
		assertTrue(gender.equals(Gender.MALE));
		
		gender = (Gender)genderDeriver.getAttribute(TargetingContext.GENDER, context);
		assertTrue(gender.equals(Gender.FEMALE));
		
		gender = (Gender)genderDeriver.getAttribute(TargetingContext.GENDER, context);
		assertTrue(gender.equals(Gender.FEMALE));
		
		assertNull(genderDeriver.getAttribute(TargetingContext.GENDER, context));
		assertNull(genderDeriver.getAttribute(TargetingContext.GENDER, context));
		assertNull(genderDeriver.getAttribute(TargetingContext.GENDER, context));
	}

}
