package com.adfonic.ddr;

/**
 * Interface that allows access to HTTP headers
 */
public interface HttpHeaderAware {
    /**
     * Generic way of accessing HTTP headers.  This is used, for example, by
     * the DdrService in determining if an alternate User-Agent was supplied
     * in form of various X- headers.
     * @return the value of the given HTTP header
     */
    String getHeader(String header);
}
    