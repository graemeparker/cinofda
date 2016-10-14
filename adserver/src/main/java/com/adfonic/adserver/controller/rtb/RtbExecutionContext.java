package com.adfonic.adserver.controller.rtb;

import java.util.Objects;

import com.adfonic.adserver.rtb.adx.AdX;
import com.adfonic.adserver.rtb.nativ.ByydRequest;
import com.adfonic.adserver.rtb.nativ.ByydResponse;
import com.adfonic.adserver.rtb.openx.OpenX;
import com.adfonic.domain.cache.dto.adserver.adspace.AdSpaceDto;
import com.adfonic.domain.cache.dto.adserver.creative.CreativeDto;
import com.google.protobuf.MessageOrBuilder;
import com.google.protobuf.TextFormat;

/**
 * 
 * @author mvanek
 *
 */
public class RtbExecutionContext<I, O> {

    public static final String RTB_CONTEXT = "RTB_CONTEXT";

    private final RtbHttpContext httpContext;

    private final boolean saveRtbMessages;

    //original String or byte[] 
    private Object rtbRequestContent;

    //Same as rtbRequestContent for JSON or TextFormat for Protobuf 
    private String rtbRequestString;

    private I rtbRequest;

    private Long rtbRequestParsedAt;

    private ByydRequest byydRequest;

    private Long byydRequestMappedAt;

    private ByydResponse byydResponse;

    private Long byydResponseCreatedAt;

    private O rtbResponse;

    private Object rtbResponseContent;

    private String rtbResponseString;

    private Long rtbResponseMappedAt;

    private Long rtbResponseWrittenAt;

    private Long executionCompletedAt;

    private Exception exception;

    private AdSpaceDto adSpace;

    private CreativeDto creative;

    private Long targetingStartedAt;

    private Long targetingCompletedAt;

    public RtbExecutionContext(RtbHttpContext httpContext, boolean saveRtbMessages) {
        Objects.requireNonNull(httpContext);
        this.httpContext = httpContext;
        this.saveRtbMessages = saveRtbMessages;
    }

    public RtbHttpContext getHttpContext() {
        return httpContext;
    }

    public String getPublisherExternalId() {
        return httpContext.getPublisherExternalId();
    }

    public I getRtbRequest() {
        return rtbRequest;
    }

    public void setRtbRequest(I rtbRequest) {
        Objects.requireNonNull(rtbRequest);
        if (this.rtbRequest != null) {
            throw new IllegalStateException("RtbRequest already set " + this.rtbRequest);
        }
        this.rtbRequest = rtbRequest;
    }

    public boolean getSaveRtbMessages() {
        return saveRtbMessages;
    }

    /**
     * Consider extending setRtbRequest(I rtbRequest) instead of this separate method
     */
    public void setRtbRequestContent(String rtbRequestContent) {
        this.rtbRequestContent = rtbRequestContent;
        this.rtbRequestString = rtbRequestContent;
    }

    public void setRtbRequestContent(byte[] rtbRequestContent) {
        this.rtbRequestContent = rtbRequestContent;
    }

    public Object getRtbRequestContent() {
        return rtbRequestContent;
    }

    public O getRtbResponse() {
        return rtbResponse;
    }

    public void setRtbResponse(O rtbResponse) {
        Objects.requireNonNull(rtbResponse);
        if (this.rtbResponse != null) {
            throw new IllegalStateException("RtbResponse already set " + this.rtbResponse);
        }
        this.rtbResponse = rtbResponse;
    }

    public void setRtbResponseContent(String content) {
        this.rtbResponseContent = content;
    }

    public void setRtbResponseContent(byte[] content) {
        this.rtbResponseContent = content;
    }

    public Object getRtbResponseContent() {
        return this.rtbResponseContent;
    }

    public ByydRequest getByydRequest() {
        return byydRequest;
    }

    public void setByydRequest(ByydRequest byydRequest) {
        Objects.requireNonNull(byydRequest);
        if (this.byydRequest != null) {
            throw new IllegalStateException("ByydRequest already set " + this.byydRequest);
        }
        this.byydRequest = byydRequest;
    }

