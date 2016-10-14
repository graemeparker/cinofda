package com.adfonic.adserver.controller.rtb;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Objects;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.adfonic.adserver.controller.rtb.OpenXBidAdapter.ContentStoringInputStream;
import com.adfonic.adserver.controller.rtb.OpenXBidAdapter.ContentStoringOutputStream;
import com.adfonic.adserver.rtb.NoBidException;
import com.adfonic.adserver.rtb.adx.AdX;
import com.adfonic.adserver.rtb.adx.AdX.BidResponse;
import com.adfonic.adserver.rtb.mapper.AdXMapper;
import com.adfonic.adserver.rtb.nativ.ByydRequest;
import com.adfonic.adserver.rtb.nativ.ByydResponse;

/**
 * 
 * @author mvanek
 *
 */
public class AdXBidAdapter implements ExchangeBidAdapter<AdX.BidRequest, AdX.BidResponse> {

    private final AdXMapper adxMapper;

    public AdXBidAdapter(AdXMapper bidMapper) {
        Objects.requireNonNull(bidMapper);
        this.adxMapper = bidMapper;
    }

    @Override
    public AdX.BidRequest read(HttpServletRequest httpRequest, RtbExecutionContext<AdX.BidRequest, BidResponse> context) throws IOException {
        if (context.getSaveRtbMessages()) {
            ContentStoringInputStream stream = new ContentStoringInputStream(httpRequest);
            try {
                return AdX.BidRequest.parseFrom(stream);
            } finally {
                context.setRtbRequestContent(stream.getContent());
            }
        } else {
            return AdX.BidRequest.parseFrom(httpRequest.getInputStream());
        }
    }

    @Override
    public ByydRequest mapRequest(AdX.BidRequest rtbRequest, RtbExecutionContext<AdX.BidRequest, BidResponse> context) throws IOException, NoBidException {
        return adxMapper.mapRequest(context.getPublisherExternalId(), rtbRequest, null);
    }

    @Override
    public AdX.BidResponse mapResponse(ByydResponse byydResponse, RtbExecutionContext<AdX.BidRequest, BidResponse> context) throws IOException, NoBidException {
        AdX.BidResponse.Builder builder = adxMapper.mapResponse(byydResponse, context);
        setProcessingTimeMs(builder, context.getExecutionStartedAt());
        return builder.build();
    }

    @Override
    public void write(AdX.BidResponse rtbResponse, OutputStream stream, RtbExecutionContext<AdX.BidRequest, BidResponse> context) throws IOException {
        if (context.getSaveRtbMessages()) {
            ContentStoringOutputStream cstream = new ContentStoringOutputStream(stream);
            try {
                rtbResponse.writeTo(cstream);
            } finally {
                context.setRtbResponseContent(cstream.getContent());
            }
        } else {
            rtbResponse.writeTo(stream);
        }
    }

    /**
     * No special behaviour for NoBidException
     */
    @Override
    public void onNoBidException(NoBidException nobidx, HttpServletRequest httpRequest, HttpServletResponse httpResponse, RtbExecutionContext<AdX.BidRequest, BidResponse> context)
            throws IOException {
        onBiddingException(nobidx, httpRequest, httpResponse, context);
    }

    @Override
    public void onBiddingException(Exception exception, HttpServletRequest httpRequest, HttpServletResponse httpResponse, RtbExecutionContext<AdX.BidRequest, BidResponse> context)
            throws IOException {
        AdX.BidResponse.Builder builder = setProcessingTimeMs(AdX.BidResponse.newBuilder(), context.getExecutionStartedAt());
        builder.build().writeTo(httpResponse.getOutputStream());
    }

    private AdX.BidResponse.Builder setProcessingTimeMs(AdX.BidResponse.Builder builder, long startedAt) {
        int processingTime = (int) (System.currentTimeMillis() - startedAt);
        builder.setProcessingTimeMs(processingTime);
        return builder;
    }

}
