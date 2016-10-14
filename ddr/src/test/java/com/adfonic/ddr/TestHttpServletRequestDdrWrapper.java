package com.adfonic.ddr;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import javax.servlet.http.HttpServletRequest;

import org.jmock.Expectations;
import org.junit.Before;
import org.junit.Test;

import com.adfonic.test.AbstractAdfonicTest;

public class TestHttpServletRequestDdrWrapper extends AbstractAdfonicTest {
    private HttpServletRequest request;
    private HttpServletRequestDdrWrapper wrapper;
    
    @Before
    public void runBeforeEachTest() {
        request = mock(HttpServletRequest.class, "request");
        wrapper = new HttpServletRequestDdrWrapper(request);
    }

    @Test
    public void test01_getEffectiveUserAgent_set() {
        String ua = randomAlphaNumericString(10);
        inject(wrapper, "effectiveUserAgent", ua);
        assertEquals(ua, wrapper.getEffectiveUserAgent());
    }
    
    @Test
    public void test02_getEffectiveUserAgent_not_set() {
        final String ua = randomAlphaNumericString(10);
        expect(new Expectations() {{
            oneOf (request).getHeader("User-Agent"); will(returnValue(ua));
        }});
        assertEquals(ua, wrapper.getEffectiveUserAgent());
    }

    @Test
    public void test03_setUserAgent() {
        String ua = randomAlphaNumericString(10);
        wrapper.setUserAgent(ua);
        assertEquals(ua, wrapper.getEffectiveUserAgent());
    }

    @Test
    public void test04_getHeader() {
        final String header1 = uniqueAlphaNumericString(10, "header");
        final String value1 = randomAlphaNumericString(10);
        final String header2 = uniqueAlphaNumericString(10, "header");
        expect(new Expectations() {{
            oneOf (request).getHeader(header1); will(returnValue(value1));
            oneOf (request).getHeader(header2); will(returnValue(null));
        }});
        assertEquals(value1, wrapper.getHeader(header1));
        assertNull(wrapper.getHeader(header2));
    }
}