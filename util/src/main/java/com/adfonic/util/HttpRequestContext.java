package com.adfonic.util;

/**
 * Defines common interface to data deriving from a HTTP request.
 */
public interface HttpRequestContext {
    /**
     * Returns the value of a HTTP header. By contract, this should be
     * case-insensitive.
     */
    String getHeader(String header);
}
