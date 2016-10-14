package com.adfonic.adserver.controller.rtb;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;

import com.adfonic.adserver.rtb.NoBidException;
import com.adfonic.adserver.rtb.mapper.OpenXMapper;
import com.adfonic.adserver.rtb.nativ.ByydRequest;
import com.adfonic.adserver.rtb.openx.OpenX;

/**
 * 
 * @author mvanek
 *
 */
public class OpenXBidAdapter implements ExchangeBidAdapter<OpenX.BidRequest, OpenX.BidResponse>, ExchangeExceptionHandler<OpenX.BidRequest, OpenX.BidResponse> {

    private final OpenXMapper openxMapper = OpenXMapper.instance();

    @Override
    public OpenX.BidRequest read(HttpServletRequest httpRequest, RtbExecutionContext<OpenX.BidRequest, OpenX.BidResponse> context) throws IOException {
        if (context.getSaveRtbMessages()) {
            ContentStoringInputStream stream = new ContentStoringInputStream(httpRequest);
            try {
                return OpenX.BidRequest.parseFrom(stream);
            } finally {
                context.setRtbRequestContent(stream.getContent());
            }
        } else {
            return OpenX.BidRequest.parseFrom(httpRequest.getInputStream());
        }
    }

    @Override
    public ByydRequest mapRequest(OpenX.BidRequest rtbRequest, RtbExecutionContext<OpenX.BidRequest, OpenX.BidResponse> context) throws IOException, NoBidException {
        ByydRequest byydRequest = openxMapper.mapRequest(context.getPublisherExternalId(), rtbRequest);
        String ptmax = context.getHttpContext().getHttpRequest().getParameter("tmax");
        if (StringUtils.isNotBlank(ptmax)) {
            Long tmax = Long.valueOf(ptmax);
            byydRequest.setTmax(tmax);
        }
        return byydRequest;
    }

    @Override
    public OpenX.BidResponse mapResponse(com.adfonic.adserver.rtb.nativ.ByydResponse byydResponse, RtbExecutionContext<OpenX.BidRequest, OpenX.BidResponse> context)
            throws IOException {
        return openxMapper.mapResponse(byydResponse, context.getRtbRequest());
    }

    @Override
    public void write(OpenX.BidResponse rtbResponse, OutputStream stream, RtbExecutionContext<OpenX.BidRequest, OpenX.BidResponse> context) throws IOException {
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
    public void onNoBidException(NoBidException nobidx, HttpServletRequest httpRequest, HttpServletResponse httpResponse,
            RtbExecutionContext<OpenX.BidRequest, OpenX.BidResponse> context) throws IOException {

        onBiddingException(nobidx, httpRequest, httpResponse, context);
    }

    @Override
    public void onBiddingException(Exception exception, HttpServletRequest httpRequest, HttpServletResponse httpResponse,
            RtbExecutionContext<OpenX.BidRequest, OpenX.BidResponse> context) throws IOException {

        OpenX.BidRequest rtbRequest = context.getRtbRequest();
        OpenX.BidResponse rtbResponse = OpenX.BidResponse.newBuilder().setApiVersion(rtbRequest.getApiVersion()).setAuctionId(rtbRequest.getAuctionId()).build();
        write(rtbResponse, httpResponse.getOutputStream(), context);
    }

    public static class ContentStoringOutputStream extends OutputStream {

        private final ByteArrayOutputStream baos;

        private final OutputStream delegate;

        public ContentStoringOutputStream(OutputStream stream) throws IOException {
            this.delegate = stream;
            this.baos = new ByteArrayOutputStream(ContentStoringInputStream.DEFBUFFSIZE);
        }

        public byte[] getContent() {
            return baos.toByteArray();
        }

        @Override
        public void write(int b) throws IOException {
            delegate.write(b);
            baos.write(b);
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            delegate.write(b, off, len);
            baos.write(b, off, len);
        }

    }

    public static class ContentStoringInputStream extends InputStream {

        private static final int DEFBUFFSIZE = 4096;

        private final InputStream delegate;

        private final ByteArrayOutputStream baos;

        public ContentStoringInputStream(HttpServletRequest httpRequest) throws IOException {
            this.delegate = httpRequest.getInputStream();
            int contentLength = httpRequest.getContentLength();
            if (contentLength == -1) {
                contentLength = DEFBUFFSIZE;
            }
            this.baos = new ByteArrayOutputStream(contentLength);
        }

        public byte[] getContent() {
            return baos.toByteArray();
        }

        @Override
        public int read() throws IOException {
            int read = delegate.read();
            if (read != -1) {
                baos.write(read);
            }
            return read;
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            int read = delegate.read(b, off, len);
            if (read > 0) {
                baos.write(b, off, read);
            }
            return read;
        }

    }
}
