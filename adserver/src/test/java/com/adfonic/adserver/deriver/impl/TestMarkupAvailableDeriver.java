package com.adfonic.adserver.deriver.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.jmock.Expectations;
import org.junit.Before;
import org.junit.Test;

import com.adfonic.adserver.Parameters;
import com.adfonic.adserver.TargetingContext;
import com.adfonic.adserver.deriver.DeriverManager;
import com.adfonic.test.AbstractAdfonicTest;

public class TestMarkupAvailableDeriver extends AbstractAdfonicTest {
    private MarkupAvailableDeriver deriver;
    private TargetingContext context;

    @Before
    public void runBeforeEachTest() {
        deriver = new MarkupAvailableDeriver(new DeriverManager());
        context = mock(TargetingContext.class, "context");
    }
    
    @Test
    public void testGetAttribute01_no_param_returns_true() {
        // With no parameters specified at all, markup should be available
        expect(new Expectations() {{
            oneOf (context).getAttribute(Parameters.MARKUP); will(returnValue(null));
        }});
        assertEquals(Boolean.TRUE, deriver.getAttribute(TargetingContext.MARKUP_AVAILABLE, context));
    }
    
    @Test
    public void testGetAttribute02_param_1_returns_true() {
        expect(new Expectations() {{
            oneOf (context).getAttribute(Parameters.MARKUP); will(returnValue("1"));
        }});
        assertEquals(Boolean.TRUE, deriver.getAttribute(TargetingContext.MARKUP_AVAILABLE, context));
    }
    
    @Test
    public void testGetAttribute03_param_true_returns_true() {
        expect(new Expectations() {{
            oneOf (context).getAttribute(Parameters.MARKUP); will(returnValue("true"));
        }});
        assertEquals(Boolean.TRUE, deriver.getAttribute(TargetingContext.MARKUP_AVAILABLE, context));
    }
    
    @Test
    public void testGetAttribute04_no_param_format_html() {
        expect(new Expectations() {{
            oneOf (context).getAttribute(Parameters.MARKUP); will(returnValue("something other than null or 1 or true"));
            oneOf (context).getAttribute(Parameters.FORMAT); will(returnValue("html"));
        }});
        assertEquals(Boolean.TRUE, deriver.getAttribute(TargetingContext.MARKUP_AVAILABLE, context));
    }
    
    @Test
    public void testGetAttribute05_no_param_format_non_html() {
        expect(new Expectations() {{
            oneOf (context).getAttribute(Parameters.MARKUP); will(returnValue("something other than null or 1 or true"));
            oneOf (context).getAttribute(Parameters.FORMAT); will(returnValue("not html"));
        }});
        assertEquals(Boolean.FALSE, deriver.getAttribute(TargetingContext.MARKUP_AVAILABLE, context));
    }

    @Test
    public void testGetAttribute06_invalid_attribute() {
        assertNull(deriver.getAttribute(randomAlphaNumericString(10), context));
    }
}
