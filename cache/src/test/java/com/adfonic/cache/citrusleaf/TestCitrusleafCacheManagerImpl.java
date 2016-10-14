package com.adfonic.cache.citrusleaf;

import net.citrusleaf.CitrusleafClient;

import org.jmock.Expectations;
import org.junit.Before;
import org.junit.Test;

import com.adfonic.test.AbstractAdfonicTest;

public class TestCitrusleafCacheManagerImpl extends AbstractAdfonicTest {
    private CitrusleafClient client;

    @Before
    public void runBeforeEachTest() {
        client = mock(CitrusleafClient.class, "client");
    }

    @Test
    public void test01_constructor_with_no_timeout() {
        expect(new Expectations() {{
            oneOf (client).isConnected(); will(returnValue(true));
        }});
        new CitrusleafCacheManagerImpl(client, "", 0);
    }

    @Test
    public void test02_constructor_with_timeout_specified() {
        final String namespace = randomAlphaNumericString(10);
        final int connectTimeoutMs = 5000;
        final int operationTimeoutMs = 2000;
        expect(new Expectations() {{
            oneOf (client).isConnected(); will(returnValue(false));
            oneOf (client).isConnected(); will(returnValue(true));
        }});
        new CitrusleafCacheManagerImpl(client, "", 0, namespace, connectTimeoutMs, operationTimeoutMs);
    }
}
