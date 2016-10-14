package com.adfonic.adserver.controller.rtb;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.adfonic.adserver.rtb.NoBidException;

public interface ExchangeExceptionHandler<I, O> {

    /**
     * Expected and common NoBidException
     */
    public void onNoBidException(NoBidException nobidx, HttpServletRequest httpRequest, HttpServletResponse httpResponse, RtbExecutionContext<I, O> context) throws IOException;

    /**
     * Uncaught exception from mapping or bidding
     */
    public void onBiddingException(Exception exception, HttpServletRequest httpRequest, HttpServletResponse httpResponse, RtbExecutionContext<I, O> context) throws IOException;
}
