package com.adfonic.adserver.rtb.open.v1;

import java.util.List;
import java.util.Set;

import com.adfonic.adserver.rtb.nativ.APIFramework;
import com.adfonic.adserver.rtb.nativ.AdType;

public class Imp {

    private String impid;

    private Set<AdType> btype;

    private Set<Integer> battr;

    private Integer h;

    private Integer w;

    // mopub specific field; does not obey rtbv1 convention for custom fields because mopub does not
    private APIFramework api;

    // mopub specific; sdk ver
    private String displaymanagerver;

    // mopub specific; list of deals (excl)
    private List<String> wseat;

    public String getImpid() {
        return impid;
    }

    public void setImpid(String impid) {
        this.impid = impid;
    }

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

    public APIFramework getApi() {
        return api;
    }

    public void setApi(APIFramework api) {
        this.api = api;
    }

    public String getDisplaymanagerver() {
        return displaymanagerver;
    }

    public void setDisplaymanagerver(String displaymanagerver) {
        this.displaymanagerver = displaymanagerver;
    }

    public List<String> getWseat() {
        return wseat;
    }

    public void setWseat(List<String> wseat) {
        this.wseat = wseat;
    }

}
