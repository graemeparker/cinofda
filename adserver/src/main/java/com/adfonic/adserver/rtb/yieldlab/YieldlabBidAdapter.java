package com.adfonic.adserver.rtb.yieldlab;

import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.net.URLEncoder;
import java.util.Enumeration;
import java.util.Objects;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.adfonic.adserver.controller.rtb.ExchangeBidAdapter;
import com.adfonic.adserver.controller.rtb.OpenRtbV2BidAdapter;
import com.adfonic.adserver.controller.rtb.RtbExecutionContext;
import com.adfonic.adserver.controller.rtb.OpenRtbV2BidAdapter.ContentStoringWriter;
import com.adfonic.adserver.rtb.NoBidException;
import com.adfonic.adserver.rtb.nativ.ByydRequest;
import com.adfonic.adserver.rtb.nativ.ByydResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

/**
 * 
 * @author mvanek
 *
 */
public class YieldlabBidAdapter implements ExchangeBidAdapter<HttpServletRequest, YieldlabBidResponse> {

    private final YieldLabMapper bidMapper;

    private final ObjectWriter responseWriter;

    // Our nobid response is actually not according Yieldlab spec (but nobody complains so keep it)
    // In the case where a DSP does not want to make a bid offer, the following Bid Response should be sent:
    // { "bid": { "cpm": 0, "tid": "730d97d0-dbca-42c4-a54f-6a7ef08d80e2" } }

    public YieldlabBidAdapter(ObjectMapper rtbJsonMapper, YieldLabMapper bidMapper) {
        Objects.requireNonNull(bidMapper);
        this.bidMapper = bidMapper;

        Objects.requireNonNull(rtbJsonMapper);
        this.responseWriter = rtbJsonMapper.writerFor(YieldlabBidResponse.class);
    }

    @Override
    public HttpServletRequest read(HttpServletRequest httpRequest, RtbExecutionContext<HttpServletRequest, YieldlabBidResponse> context) throws IOException {
        if (context.getSaveRtbMessages()) {
            Enumeration<String> parameterNames = httpRequest.getParameterNames();
            StringBuilder sb = new StringBuilder();
            while (parameterNames.hasMoreElements()) {
                String paramName = parameterNames.nextElement();
                String paramValue = httpRequest.getParameter(paramName);
                sb.append(URLEncoder.encode(paramName, "utf-8")).append('=').append(URLEncoder.encode(paramValue, "utf-8")).append('&');
            }
            sb.deleteCharAt(sb.length() - 1);
            context.setRtbRequestContent(sb.toString());
        }
        return httpRequest;
    }

    @Override
    public ByydRequest mapRequest(HttpServletRequest httpRequest, RtbExecutionContext<HttpServletRequest, YieldlabBidResponse> context) throws IOException, NoBidException {
        return bidMapper.getRequest(context.getPublisherExternalId(), httpRequest, null);
    }

    @Override
    public YieldlabBidResponse mapResponse(ByydResponse byydResponse, RtbExecutionContext<HttpServletRequest, YieldlabBidResponse> context) throws IOException {
        return bidMapper.getResponse(byydResponse);
    }

    @Override
    public void write(YieldlabBidResponse rtbResponse, OutputStream stream, RtbExecutionContext<HttpServletRequest, YieldlabBidResponse> context) throws IOException {
        if (context.getSaveRtbMessages()) {
            ContentStoringWriter writer = new ContentStoringWriter(stream);
            try {
                responseWriter.writeValue(writer, rtbResponse);
            } finally {
                context.setRtbResponseContent(writer.getContent());
            }
        } else {
            responseWriter.writeValue(stream, rtbResponse);
        }
    }

    @Override
    public void onNoBidException(NoBidException nobidx, HttpServletRequest httpRequest, HttpServletResponse httpResponse,
            RtbExecutionContext<HttpServletRequest, YieldlabBidResponse> context) throws IOException {
        // same same same
        onBiddingException(nobidx, httpRequest, httpResponse, context);
    }

    @Override
    public void onBiddingException(Exception exception, HttpServletRequest httpRequest, HttpServletResponse httpResponse,
            RtbExecutionContext<HttpServletRequest, YieldlabBidResponse> context) throws IOException {

        String tid = httpRequest.getParameter("tid");
        String nobidReponse = buildNobidResponse(tid);
        httpResponse.getWriter().print(nobidReponse);
        if (context.getSaveRtbMessages()) {
            context.setRtbResponseContent(nobidReponse);
        }
    }

    public String buildNobidResponse(String tid) {
        StringWriter swriter = new StringWriter();
        try {
            this.responseWriter.writeValue(swriter, new YieldlabBidResponse(tid));
        } catch (IOException iox) {
            throw new IllegalStateException("Failed to prepare empty YieldlabBidResponse", iox);
        }
        return swriter.toString();
    }

}
