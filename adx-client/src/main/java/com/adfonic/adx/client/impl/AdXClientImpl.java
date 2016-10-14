package com.adfonic.adx.client.impl;

import java.net.URLEncoder;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.util.EntityUtils;

import com.adfonic.adx.client.AdXClient;
import com.adfonic.adx.client.AdXClientException;
import com.adfonic.util.HttpUtils;

public class AdXClientImpl implements AdXClient {
    private static final transient Logger LOG = Logger.getLogger(AdXClientImpl.class.getName());

    private static final String PROVISION_URI = "/prov/adfonic";
    private static final String CLICK_URI = "/click/adfonic";
    
    private static final int DEFAULT_CONNECTION_TIMEOUT = 10;
    private static final int DEFAULT_SO_TIMEOUT         = 15;

    private final String secret;
    private final String baseUri;
    private final int connectionTimeout;
    private final int soTimeout;
    private final HttpClient httpClient;
    
    public AdXClientImpl(ClientConnectionManager clientConnectionManager) {
        this("AdF7125zU", "https://ad-x.co.uk", DEFAULT_CONNECTION_TIMEOUT, DEFAULT_SO_TIMEOUT, new DefaultHttpClient(clientConnectionManager));
    }
 
    public AdXClientImpl(String secret, String baseUri, int connectionTimeout, int soTimeout, ClientConnectionManager clientConnectionManager) {
        this(secret, baseUri, connectionTimeout, soTimeout, new DefaultHttpClient(clientConnectionManager));
    }

    public AdXClientImpl(HttpClient httpClient) {
        this("AdF7125zU", "https://ad-x.co.uk", DEFAULT_CONNECTION_TIMEOUT, DEFAULT_SO_TIMEOUT, httpClient);
    }
    
    public AdXClientImpl(String secret, String baseUri, int connectionTimeout, int soTimeout, HttpClient httpClient) {
        this.secret = secret;
        this.baseUri = baseUri;
        this.connectionTimeout = connectionTimeout;
        this.soTimeout = soTimeout;
        this.httpClient = httpClient;
    }

    @Override
    /** @{inheritDoc} */
    public Outcome provisionCreative(String bundleId, String advertiserExternalId, String creativeExternalId, Platform platform, String destinationUrl) throws AdXClientException {
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Provisioning bundleId=" + bundleId + ", advertiserExternalId=" + advertiserExternalId + ", creativeExternalId=" + creativeExternalId + ", platform=" + platform + ", destinationUrl=" + destinationUrl);
        }
        
        StringBuilder buf = new StringBuilder().append(baseUri).append(PROVISION_URI);
        buf.append('/').append(secret);
        buf.append('/').append(bundleId);
        buf.append('/').append(advertiserExternalId);
        buf.append('/').append(creativeExternalId);
        buf.append('/').append(platform);
        buf.append("?url=");
        try {
            buf.append(URLEncoder.encode(destinationUrl, "utf-8"));
        } catch (java.io.UnsupportedEncodingException e) {
            throw new UnsupportedOperationException(e);
        }
        
        int statusCode = execute(new HttpPost(buf.toString()));
        switch (statusCode) {
        case HttpStatus.SC_CREATED:
            return Outcome.CREATED;
        case HttpStatus.SC_OK:
            return Outcome.UPDATED;
        default:
            throw new AdXClientException("Failed to provision creative: POST " + buf.toString() + " returned " + statusCode);
        }
    }

    @Override
    /** @{inheritDoc} */
    public void trackClick(String advertiserExternalId, String creativeExternalId, String clickExternalId, Map<String,String> deviceIdentifiers) throws AdXClientException {
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Tracking click for advertiserExternalId=" + advertiserExternalId + ", creativeExternalId=" + creativeExternalId + ", clickExternalId=" + clickExternalId + ", deviceIdentifiers=" + deviceIdentifiers);
        }
        
        StringBuilder buf = new StringBuilder().append(baseUri).append(CLICK_URI);
        buf.append('/').append(advertiserExternalId);
        buf.append('/').append(creativeExternalId);
        buf.append('/').append(clickExternalId);
        buf.append('?').append(HttpUtils.encodeParams(deviceIdentifiers));
        
        int statusCode = execute(new HttpGet(buf.toString()));
        if (statusCode != HttpStatus.SC_OK) {
            throw new AdXClientException("Failed to track click: " + buf.toString() + " returned " + statusCode);
        }
    }

    int execute(HttpUriRequest httpRequest) throws AdXClientException {
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Executing " + httpRequest.getRequestLine().getMethod() + " " + httpRequest.getRequestLine().getUri());
        }
        
        // Set the connection and read timeouts
        httpRequest.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, Integer.valueOf(connectionTimeout));
        httpRequest.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, Integer.valueOf(soTimeout));

        String requestLine = httpRequest.getRequestLine().getMethod() + " " + httpRequest.getRequestLine().getUri();
        
        // Execute the request, throwing if the status code isn't copacetic
        HttpEntity httpEntity = null;
        try {
            HttpResponse httpResponse = httpClient.execute(httpRequest);
            httpEntity = httpResponse.getEntity();
            StatusLine statusLine = httpResponse.getStatusLine();

            // Requirement per AO-104 is that we log every URL and HTTP status code
            if (LOG.isLoggable(Level.INFO)) {
                LOG.info(requestLine + " returned " + statusLine.getStatusCode() + " " + statusLine.getReasonPhrase());
            }

            return statusLine.getStatusCode();
        } catch (java.io.IOException e) {
            throw new AdXClientException("Request failed: " + requestLine, e);
        } finally {
            EntityUtils.consumeQuietly(httpEntity);
        }
    }
}