package com.adfonic.adx.client.impl;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.jmock.Expectations;
import org.junit.Before;
import org.junit.Test;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.RequestLine;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.HttpParams;

import com.adfonic.adx.client.AdXClient;
import com.adfonic.adx.client.AdXClientException;
import com.adfonic.test.AbstractAdfonicTest;

public class TestAdXClientImpl extends AbstractAdfonicTest {
    private String secret;
    private String baseUri;
    private int connectionTimeout;
    private int soTimeout;
    private HttpClient httpClient;
    private AdXClientImpl impl;

    @Before
    public void runBeforeEachTest() {
        secret = randomAlphaNumericString(10);
        baseUri = randomUrl();
        connectionTimeout = randomInteger();
        soTimeout = randomInteger();
        httpClient = mock(HttpClient.class);
        impl = new AdXClientImpl(secret, baseUri, connectionTimeout, soTimeout, httpClient);
    }

    @Test
    public void test01_provisionCreative_created() throws Exception {
        final String bundleId = randomAlphaNumericString(10);
        final String advertiserExternalId = randomAlphaNumericString(10);
        final String creativeExternalId = randomAlphaNumericString(10);
        final AdXClient.Platform platform = AdXClient.Platform.iOS;
        final String destinationUrl = randomUrl();
        final HttpResponse httpResponse = mock(HttpResponse.class);
        final StatusLine statusLine = mock(StatusLine.class);
        expect(new Expectations() {{
            oneOf (httpClient).execute(with(any(HttpPost.class))); will(returnValue(httpResponse));
            ignoring (httpResponse).getEntity();
            oneOf (httpResponse).getStatusLine(); will(returnValue(statusLine));
            allowing (statusLine).getStatusCode(); will(returnValue(HttpStatus.SC_CREATED));
            allowing (statusLine).getReasonPhrase(); will(returnValue("Created"));
        }});
        assertEquals(AdXClient.Outcome.CREATED, impl.provisionCreative(bundleId, advertiserExternalId, creativeExternalId, platform, destinationUrl));
    }

    @Test
    public void test02_provisionCreative_updated() throws Exception {
        final String bundleId = randomAlphaNumericString(10);
        final String advertiserExternalId = randomAlphaNumericString(10);
        final String creativeExternalId = randomAlphaNumericString(10);
        final AdXClient.Platform platform = AdXClient.Platform.iOS;
        final String destinationUrl = randomUrl();
        final HttpResponse httpResponse = mock(HttpResponse.class);
        final StatusLine statusLine = mock(StatusLine.class);
        expect(new Expectations() {{
            oneOf (httpClient).execute(with(any(HttpPost.class))); will(returnValue(httpResponse));
            ignoring (httpResponse).getEntity();
            oneOf (httpResponse).getStatusLine(); will(returnValue(statusLine));
            allowing (statusLine).getStatusCode(); will(returnValue(HttpStatus.SC_OK));
            allowing (statusLine).getReasonPhrase(); will(returnValue("OK"));
        }});
        assertEquals(AdXClient.Outcome.UPDATED, impl.provisionCreative(bundleId, advertiserExternalId, creativeExternalId, platform, destinationUrl));
    }

    @Test(expected=AdXClientException.class)
    public void test03_provisionCreative_error() throws Exception {
        final String bundleId = randomAlphaNumericString(10);
        final String advertiserExternalId = randomAlphaNumericString(10);
        final String creativeExternalId = randomAlphaNumericString(10);
        final AdXClient.Platform platform = AdXClient.Platform.iOS;
        final String destinationUrl = randomUrl();
        final HttpResponse httpResponse = mock(HttpResponse.class);
        final StatusLine statusLine = mock(StatusLine.class);
        expect(new Expectations() {{
            oneOf (httpClient).execute(with(any(HttpPost.class))); will(returnValue(httpResponse));
            ignoring (httpResponse).getEntity();
            oneOf (httpResponse).getStatusLine(); will(returnValue(statusLine));
            allowing (statusLine).getStatusCode(); will(returnValue(HttpStatus.SC_FORBIDDEN));
            allowing (statusLine).getReasonPhrase(); will(returnValue("Forbidden"));
        }});
        impl.provisionCreative(bundleId, advertiserExternalId, creativeExternalId, platform, destinationUrl);
    }

