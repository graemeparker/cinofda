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
import com.adfonic.util.AcceptedLanguages;

public class TestLanguageDeriver extends BaseAdserverTest {

	DeriverManager deriverManager;
	LanguageDeriver languageDeriver;
	private TargetingContext context;

	@Before
	public void initTests() {
		deriverManager = new DeriverManager();
		languageDeriver = new LanguageDeriver(deriverManager);
		context = mock(TargetingContext.class);
	}

	@Test
	public void testLanguageDeriver01(){
		assertNull(languageDeriver.getAttribute(TargetingContext.MARKUP_AVAILABLE, context));
	}
	
	@Test
	public void testLanguageDeriver02(){
		expect(new Expectations() {{
			
			//1
			oneOf (context).getAttribute(Parameters.LANGUAGE); will(returnValue(null));
			oneOf (context).getHeader("Accept-Language");will(returnValue(null));
			
			//2
			oneOf (context).getAttribute(Parameters.LANGUAGE); will(returnValue(null));
			oneOf (context).getHeader("Accept-Language");will(returnValue(""));
			
			//3
			oneOf (context).getAttribute(Parameters.LANGUAGE); will(returnValue(""));
			oneOf (context).getHeader("Accept-Language");will(returnValue(""));
			
			//4
			oneOf (context).getAttribute(Parameters.LANGUAGE); will(returnValue("en-us"));
			
			//5
			oneOf (context).getAttribute(Parameters.LANGUAGE); will(returnValue(null));
			oneOf (context).getHeader("Accept-Language");will(returnValue("EN"));
			
		}});
		
		assertNull(languageDeriver.getAttribute(TargetingContext.ACCEPTED_LANGUAGES, context));
		assertNull(languageDeriver.getAttribute(TargetingContext.ACCEPTED_LANGUAGES, context));
		assertNull(languageDeriver.getAttribute(TargetingContext.ACCEPTED_LANGUAGES, context));
		
		AcceptedLanguages acceptedLanguages = (AcceptedLanguages)languageDeriver.getAttribute(TargetingContext.ACCEPTED_LANGUAGES, context);
		assertNotNull(acceptedLanguages);
		
		AcceptedLanguages acceptedLanguages2 = (AcceptedLanguages)languageDeriver.getAttribute(TargetingContext.ACCEPTED_LANGUAGES, context);
		assertNotNull(acceptedLanguages2);
	}
	
}
