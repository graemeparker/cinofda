package com.adfonic.adserver.offence;

import com.adfonic.adserver.controller.rtb.RtbExecutionContext;

/**
 * @author mvanek
 * 
 * Offence information about failed bid request
 *
 */
public class TroubledBidRequest {

    private final long capturedAt;

    private final String exceptionMessage;

    private final RtbExecutionContext<?, ?> rtbExecutionContext;

    private final OffenceHttpServletRequest httpRequest;

    private final String publisherExtId;

    public TroubledBidRequest(Exception exception, RtbExecutionContext<?, ?> rtbExecutionContext, String publisherExtId) {
        this.capturedAt = rtbExecutionContext.getExecutionStartedAt();
        this.exceptionMessage = exception.getMessage();
        this.rtbExecutionContext = rtbExecutionContext;
        this.httpRequest = new OffenceHttpServletRequest(rtbExecutionContext.getHttpContext().getHttpRequest());
        this.publisherExtId = publisherExtId;
    }

    public long getCapturedAt() {
        return capturedAt;
    }

    public String getExceptionMessage() {
        return exceptionMessage;
    }

    public RtbExecutionContext<?, ?> getExecutionContext() {
        return rtbExecutionContext;
    }

    public OffenceHttpServletRequest getHttpRequest() {
        return httpRequest;
    }

    public String getPublisherExtId() {
        return publisherExtId;
    }

}
