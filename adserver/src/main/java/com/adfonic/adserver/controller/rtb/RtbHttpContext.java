package com.adfonic.adserver.controller.rtb;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 
 * @author mvanek
 *
 */
public class RtbHttpContext {

    private final RtbEndpoint endpoint;

    private final String publisherExternalId;

    private final HttpServletRequest httpRequest;

    private final HttpServletResponse httpResponse;

    private final String winUrlPath;

    private final long createdAt;

    public RtbHttpContext(RtbEndpoint endpoint, String publisherExternalId, HttpServletRequest httpRequest, HttpServletResponse httpResponse, String winUrlPath) {
        this.createdAt = System.currentTimeMillis();
        this.endpoint = endpoint;
        this.publisherExternalId = publisherExternalId;
        this.httpRequest = httpRequest;
        this.httpResponse = httpResponse;
        this.winUrlPath = winUrlPath;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public RtbEndpoint getEndpoint() {
        return endpoint;
    }

    public String getPublisherExternalId() {
        return publisherExternalId;
    }

    public HttpServletRequest getHttpRequest() {
        return httpRequest;
    }

    public HttpServletResponse getHttpResponse() {
        return httpResponse;
    }

    public String getWinUrlPath() {
        return winUrlPath;
    }

}
