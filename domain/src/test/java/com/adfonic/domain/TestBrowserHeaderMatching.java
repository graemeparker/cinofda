package com.adfonic.domain;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.adfonic.util.HttpRequestContext;

public class TestBrowserHeaderMatching {
    @Test
    @SuppressWarnings("serial")
    public void test_AF_549_and_AF_583() {
        // Normal NON Opera Mini browser
        HttpRequestContext nonOperaMiniContext = new HttpRequestContext() {
                
				private final Map<String,String> headerMap = new HashMap<String,String>() {{
                        put("user-agent", "Mozilla/5.0 (iPhone; U; CPU iPhone OS 3_0 like Mac OS X; en-us) AppleWebKit/528.18 (KHTML, like Gecko) Version/4.0 Mobile/7A341 Safari/528.16");
                    }};
                public String getHeader(String header) {
                    return headerMap.get(header.toLowerCase());
                }
            };

        // Normal Opera Mini browser...the User-Agent does have "Opera/"
        HttpRequestContext operaMiniContext1 = new HttpRequestContext() {
                private final Map<String,String> headerMap = new HashMap<String,String>() {{
                        put("user-agent", "Opera/9.80 (J2ME/MIDP; Opera Mini/6.1.25378/25.692; U; en) Presto/2.5.25 Version/10.54");
                        put("x-operamini-phone-ua", "Blah");
                    }};
                public String getHeader(String header) {
                    return headerMap.get(header.toLowerCase());
                }
            };
                
        // Test for AF-549 and AF-583...the User-Agent doesn't have "Opera/"
        // but there's an "X-OperaMini-Phone-UA" header.
        HttpRequestContext operaMiniContext2 = new HttpRequestContext() {
                private final Map<String,String> headerMap = new HashMap<String,String>() {{
                        put("user-agent", "NokiaN73-1");
                        put("x-operamini-phone-ua", "NokiaN73-1");
                    }};
                public String getHeader(String header) {
                    return headerMap.get(header.toLowerCase());
                }
            };

        // Create a Browser to simulate the "Exclude Opera Mini" browser
        Browser browser = new Browser("Exclude Opera Mini");
        browser.setHeaderRegexp("user-agent", "^(.(?!(Opera(?:(\\s|%20)+Mini)?\\b)))*$");

        assertTrue("Non Opera Mini with just user-agent", browser.isMatch(nonOperaMiniContext));
        assertFalse("Normal Opera Mini with just user-agent", browser.isMatch(operaMiniContext1));
        // At this point the 2nd Opera Mini context will still match...
        assertTrue("Funky Opera Mini with just user-agent", browser.isMatch(operaMiniContext2));

        // But once we add this "header must be absent or empty" constraint,
        // the 2nd Opera Mini context should not match (should be excluded).
        browser.setHeaderRegexp("x-operamini-phone-ua", "^$");
        
        assertTrue("Non Opera Mini with both", browser.isMatch(nonOperaMiniContext));
        assertFalse("Normal Opera Mini with both", browser.isMatch(operaMiniContext1));
        assertFalse("Funky Opera Mini with both", browser.isMatch(operaMiniContext2));
    }
}
