package com.adfonic.util;

import org.apache.http.client.HttpRequestRetryHandler;

public abstract class AbstractThreadSafeHttpClient extends AbstractHttpClient{

    private final String baseUrl;

    protected AbstractThreadSafeHttpClient(String baseUrl, int connTtlMs, int maxTotal, int defaultMaxPerRoute, HttpRequestRetryHandler requestRetryHandler) {
        super(connTtlMs, maxTotal, defaultMaxPerRoute);
        this.baseUrl = baseUrl;

        if (requestRetryHandler != null) {
            httpClient.setHttpRequestRetryHandler(requestRetryHandler);
        }
    }

    public String getBaseUrl() {
        return baseUrl;
    }
}
