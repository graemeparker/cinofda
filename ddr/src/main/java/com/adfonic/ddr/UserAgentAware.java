package com.adfonic.ddr;

/**
 * Interface that extends HeaderAware with methods that wrap around read
 * and write access to User-Agent.  This allows a "callback" to update
 * the effective User-Agent, if it was gleaned from an alternate header.
 */
public interface UserAgentAware extends HttpHeaderAware {
    /**
     * @return the effective value of the User-Agent header
     */
    String getEffectiveUserAgent();

    /**
     * Update the effective User-Agent header value.  This is a callback that
     * happens as a "output" from doing device recognition.  The DdrService
     * may determine that some header other than User-Agent is the actual
     * one used to identify the device.  This will typically only be called
     * when the DdrService identifies the device with an X- header instead
     * of the supplied User-Agent.
     * @param effectiveUserAgent the User-Agent that was used to identify the
     * device successfully
     */
    void setUserAgent(String effectiveUserAgent);
}
    