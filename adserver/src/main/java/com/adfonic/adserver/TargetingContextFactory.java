package com.adfonic.adserver;

import javax.servlet.http.HttpServletRequest;

public interface TargetingContextFactory {
    /**
     * Create a new empty TargetingContext.  This method will create
     * a new TargetingContext with no attributes.
     */
    TargetingContext createTargetingContext();

    /**
     * Create a new TargetingContext for a request.  This method will
     * populate the context with attributes from the HTTP request.
     * @param request the HTTP servlet request
     * @param useHttpHeaders whether or not the actual HTTP request
     * headers should be used, or whether we should only look for
     * parameter-based headers (i.e. server-to-server passed headers)
     * @throws InvalidIpAddressException if the provided IP address is invalid
     */
    //To be replaced by 2 specialized methods below
    TargetingContext createTargetingContext(HttpServletRequest request, boolean useHttpHeaders) throws InvalidIpAddressException;

    /**
     * Create a new TargetingContext for a request.  This method will
     * populate the context with attributes from the HTTP request.
     * 
     * Use HTTP headers directly as request is coming from end user device
     */
    TargetingContext buildForDevice(HttpServletRequest request) throws InvalidIpAddressException;

    /**
     * Create a new TargetingContext for a request.  This method will
     * populate the context with attributes from the HTTP request.
     * 
     * Only look for parameter-based headers (i.e. server-to-server passed headers)
     */
    TargetingContext buildForServer(HttpServletRequest request) throws InvalidIpAddressException;
}
