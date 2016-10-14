package com.adfonic.adserver.controller.rtb;

import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.adfonic.adserver.controller.WebConfig;
import com.adfonic.adserver.controller.rtb.OpenRtbV2BidAdapter.ContentStoringReader;
import com.adfonic.adserver.controller.rtb.OpenRtbV2BidAdapter.ContentStoringWriter;
import com.adfonic.adserver.rtb.NoBidException;
import com.adfonic.adserver.rtb.mapper.OpenRTBv1QuickNdirty;
import com.adfonic.adserver.rtb.nativ.ByydRequest;
import com.adfonic.adserver.rtb.nativ.ByydResponse;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;

/**
 * 
 * @author mvanek
 *
 */
public class OpenRtbV1BidAdapter implements ExchangeBidAdapter<com.adfonic.adserver.rtb.open.v1.BidRequest, com.adfonic.adserver.rtb.open.v1.BidResponse> {

    private final OpenRTBv1QuickNdirty bidMapper = OpenRTBv1QuickNdirty.getInstance();

    private static final String ON_EXCETPION_RESPONSE = "{}";//empty response indicates no bid 

    private final ObjectReader objectReader;
    private final ObjectWriter objectWriter;

    public OpenRtbV1BidAdapter() {
        this.objectReader = WebConfig.getRtbJsonMapper().readerFor(com.adfonic.adserver.rtb.open.v1.BidRequest.class);
        this.objectWriter = WebConfig.getRtbJsonMapper().writerFor(com.adfonic.adserver.rtb.open.v1.BidResponse.class);
    }

    @Override
    public com.adfonic.adserver.rtb.open.v1.BidRequest read(HttpServletRequest httpRequest,
            RtbExecutionContext<com.adfonic.adserver.rtb.open.v1.BidRequest, com.adfonic.adserver.rtb.open.v1.BidResponse> context) throws IOException {
        if (context.getSaveRtbMessages()) {
            ContentStoringReader reader = new ContentStoringReader(httpRequest);
            try {
                return objectReader.readValue(reader);
            } finally {
                context.setRtbRequestContent(reader.getContent());
            }
        } else {
            return objectReader.readValue(httpRequest.getReader());
        }
    }

    @Override
    public ByydRequest mapRequest(com.adfonic.adserver.rtb.open.v1.BidRequest rtbRequest,
            RtbExecutionContext<com.adfonic.adserver.rtb.open.v1.BidRequest, com.adfonic.adserver.rtb.open.v1.BidResponse> context) throws IOException, NoBidException {
        return bidMapper.mapRtbRequest(context.getPublisherExternalId(), rtbRequest, null);
    }

    @Override
    public com.adfonic.adserver.rtb.open.v1.BidResponse mapResponse(ByydResponse byydResponse,
            RtbExecutionContext<com.adfonic.adserver.rtb.open.v1.BidRequest, com.adfonic.adserver.rtb.open.v1.BidResponse> context) throws IOException {
        return bidMapper.mapRtbResponse(byydResponse, context.getByydRequest());
    }

    @Override
    public void write(com.adfonic.adserver.rtb.open.v1.BidResponse rtbResponse, OutputStream stream,
            RtbExecutionContext<com.adfonic.adserver.rtb.open.v1.BidRequest, com.adfonic.adserver.rtb.open.v1.BidResponse> context) throws IOException {
        if (context.getSaveRtbMessages()) {
            ContentStoringWriter writer = new ContentStoringWriter(stream);
            try {
                objectWriter.writeValue(writer, rtbResponse);
            } finally {
                context.setRtbResponseContent(writer.getContent());
            }
        } else {
            objectWriter.writeValue(stream, rtbResponse);
        }
    }

    @Override
    public void onNoBidException(NoBidException nobidx, HttpServletRequest httpRequest, HttpServletResponse httpResponse,
            RtbExecutionContext<com.adfonic.adserver.rtb.open.v1.BidRequest, com.adfonic.adserver.rtb.open.v1.BidResponse> context) throws IOException {

        String rtbResponseString = "{\"id\":\"" + nobidx.getByydRequest().getId() + "\",\"nbr\":" + nobidx.getNoBidReason().getV1nbr() + "}";
        httpResponse.getWriter().write(rtbResponseString);
        if (context.getSaveRtbMessages()) {
            context.setRtbResponseContent(rtbResponseString);
        }
    }

    @Override
    public void onBiddingException(Exception exception, HttpServletRequest httpRequest, HttpServletResponse httpResponse,
            RtbExecutionContext<com.adfonic.adserver.rtb.open.v1.BidRequest, com.adfonic.adserver.rtb.open.v1.BidResponse> context) throws IOException {

        httpResponse.getWriter().write(ON_EXCETPION_RESPONSE);
        if (context.getSaveRtbMessages()) {
            context.setRtbResponseContent(ON_EXCETPION_RESPONSE);
        }
    }

}
