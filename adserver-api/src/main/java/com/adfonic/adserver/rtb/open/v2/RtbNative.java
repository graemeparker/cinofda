package com.adfonic.adserver.rtb.open.v2;

import java.util.List;
import java.util.Set;

/**
 * OpenRTB-API-Specification
 * A Native object (Section 3.2.5); required if this impression is offered as a native ad opportunity.
 *
 */
public class RtbNative {

    /**
     * Request payload complying with the Native Ad Specification.
     * 
     * string; required
     */
    private String request;

    /**
     * Version of the Native Ad Specification to which request complies; highly recommended for efficient parsing.
     * 
     * string; recommended
     */
    private String ver;

    /**
     * List of supported API frameworks for this impression. Refer to List 5.6. 
     * If an API is not explicitly listed, it is assumed not to be supported.
     * 
     * integer array
     */
    private List<Integer> api;

    /**
     * Blocked creative attributes. Refer to List 5.3.
     * 
     * integer array
     */
    private Set<Integer> battr;

    public String getRequest() {
        return request;
    }

    public void setRequest(String request) {
        this.request = request;
    }

    public String getVer() {
        return ver;
    }

    public void setVer(String ver) {
        this.ver = ver;
    }

    public List<Integer> getApi() {
        return api;
    }

    public void setApi(List<Integer> api) {
        this.api = api;
    }

    public Set<Integer> getBattr() {
        return battr;
    }

    public void setBattr(Set<Integer> battr) {
        this.battr = battr;
    }

}
