package com.adfonic.ddr;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.HashMap;
import java.util.Map;

import org.jmock.Expectations;
import org.junit.Test;

import com.adfonic.test.AbstractAdfonicTest;

@SuppressWarnings("serial")
public class TestAbstractDdrService extends AbstractAdfonicTest {
    private static final class TestImpl extends AbstractDdrService {
        private final Map<String,Map<String,String>> propertiesByUserAgent;
        
        TestImpl(Map<String,Map<String,String>> propertiesByUserAgent) {
            this.propertiesByUserAgent = propertiesByUserAgent;
        }

        @Override
        protected Map<String,String> doGetDdrProperties(String userAgent) {
            return propertiesByUserAgent.get(userAgent);
        }
    }
    
    @Test
    public void test01_getDdrProperties_null() {
        final String ua = randomAlphaNumericString(10);
        final Map<String,String> props = null;
        final Map<String,Map<String,String>> propertiesByUserAgent = new HashMap<String,Map<String,String>>() {{
                put(ua, props);
            }};
        assertNull(new TestImpl(propertiesByUserAgent).getDdrProperties(ua));
    }

    @Test
    public void test02_getDdrProperties_empty() {
        final String ua = randomAlphaNumericString(10);
        final Map<String,String> props = new HashMap<String,String>();
        final Map<String,Map<String,String>> propertiesByUserAgent = new HashMap<String,Map<String,String>>() {{
                put(ua, props);
            }};
        assertNull(new TestImpl(propertiesByUserAgent).getDdrProperties(ua));
    }

    @Test
    public void test03_getDdrProperties_non_empty() {
        final String ua = randomAlphaNumericString(10);
        final Map<String,String> props = new HashMap<String,String>() {{
                put("id", randomAlphaNumericString(10));
            }};
        final Map<String,Map<String,String>> propertiesByUserAgent = new HashMap<String,Map<String,String>>() {{
                put(ua, props);
            }};
        assertEquals(props, new TestImpl(propertiesByUserAgent).getDdrProperties(ua));
    }

    @Test
    public void test04_getDdrProperties_operaMini() {
        final String ua = randomAlphaNumericString(10);
        final Map<String,String> props = new HashMap<String,String>() {{
                put("id", randomAlphaNumericString(10));
            }};
        final Map<String,Map<String,String>> propertiesByUserAgent = new HashMap<String,Map<String,String>>() {{
                put(ua, props);
            }};
        final UserAgentAware context = mock(UserAgentAware.class, "context");
        expect(new Expectations() {{
            oneOf (context).getHeader("X-OperaMini-Phone-UA"); will(returnValue(ua));
            oneOf (context).setUserAgent(ua);
        }});
        assertEquals(props, new TestImpl(propertiesByUserAgent).getDdrProperties(context));
    }

    @Test
    public void test05_getDdrProperties_googleWirelessTranscoder() {
        final String ua = randomAlphaNumericString(10);
        final Map<String,String> props = new HashMap<String,String>() {{
                put("id", randomAlphaNumericString(10));
            }};
        final Map<String,Map<String,String>> propertiesByUserAgent = new HashMap<String,Map<String,String>>() {{
                put(ua, props);
            }};
        final UserAgentAware context = mock(UserAgentAware.class, "context");
        expect(new Expectations() {{
            oneOf (context).getHeader("X-OperaMini-Phone-UA"); will(returnValue(null));
            oneOf (context).getHeader("X-Original-User-Agent"); will(returnValue(ua));
            oneOf (context).setUserAgent(ua);
        }});
        assertEquals(props, new TestImpl(propertiesByUserAgent).getDdrProperties(context));
    }

    @Test
    public void test06_getDdrProperties_deviceUserAgent() {
        final String ua = randomAlphaNumericString(10);
        final Map<String,String> props = new HashMap<String,String>() {{
                put("id", randomAlphaNumericString(10));
            }};
        final Map<String,Map<String,String>> propertiesByUserAgent = new HashMap<String,Map<String,String>>() {{
                put(ua, props);
            }};
        final UserAgentAware context = mock(UserAgentAware.class, "context");
        expect(new Expectations() {{
            oneOf (context).getHeader("X-OperaMini-Phone-UA"); will(returnValue(null));
            oneOf (context).getHeader("X-Original-User-Agent"); will(returnValue(null));
            oneOf (context).getHeader("X-Device-User-Agent"); will(returnValue(ua));
            oneOf (context).setUserAgent(ua);
        }});
        assertEquals(props, new TestImpl(propertiesByUserAgent).getDdrProperties(context));
    }

