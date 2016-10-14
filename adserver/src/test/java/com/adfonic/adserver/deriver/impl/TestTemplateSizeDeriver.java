package com.adfonic.adserver.deriver.impl;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.awt.Dimension;

import org.jmock.Expectations;
import org.junit.Before;
import org.junit.Test;

import com.adfonic.adserver.BaseAdserverTest;
import com.adfonic.adserver.Parameters;
import com.adfonic.adserver.TargetingContext;
import com.adfonic.adserver.deriver.DeriverManager;

public class TestTemplateSizeDeriver extends BaseAdserverTest {

	DeriverManager deriverManager;
	TemplateSizeDeriver templateSizeDeriver;
	private TargetingContext context;

	@Before
	public void initTests() {
		deriverManager = new DeriverManager();
		templateSizeDeriver = new TemplateSizeDeriver(deriverManager);
		context = mock(TargetingContext.class);
	}

	@Test
	public void testTemplateSizeDeriver01(){
		assertNull(templateSizeDeriver.getAttribute(TargetingContext.MARKUP_AVAILABLE, context));
	}
	
	@Test
	public void testTemplateSizeDeriver02(){
		
		expect(new Expectations() {{
			//1
			oneOf (context).getAttribute(Parameters.TEMPLATE_WIDTH); will(returnValue(null));
			//2
			oneOf (context).getAttribute(Parameters.TEMPLATE_WIDTH); will(returnValue("something"));
			oneOf (context).getAttribute(Parameters.TEMPLATE_HEIGHT); will(returnValue(null));
			//3
			oneOf (context).getAttribute(Parameters.TEMPLATE_WIDTH); will(returnValue("something"));
			oneOf (context).getAttribute(Parameters.TEMPLATE_HEIGHT); will(returnValue("24"));
			//4
			oneOf (context).getAttribute(Parameters.TEMPLATE_WIDTH); will(returnValue("267"));
			oneOf (context).getAttribute(Parameters.TEMPLATE_HEIGHT); will(returnValue("50"));
			//5
			oneOf (context).getAttribute(Parameters.TEMPLATE_WIDTH); will(returnValue("320"));
			oneOf (context).getAttribute(Parameters.TEMPLATE_HEIGHT); will(returnValue("90"));
			//6
			oneOf (context).getAttribute(Parameters.TEMPLATE_WIDTH); will(returnValue("320"));
			oneOf (context).getAttribute(Parameters.TEMPLATE_HEIGHT); will(returnValue("something"));
			//7
			oneOf (context).getAttribute(Parameters.TEMPLATE_WIDTH); will(returnValue("320"));
			oneOf (context).getAttribute(Parameters.TEMPLATE_HEIGHT); will(returnValue("50"));
			//8
			oneOf (context).getAttribute(Parameters.TEMPLATE_WIDTH); will(returnValue("320"));
			oneOf (context).getAttribute(Parameters.TEMPLATE_HEIGHT); will(returnValue("50"));
		}});
		
		assertNull(templateSizeDeriver.getAttribute(TargetingContext.TEMPLATE_SIZE, context));
		assertNull(templateSizeDeriver.getAttribute(TargetingContext.TEMPLATE_SIZE, context));
		assertNull(templateSizeDeriver.getAttribute(TargetingContext.TEMPLATE_SIZE, context));
		assertTrue(templateSizeDeriver.getAttribute(TargetingContext.TEMPLATE_SIZE, context) != null);
		assertTrue(templateSizeDeriver.getAttribute(TargetingContext.TEMPLATE_SIZE, context) != null);
		assertNull(templateSizeDeriver.getAttribute(TargetingContext.TEMPLATE_SIZE, context));
		assertTrue(templateSizeDeriver.getAttribute(TargetingContext.TEMPLATE_SIZE, context) != null);
		Dimension dim = (Dimension)templateSizeDeriver.getAttribute(TargetingContext.TEMPLATE_SIZE, context);
		assertTrue(dim.getHeight() == 50);
		assertTrue(dim.getWidth() == 320);
	}
	
}
