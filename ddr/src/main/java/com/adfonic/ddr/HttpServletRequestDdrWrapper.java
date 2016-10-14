package com.adfonic.ddr;

import javax.servlet.http.HttpServletRequest;

/**
 * Simple UserAgentAware implementation wrapped around an HttpServletRequest.
 * This is intended for use in webapps where you want to do device recognition.
 *
 * NOTE: effective User-Agent updates are stored and accessible after the fact.
 * That is, if a device is recognized using an alternate header, when
 * setEffectiveUserAgent is called, the value is stored for later access, in
 * case the creator of this wrapper needs that updated value for any reason.
 */
public class HttpServletRequestDdrWrapper implements UserAgentAware {
    private final HttpServletRequest request;
    private String effectiveUserAgent;
    
    public HttpServletRequestDdrWrapper(HttpServletRequest request) {
        this.request = request;
    }

    /** @{inheritDoc} */
    @Override
    public String getEffectiveUserAgent() {
        if (effectiveUserAgent != null) {
            return effectiveUserAgent;
        } else {
            return getHeader("User-Agent");
        }
    }

    /** @{inheritDoc} */
    @Override
    public void setUserAgent(String effectiveUserAgent) {
        this.effectiveUserAgent = effectiveUserAgent;
    }

    /** @{inheritDoc} */
    @Override
    public String getHeader(String header) {
        return request.getHeader(header);
    }
}
    