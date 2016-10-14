package com.adfonic.adserver.controller.rtb;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import com.adfonic.adserver.AdSrvCounter;
import com.adfonic.adserver.rtb.NoBidException;
import com.adfonic.adserver.rtb.NoBidReason;
import com.adfonic.adserver.rtb.adx.AdX;
import com.adfonic.adserver.rtb.mapper.mobfox.MobFoxBidRequest;
import com.adfonic.adserver.rtb.nativ.ByydRequest;
import com.adfonic.adserver.rtb.open.v2.ext.appnxs.AppNexusBidRequest;
import com.adfonic.adserver.rtb.open.v2.ext.mopub.MopubBidRequest;
import com.adfonic.adserver.rtb.open.v2.ext.nexage.NexageBidRequest;
import com.adfonic.adserver.rtb.open.v2.ext.pubmatic.PubmaticBidRequest;
import com.adfonic.adserver.rtb.openx.OpenX;
import com.adfonic.adserver.rtb.rubicon.RubiconBidRequest;
import com.adfonic.adserver.rtb.smaato.SmaatoBidRequest;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.google.protobuf.InvalidProtocolBufferException;

/**
 * 
 * @author mvanek
 *
 * Kind of duplicates metadata in Bid Controller for testing and verification purposes
 */
public enum RtbEndpoint {

    ORTBv1(RtbProtocol.OpenRtbV1, "/rtb/bid/{publisherExternalID}", OpenRtbV1Controller.class, com.adfonic.adserver.rtb.open.v1.BidRequest.class), // 
    ORTBv2(RtbProtocol.OpenRtbV2, "/rtb/v2/bid/{publisherExternalID}", OpenRTBv2Controller.class, com.adfonic.adserver.rtb.open.v2.BidRequest.class), // 
    OpenX(RtbProtocol.OpenX, "/rtb/openx/bid/{publisherExternalID}", OpenXController.class, OpenX.BidRequest.class), //
    DcAdX(RtbProtocol.DcAdX, "/rtb/dcadx/bid/{publisherExternalID}", AdXController.class, AdX.BidRequest.class), //
    AppNexusV2(RtbProtocol.OpenRtbV2, "/rtb/appnexus/v2/bid/{publisherExternalID}", AppNexusV2Controller.class, AppNexusBidRequest.class), //
    SmaatoV2(RtbProtocol.OpenRtbV2, "/rtb/smaato/bid/{publisherExternalID}", SmaatoBidController.class, SmaatoBidRequest.class), // 
    MobclixV1(RtbProtocol.OpenRtbV1, "/rtb/bid/{publisherExternalID}", OpenRtbV1Controller.class, com.adfonic.adserver.rtb.open.v1.BidRequest.class), //
    MopubV2(RtbProtocol.OpenRtbV2, "/rtb/mopub/bid/{publisherExternalID}", "/rtb/mopub/notify", MopubV2BidController.class, MopubBidRequest.class), //
    NexageV2(RtbProtocol.OpenRtbV2, "/rtb/nexage/v2/bid/{publisherExternalID}", NexageV2Controller.class, NexageBidRequest.class), // 
    Omax(RtbProtocol.OpenRtbV2, "/rtb/omax/bid/{publisherExternalID}", OmaxRtbController.class, com.adfonic.adserver.rtb.open.v2.BidRequest.class), //
    MobFox(RtbProtocol.OpenRtbV2, "/rtb/mobfox/bid/{publisherExternalID}", MobFoxRtbController.class, MobFoxBidRequest.class), //
    PubmaticV2(RtbProtocol.OpenRtbV2, "/rtb/pubmatic/bid/{publisherExternalID}", PubmaticRTBv2Controller.class, PubmaticBidRequest.class), //
    RubiconV2(RtbProtocol.OpenRtbV2, "/rtb/rubicon/bid/{publisherExternalID}", RubiconRTBv2Controller.class, RubiconBidRequest.class), //
    Admeld(RtbProtocol.CustomGet, "/rtb/adm/bid/{publisherExternalID}", AdmeldRtbController.class, HttpServletRequest.class), //
    YieldLab(RtbProtocol.CustomGet, "/rtb/yieldlab/bid/{publisherExternalID}", YieldlabController.class, HttpServletRequest.class), //

