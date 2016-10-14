package com.adfonic.adserver.rtb.open.v2;

/**
 * OpenRTB-API-Specification
 * 3.2.3 Object: Banner
 * 
 */
import java.util.List;
import java.util.Set;

import com.adfonic.adserver.rtb.nativ.APIFramework;
import com.adfonic.adserver.rtb.nativ.AdType;

public class Banner {

    /**
     * Width of the impression in pixels.
     * If neither wmin nor wmax are specified, this value is an exact
     * width requirement. Otherwise it is a preferred width.
     */
    private Integer w;

    /**
     * Height of the impression in pixels.
     * If neither hmin nor hmax are specified, this value is an exact
     * height requirement. Otherwise it is a preferred height.
     */
    private Integer h;

    /**
     * Blocked banner ad types. Refer to List 5.2
     */
    private Set<AdType> btype;

    /**
     * Blocked creative attributes. Refer to List 5.3.
     */
    private Set<Integer> battr;

    /**
     * Content MIME types supported. Popular MIME types may
     * include “application/x-shockwave-flash”, “image/jpg”, and “image/gif” 
     */
    private List<String> mimes;

    /**
     * List of supported API frameworks for this impression. Refer to
     * List 5.6. If an API is not explicitly listed, it is assumed not to be
     * supported.
     */
    private Set<APIFramework> api;

    /*
     * Unmapped/ignored
     * wmax, hmax, wmin, hmin, id, pos, topframe, expdir
     */

    public Set<AdType> getBtype() {
        return btype;
    }

    public void setBtype(Set<AdType> btype) {
        if (btype != null) {
            btype.remove(null);// deserializer return nulls for invalids
        }
        this.btype = btype;
    }

    public Set<Integer> getBattr() {
        return battr;
    }

    public void setBattr(Set<Integer> battr) {
        if (battr != null) {
            battr.remove(null);// deserializer return nulls for invalids
        }
        this.battr = battr;
    }

    public Integer getH() {
        return h;
    }

    public void setH(Integer h) {
        this.h = h;
    }

    public Integer getW() {
        return w;
    }

    public void setW(Integer w) {
        this.w = w;
    }

    public List<String> getMimes() {
        return mimes;
    }

    public void setMimes(List<String> mimes) {
        this.mimes = mimes;
    }

    public Set<APIFramework> getApi() {
        return api;
    }

    public void setApi(Set<APIFramework> api) {
        this.api = api;
    }

}
