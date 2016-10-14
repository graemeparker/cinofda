package com.adfonic.util;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.protocol.HttpContext;

public abstract class AbstractPoolingClientConnectionManager extends AbstractHttpClient{

    protected AbstractPoolingClientConnectionManager(int connTtlMs, int maxTotal, int defaultMaxPerRoute, int connectTimeout, int socketTimeout) {

        super(connTtlMs, maxTotal, defaultMaxPerRoute);
        httpClient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, connectTimeout); // wait no longer than connectTimeout to establish a connection
        httpClient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, socketTimeout); // wait no longer than socketTimeout to finish retrieving a response
    }

    /**
     * Execute an HTTP request without retry support.
     */
    public HttpResponse execute(HttpUriRequest request, HttpContext context) throws java.io.IOException {
        return httpClient.execute(request, context);
    }
}
