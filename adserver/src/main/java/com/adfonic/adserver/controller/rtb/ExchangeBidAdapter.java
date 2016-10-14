package com.adfonic.adserver.controller.rtb;

import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.http.HttpServletRequest;

import com.adfonic.adserver.rtb.NoBidException;
import com.adfonic.adserver.rtb.nativ.ByydRequest;
import com.adfonic.adserver.rtb.nativ.ByydResponse;

/**
 * 
 * @author mvanek
 *
 */
public interface ExchangeBidAdapter<I, O> extends ExchangeExceptionHandler<I, O> {

    public I read(HttpServletRequest httpRequest, RtbExecutionContext<I, O> context) throws IOException;

    public ByydRequest mapRequest(I rtbRequest, RtbExecutionContext<I, O> context) throws IOException, NoBidException;

    public O mapResponse(ByydResponse byydResponse, RtbExecutionContext<I, O> context) throws IOException, NoBidException;

    public void write(O rtbResponse, OutputStream stream, RtbExecutionContext<I, O> context) throws IOException;

}