    public ByydResponse getByydResponse() {
        return byydResponse;
    }

    public void setByydResponse(ByydResponse byydResponse) {
        Objects.requireNonNull(byydResponse);
        if (this.byydResponse != null) {
            throw new IllegalStateException("ByydResponse already set " + this.byydResponse);
        }
        this.byydResponse = byydResponse;
    }

    public long getExecutionStartedAt() {
        return httpContext.getCreatedAt();
    }

    public Long getExecutionCompletedAt() {
        return executionCompletedAt;
    }

    public void setTargetingStarted(AdSpaceDto adSpace) {
        this.adSpace = adSpace;
        this.targetingStartedAt = System.currentTimeMillis();
    }

    public void setTargetingCompleted(CreativeDto creative) {
        this.creative = creative;
        this.targetingCompletedAt = System.currentTimeMillis();
    }

    void setExcutionCompletedAt(long timestamp) {
        this.executionCompletedAt = timestamp;
    }

    void setRtbRequestParsedAt(long timestamp) {
        this.rtbRequestParsedAt = timestamp;
    }

    void setByydRequestMappedAt(long timestamp) {
        this.byydRequestMappedAt = timestamp;
    }

    void setByydResponseCreatedAt(long timestamp) {
        this.byydResponseCreatedAt = timestamp;
    }

    void setRtbResponseMappedAt(long timestamp) {
        this.rtbResponseMappedAt = timestamp;
    }

    void setRtbResponseWrittenAt(long timestamp) {
        this.rtbResponseWrittenAt = timestamp;
    }

    /**
     * JSON->Rtb request reading/parsing completed 
     */
    public Long getRtbRequestParsedAt() {
        return rtbRequestParsedAt;
    }

    /**
     * Rtb->Byyd request mapping completed 
     */
    public Long getByydRequestMappedAt() {
        return byydRequestMappedAt;
    }

    public Long getTargetingStartedAt() {
        return targetingStartedAt;
    }

    public Long getTargetingCompletedAt() {
        return targetingCompletedAt;
    }

    /**
     * Byyd bidding completed
     */
    public Long getByydResponseCreatedAt() {
        return byydResponseCreatedAt;
    }

    /**
     * Rtb response mapping/marshalling completed
     */
    public Long getRtbResponseMappedAt() {
        return rtbResponseMappedAt;
    }

    /**
     * Rtb response writing completed
     */
    public Long getRtbResponseWrittenAt() {
        return rtbResponseWrittenAt;
    }

    public void setException(Exception exception) {
        this.exception = exception;
    }

    public Exception getException() {
        return exception;
    }

    public CreativeDto getCreative() {
        return creative;
    }

    public AdSpaceDto getAdSpace() {
        return adSpace;
    }

    /**
     * Beware. Can return null 
     */
    public String getRtbRequestString() {
        if (rtbRequestString != null) {
            return rtbRequestString;
        } else if (rtbRequestContent instanceof byte[]) {
            if (rtbRequest instanceof AdX.BidRequest || rtbRequest instanceof OpenX.BidRequest) {
                MessageOrBuilder protoBean = (MessageOrBuilder) rtbRequest;
                if (protoBean != null) {
                    rtbRequestString = TextFormat.printToUnicodeString(protoBean);
                }
            }
        } else if (rtbRequestContent instanceof String) {
            rtbRequestString = (String) rtbRequestContent;//this should not happen
        }
        return rtbRequestString;
    }

    /**
     * Beware. Can return null 
     */
    public String getRtbResponseString() {
        if (rtbResponseString != null) {
            return rtbResponseString;
        } else if (rtbResponseContent instanceof byte[]) {
            if (rtbResponse instanceof AdX.BidResponse || rtbResponse instanceof OpenX.BidResponse) {
                MessageOrBuilder protoBean = (MessageOrBuilder) rtbResponse;
                if (protoBean != null) {
                    rtbResponseString = TextFormat.printToUnicodeString(protoBean);
                }
            }
        } else if (rtbResponseContent instanceof String) {
            rtbResponseString = (String) rtbResponseContent;
        }
        return rtbResponseString;
    }

}
