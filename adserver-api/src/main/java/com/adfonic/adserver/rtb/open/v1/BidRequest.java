package com.adfonic.adserver.rtb.open.v1;

import java.math.BigDecimal;
import java.util.List;

import com.adfonic.adserver.rtb.nativ.BaseRequest;


public class BidRequest implements BaseRequest{

    private String id;

    private Long tmax;
    
    private Site site;
    
    private App app;
    
    private Device device;
    
    private User user;
    
    private Restrictions restrictions;
    
    private List<Imp> imp;

    // mopub specific field; does not obey rtbv1 convention for custom fields because mopub does not
    private BigDecimal pf;
    
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Long getTmax() {
        return tmax;
    }

    public void setTmax(Long tmax) {
        this.tmax = tmax;
    }

    public Site getSite() {
        return site;
    }

    public void setSite(Site site) {
        this.site = site;
    }

    public App getApp() {
        return app;
    }

    public void setApp(App app) {
        this.app = app;
    }

    public Device getDevice() {
        return device;
    }

    public void setDevice(Device device) {
        this.device = device;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Restrictions getRestrictions() {
        return restrictions;
    }

    public void setRestrictions(Restrictions restrictions) {
        this.restrictions = restrictions;
    }

    public List<Imp> getImp() {
        return imp;
    }

    public void setImp(List<Imp> imp) {
        this.imp = imp;
    }

    public BigDecimal getPf() {
        return pf;
    }

    public void setPf(BigDecimal pf) {
        this.pf = pf;
    }

}