    @Test
    public void test07_getDdrProperties_no_UserAgent_in_context() {
        final String ua = randomAlphaNumericString(10);
        final Map<String,Map<String,String>> propertiesByUserAgent = new HashMap<String,Map<String,String>>();
        final UserAgentAware context = mock(UserAgentAware.class, "context");
        expect(new Expectations() {{
            oneOf (context).getHeader("X-OperaMini-Phone-UA"); will(returnValue(ua));
            oneOf (context).getHeader("X-Original-User-Agent"); will(returnValue(ua));
            oneOf (context).getHeader("X-Device-User-Agent"); will(returnValue(ua));
            oneOf (context).getEffectiveUserAgent(); will(returnValue(null));
        }});
        assertNull(new TestImpl(propertiesByUserAgent).getDdrProperties(context));
    }

    @Test
    public void test08_getDdrProperties_no_props_for_userAgent() {
        final String ua = randomAlphaNumericString(10);
        final Map<String,Map<String,String>> propertiesByUserAgent = new HashMap<String,Map<String,String>>();
        final UserAgentAware context = mock(UserAgentAware.class, "context");
        expect(new Expectations() {{
            allowing (context).getHeader(with(any(String.class))); will(returnValue(null));
            oneOf (context).getEffectiveUserAgent(); will(returnValue(ua));
        }});
        assertNull(new TestImpl(propertiesByUserAgent).getDdrProperties(context));
    }

    @Test
    public void test09_getDdrProperties_userAgent_good() {
        final String ua = randomAlphaNumericString(10);
        final Map<String,String> props = new HashMap<String,String>() {{
                put("id", randomAlphaNumericString(10));
            }};
        final Map<String,Map<String,String>> propertiesByUserAgent = new HashMap<String,Map<String,String>>() {{
                put(ua, props);
            }};
        final UserAgentAware context = mock(UserAgentAware.class, "context");
        expect(new Expectations() {{
            allowing (context).getHeader(with(any(String.class))); will(returnValue(null));
            oneOf (context).getEffectiveUserAgent(); will(returnValue(ua));
        }});
        assertEquals(props, new TestImpl(propertiesByUserAgent).getDdrProperties(context));
    }

    @Test
    public void test10_checkForOperaMini_no_header() {
        final Map<String,Map<String,String>> propertiesByUserAgent = new HashMap<String,Map<String,String>>();
        final HttpHeaderAware context = mock(HttpHeaderAware.class, "context");
        expect(new Expectations() {{
            oneOf (context).getHeader("X-OperaMini-Phone-UA"); will(returnValue(null));
        }});
        assertNull(new TestImpl(propertiesByUserAgent).checkForOperaMini(context));
    }
    
    @Test
    public void test11_checkForOperaMini_not_found() {
        final String ua = randomAlphaNumericString(10);
        final Map<String,Map<String,String>> propertiesByUserAgent = new HashMap<String,Map<String,String>>();
        final HttpHeaderAware context = mock(HttpHeaderAware.class, "context");
        expect(new Expectations() {{
            oneOf (context).getHeader("X-OperaMini-Phone-UA"); will(returnValue(ua));
        }});
        assertNull(new TestImpl(propertiesByUserAgent).checkForOperaMini(context));
    }
    
    @Test
    public void test12_checkForOperaMini_found() {
        final String ua = randomAlphaNumericString(10);
        final Map<String,String> props = new HashMap<String,String>() {{
                put("id", randomAlphaNumericString(10));
            }};
        final Map<String,Map<String,String>> propertiesByUserAgent = new HashMap<String,Map<String,String>>() {{
                put(ua, props);
            }};
        final HttpHeaderAware context = mock(HttpHeaderAware.class, "context");
        expect(new Expectations() {{
            oneOf (context).getHeader("X-OperaMini-Phone-UA"); will(returnValue(ua));
        }});
        assertEquals(props, new TestImpl(propertiesByUserAgent).checkForOperaMini(context));
    }

    @Test
    public void test11_checkForGoogleWirelessTranscoder_no_header() {
        final Map<String,Map<String,String>> propertiesByUserAgent = new HashMap<String,Map<String,String>>();
        final HttpHeaderAware context = mock(HttpHeaderAware.class, "context");
        expect(new Expectations() {{
            oneOf (context).getHeader("X-Original-User-Agent"); will(returnValue(null));
        }});
        assertNull(new TestImpl(propertiesByUserAgent).checkForGoogleWirelessTranscoder(context));
    }
    
