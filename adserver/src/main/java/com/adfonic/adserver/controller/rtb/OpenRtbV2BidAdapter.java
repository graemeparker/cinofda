package com.adfonic.adserver.controller.rtb;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.Objects;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;

import com.adfonic.adserver.controller.WebConfig;
import com.adfonic.adserver.rtb.NoBidException;
import com.adfonic.adserver.rtb.mapper.OpenRTBv2ByHandMapper;
import com.adfonic.adserver.rtb.nativ.ByydRequest;
import com.adfonic.adserver.rtb.nativ.ByydResponse;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;

/**
 * 
 * @author mvanek
 *
 * @param <I> Bid request
 * @param <O> Bid response
 */
public class OpenRtbV2BidAdapter<I extends com.adfonic.adserver.rtb.open.v2.BidRequest, O extends com.adfonic.adserver.rtb.open.v1.BidResponse> implements
        ExchangeBidAdapter<I, O>, ExchangeExceptionHandler<I, O> {

    private final ObjectReader objectReader;
    private final ObjectWriter objectWriter;

    private final OpenRTBv2ByHandMapper rtbByydMapper;

    private Class<I> requestClass;
    private Class<O> responseClass;

    public OpenRtbV2BidAdapter(OpenRTBv2ByHandMapper rtbByydMapper, Class<I> requestClass, Class<O> responseClass) {
        this.objectReader = WebConfig.getRtbJsonMapper().readerFor(requestClass);
        this.objectWriter = WebConfig.getRtbJsonMapper().writerFor(responseClass);

        Objects.requireNonNull(rtbByydMapper);
        this.rtbByydMapper = rtbByydMapper;
        Objects.requireNonNull(requestClass);
        this.requestClass = requestClass;
        this.responseClass = responseClass;
    }

    @Override
    public I read(HttpServletRequest httpRequest, RtbExecutionContext<I, O> context) throws IOException {
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
    public ByydRequest mapRequest(I rtbRequest, RtbExecutionContext<I, O> context) throws IOException, NoBidException {
        return rtbByydMapper.mapRtbRequest(context.getPublisherExternalId(), rtbRequest, null);
    }

    @Override
    public O mapResponse(ByydResponse byydResponse, RtbExecutionContext<I, O> context) throws IOException {
        return (O) rtbByydMapper.mapRtbResponse(byydResponse, context.getByydRequest());
    }

    @Override
    public void write(O rtbResponse, OutputStream stream, RtbExecutionContext<I, O> context) throws IOException {
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
    public void onNoBidException(NoBidException nbx, HttpServletRequest httpRequest, HttpServletResponse httpResponse, RtbExecutionContext<I, O> context) {
        // Simply return 204 HttpStatus.NO_CONTENT
        httpResponse.setStatus(HttpStatus.NO_CONTENT.value());
    }

    @Override
    public void onBiddingException(Exception nbx, HttpServletRequest httpRequest, HttpServletResponse httpResponse, RtbExecutionContext<I, O> context) {
        // Simply return 204 HttpStatus.NO_CONTENT
        httpResponse.setStatus(HttpStatus.NO_CONTENT.value());
    }

    public static class ContentStoringReader extends Reader {

        private static final int DEFBUFFSIZE = 4096;

        private final Reader delegate;

        private final StringBuilder contentBuilder;

        private int read;

        private boolean closed;

        public ContentStoringReader(HttpServletRequest httpRequest) throws IOException {
            this.delegate = httpRequest.getReader();
            int contentLength = httpRequest.getContentLength();
            if (contentLength != -1) {
                contentBuilder = new StringBuilder(contentLength);
            } else {
                contentBuilder = new StringBuilder(DEFBUFFSIZE);
            }
        }

        @Override
        public int read(char[] cbuf, int off, int len) throws IOException {
            this.read = delegate.read(cbuf, off, len);
            if (read > 0) {
                contentBuilder.append(cbuf, off, read);
            }
            return read;
        }

        /**
         * Nobody should be closing HttpServletRequest stream/reader
         */
        @Override
        public void close() throws IOException {
            this.closed = true;
            //this.delegate.close();
        }

        public String getContent() {
            //input reading is not finished because of parsing exception maybe...
            if (this.read != -1 && !this.closed) {
                try {
                    //only when there is something immediately to read...
                    if (this.delegate.ready()) {
                        char[] buffer = new char[DEFBUFFSIZE];
                        while ((this.read = this.delegate.read(buffer)) != -1) {
                            contentBuilder.append(buffer, 0, read);
                        }
                    }
                } catch (IOException iox) {
                    //ignore as we can't do more
                }
            }
            return contentBuilder.toString();
        }
    }

    public static class ContentStoringWriter extends Writer {

        private static final int DEFBUFFSIZE = 4096;

        private final Writer delegate;

        private final StringBuilder contentBuilder;

        private boolean closed;

        public ContentStoringWriter(Writer writer) throws IOException {
            this.delegate = writer;
            this.contentBuilder = new StringBuilder(DEFBUFFSIZE);
        }

        public ContentStoringWriter(OutputStream stream) throws IOException {
            this.delegate = new OutputStreamWriter(stream);
            this.contentBuilder = new StringBuilder(DEFBUFFSIZE);
        }

        /**
         * Nobody should be closing HttpServletRequest stream/reader
         */
        @Override
        public void close() throws IOException {
            this.closed = true;
            this.delegate.close();
        }

        @Override
        public void write(char[] cbuf, int off, int len) throws IOException {
            delegate.write(cbuf, off, len);
            contentBuilder.append(cbuf, off, len);
        }

        @Override
        public void flush() throws IOException {
            delegate.flush();
        }

        public String getContent() {
            return contentBuilder.toString();
        }

    }

}