    ByydTest(RtbProtocol.OpenRtbV2, "/rtb/test/bid/{publisherExternalID}", ByydTestBidController.class, MopubBidRequest.class);

    private final RtbProtocol protocol;

    private final String bidUrlContext;

    private final String lossUrlContext;

    private final Class<?> controllerClass;

    private final Class<?> bidRequestClass;

    private RtbEndpoint(RtbProtocol protocol, String bidUrlContext, String lossUrlContext, Class<?> controllerClass, Class<?> bidRequestClass) {
        this.protocol = protocol;
        this.bidUrlContext = bidUrlContext;
        this.lossUrlContext = lossUrlContext;
        this.controllerClass = controllerClass;
        this.bidRequestClass = bidRequestClass;
    }

    private RtbEndpoint(RtbProtocol protocol, String bidUrlContext, Class<?> controllerClass, Class<?> bidRequestClass) {
        this(protocol, bidUrlContext, null, controllerClass, bidRequestClass);
    }

    public RtbProtocol getProtocol() {
        return protocol;
    }

    public Class<?> getBidRequestClass() {
        return bidRequestClass;
    }

    public Class<?> getControllerClass() {
        return controllerClass;
    }

    public String getBidUrlContext() {
        return bidUrlContext;
    }

    public String getLossUrlContext() {
        return lossUrlContext;
    }

    public static enum RtbProtocol {

        OpenRtbV1(HttpMethod.POST, MediaType.APPLICATION_JSON, HttpStatus.OK, new JsonMappingException("No content to map due to end-of-input")), // 
        OpenRtbV2(HttpMethod.POST, MediaType.APPLICATION_JSON, HttpStatus.NO_CONTENT, new JsonMappingException("No content to map due to end-of-input")), // 
        OpenX(HttpMethod.POST, MediaType.APPLICATION_OCTET_STREAM, HttpStatus.OK, new NoBidException(new ByydRequest("c5373546-5d54-41c0-9707-0fe49fdf5863", ""),
                NoBidReason.REQUEST_INVALID, AdSrvCounter.MISS_UA)), //
        DcAdX(HttpMethod.POST, MediaType.APPLICATION_OCTET_STREAM, HttpStatus.OK, new InvalidProtocolBufferException("Message missing required fields: id")), // 
        CustomGet(HttpMethod.GET, MediaType.ALL, MediaType.APPLICATION_JSON, HttpStatus.OK, new NoBidException(new ByydRequest("p-id", "r-id"), NoBidReason.REQUEST_INVALID,
                AdSrvCounter.MISS_UA));

        private final HttpMethod requestMethod;

        private final MediaType requestMediaType;

        private final MediaType responseMediaType;

        private final HttpStatus responseNobidStatus;

        private final Exception brokenInputException;

        private RtbProtocol(HttpMethod requestMethod, MediaType requestMediaType, HttpStatus responseNobidStatus, Exception brokenInputException) {
            this(requestMethod, requestMediaType, requestMediaType, responseNobidStatus, brokenInputException);
        }

        private RtbProtocol(HttpMethod requestMethod, MediaType requestMediaType, MediaType responseMediaType, HttpStatus responseNobidStatus, Exception brokenInputException) {
            this.requestMethod = requestMethod;
            this.requestMediaType = requestMediaType;
            this.responseMediaType = responseMediaType;
            this.responseNobidStatus = responseNobidStatus;
            this.brokenInputException = brokenInputException;
        }

        public MediaType getRequestMediaType() {
            return requestMediaType;
        }

        public HttpMethod getRequestMethod() {
            return requestMethod;
        }

        public MediaType getResponseMediaType() {
            return responseMediaType;
        }

        public HttpStatus getResponseNobidStatus() {
            return responseNobidStatus;
        }

        public Exception getBrokenInputException() {
            return brokenInputException;
        }
    }

}