    @Test
    public void test12_checkForGoogleWirelessTranscoder_not_found() {
        final String ua = randomAlphaNumericString(10);
        final Map<String,Map<String,String>> propertiesByUserAgent = new HashMap<String,Map<String,String>>();
        final HttpHeaderAware context = mock(HttpHeaderAware.class, "context");
        expect(new Expectations() {{
            oneOf (context).getHeader("X-Original-User-Agent"); will(returnValue(ua));
        }});
        assertNull(new TestImpl(propertiesByUserAgent).checkForGoogleWirelessTranscoder(context));
    }
    
    @Test
    public void test13_checkForGoogleWirelessTranscoder_found() {
        final String ua = randomAlphaNumericString(10);
        final Map<String,String> props = new HashMap<String,String>() {{
                put("id", randomAlphaNumericString(10));
            }};
        final Map<String,Map<String,String>> propertiesByUserAgent = new HashMap<String,Map<String,String>>() {{
                put(ua, props);
            }};
        final HttpHeaderAware context = mock(HttpHeaderAware.class, "context");
        expect(new Expectations() {{
            oneOf (context).getHeader("X-Original-User-Agent"); will(returnValue(ua));
        }});
        assertEquals(props, new TestImpl(propertiesByUserAgent).checkForGoogleWirelessTranscoder(context));
    }

    @Test
    public void test14_checkForDeviceUserAgent_no_header() {
        final Map<String,Map<String,String>> propertiesByUserAgent = new HashMap<String,Map<String,String>>();
        final HttpHeaderAware context = mock(HttpHeaderAware.class, "context");
        expect(new Expectations() {{
            oneOf (context).getHeader("X-Device-User-Agent"); will(returnValue(null));
        }});
        assertNull(new TestImpl(propertiesByUserAgent).checkForDeviceUserAgent(context));
    }
    
    @Test
    public void test15_checkForDeviceUserAgent_not_found() {
        final String ua = randomAlphaNumericString(10);
        final Map<String,Map<String,String>> propertiesByUserAgent = new HashMap<String,Map<String,String>>();
        final HttpHeaderAware context = mock(HttpHeaderAware.class, "context");
        expect(new Expectations() {{
            oneOf (context).getHeader("X-Device-User-Agent"); will(returnValue(ua));
        }});
        assertNull(new TestImpl(propertiesByUserAgent).checkForDeviceUserAgent(context));
    }
    
    @Test
    public void test16_checkForDeviceUserAgent_found() {
        final String ua = randomAlphaNumericString(10);
        final Map<String,String> props = new HashMap<String,String>() {{
                put("id", randomAlphaNumericString(10));
            }};
        final Map<String,Map<String,String>> propertiesByUserAgent = new HashMap<String,Map<String,String>>() {{
                put(ua, props);
            }};
        final HttpHeaderAware context = mock(HttpHeaderAware.class, "context");
        expect(new Expectations() {{
            oneOf (context).getHeader("X-Device-User-Agent"); will(returnValue(ua));
        }});
        assertEquals(props, new TestImpl(propertiesByUserAgent).checkForDeviceUserAgent(context));
    }

    @Test
    public void test17_getUserAgentFromContext_UserAgentAware() {
        final String ua = randomAlphaNumericString(10);
        final UserAgentAware context = mock(UserAgentAware.class, "context");
        expect(new Expectations() {{
            oneOf (context).getEffectiveUserAgent(); will(returnValue(ua));
        }});
        assertEquals(ua, AbstractDdrService.getUserAgentFromContext(context));
    }

    @Test
    public void test18_getUserAgentFromContext_non_UserAgentAware() {
        final String ua = randomAlphaNumericString(10);
        final HttpHeaderAware context = mock(HttpHeaderAware.class, "context");
        expect(new Expectations() {{
            oneOf (context).getHeader("User-Agent"); will(returnValue(ua));
        }});
        assertEquals(ua, AbstractDdrService.getUserAgentFromContext(context));
    }

    @Test
    public void test19_setUserAgentInContext_UserAgentAware() {
        final String ua = randomAlphaNumericString(10);
        final UserAgentAware context = mock(UserAgentAware.class, "context");
        expect(new Expectations() {{
            oneOf (context).setUserAgent(ua);
        }});
        AbstractDdrService.setUserAgentInContext(ua, context);
    }

    @Test
    public void test20_setUserAgentInContext_non_UserAgentAware() {
        final String ua = randomAlphaNumericString(10);
        final HttpHeaderAware context = mock(HttpHeaderAware.class, "context");
        expect(new Expectations() {{
            // nothing
        }});
        AbstractDdrService.setUserAgentInContext(ua, context);
    }
}