    @Test
    @SuppressWarnings("serial")
    public void test04_trackClick_valid() throws Exception {
        final String advertiserExternalId = randomAlphaNumericString(10);
        final String creativeExternalId = randomAlphaNumericString(10);
        final String clickExternalId = randomAlphaNumericString(10);
        final Map<String,String> deviceIdentifiers = new LinkedHashMap<String,String>() {{
                put("dpid", randomHexString(40));
                put("odin-1", randomHexString(40));
                put("openudid", randomHexString(40));
                put("hifa", randomHexString(40));
            }};
        final HttpResponse httpResponse = mock(HttpResponse.class);
        final StatusLine statusLine = mock(StatusLine.class);
        expect(new Expectations() {{
            oneOf (httpClient).execute(with(any(HttpGet.class))); will(returnValue(httpResponse));
            ignoring (httpResponse).getEntity();
            oneOf (httpResponse).getStatusLine(); will(returnValue(statusLine));
            allowing (statusLine).getStatusCode(); will(returnValue(HttpStatus.SC_OK));
            allowing (statusLine).getReasonPhrase(); will(returnValue("OK"));
        }});
        impl.trackClick(advertiserExternalId, creativeExternalId, clickExternalId, deviceIdentifiers);
    }

    @Test(expected=AdXClientException.class)
    @SuppressWarnings("serial")
    public void test05_trackClick_error() throws Exception {
        final String advertiserExternalId = randomAlphaNumericString(10);
        final String creativeExternalId = randomAlphaNumericString(10);
        final String clickExternalId = randomAlphaNumericString(10);
        final Map<String,String> deviceIdentifiers = new LinkedHashMap<String,String>() {{
                put("dpid", randomHexString(40));
                put("odin-1", randomHexString(40));
                put("openudid", randomHexString(40));
                put("hifa", randomHexString(40));
            }};
        final HttpResponse httpResponse = mock(HttpResponse.class);
        final StatusLine statusLine = mock(StatusLine.class);
        expect(new Expectations() {{
            oneOf (httpClient).execute(with(any(HttpGet.class))); will(returnValue(httpResponse));
            ignoring (httpResponse).getEntity();
            oneOf (httpResponse).getStatusLine(); will(returnValue(statusLine));
            allowing (statusLine).getStatusCode(); will(returnValue(HttpStatus.SC_INTERNAL_SERVER_ERROR));
            allowing (statusLine).getReasonPhrase(); will(returnValue("Internal Server Error"));
        }});
        impl.trackClick(advertiserExternalId, creativeExternalId, clickExternalId, deviceIdentifiers);
    }

    @Test(expected=AdXClientException.class)
    public void test06_execute_IOException() throws Exception {
        final RequestLine requestLine = mock(RequestLine.class);
        final String uri = randomUrl();
        final HttpUriRequest httpRequest = mock(HttpUriRequest.class);
        final HttpParams httpParams = mock(HttpParams.class);
        expect(new Expectations() {{
            allowing (httpRequest).getRequestLine(); will(returnValue(requestLine));
            ignoring (requestLine);
            allowing (httpRequest).getURI(); will(returnValue(uri));
            allowing (httpRequest).getParams(); will(returnValue(httpParams));
            oneOf (httpParams).setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, Integer.valueOf(connectionTimeout));
            oneOf (httpParams).setParameter(CoreConnectionPNames.SO_TIMEOUT, Integer.valueOf(soTimeout));
            oneOf (httpClient).execute(httpRequest); will(throwException(new IOException("bummer dude")));
        }});
        impl.execute(httpRequest);
    }

    @Test
    public void test07_execute_normal() throws Exception {
        final RequestLine requestLine = mock(RequestLine.class);
        final String uri = randomUrl();
        final HttpUriRequest httpRequest = mock(HttpUriRequest.class);
        final HttpParams httpParams = mock(HttpParams.class);
        final HttpResponse httpResponse = mock(HttpResponse.class);
        final StatusLine statusLine = mock(StatusLine.class);
        expect(new Expectations() {{
            allowing (httpRequest).getRequestLine(); will(returnValue(requestLine));
            ignoring (requestLine);
            allowing (httpRequest).getURI(); will(returnValue(uri));
            allowing (httpRequest).getParams(); will(returnValue(httpParams));
            oneOf (httpParams).setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, Integer.valueOf(connectionTimeout));
            oneOf (httpParams).setParameter(CoreConnectionPNames.SO_TIMEOUT, Integer.valueOf(soTimeout));
            oneOf (httpClient).execute(httpRequest); will(returnValue(httpResponse));
            ignoring (httpResponse).getEntity();
            oneOf (httpResponse).getStatusLine(); will(returnValue(statusLine));
            allowing (statusLine).getStatusCode(); will(returnValue(HttpStatus.SC_OK));
            allowing (statusLine).getReasonPhrase(); will(returnValue("OK"));
        }});
        impl.execute(httpRequest);
    }
}