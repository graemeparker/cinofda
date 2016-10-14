package com.adfonic.adserver.rtb.yieldlab;

public class YieldlabBid {

    private String cpm = "0.0";

    private String tid;

    private String adtag;

    private String advertiser;

    private String camurl;

    /**
     * Multi-agency parameter for the demand-side seat ID specifying the agency. This parameter is required.
     */
    private String seatid;

    /**
     * Multi-agency parameter for the demandside deal ID indicating an agency partnership. This parameter is optional.
     */
    private String dealid;

    public String getCpm() {
        return cpm;
    }

    public void setCpm(String cpm) {
        this.cpm = cpm;
    }

    public String getTid() {
        return tid;
    }

    public void setTid(String tid) {
        this.tid = tid;
    }

    public String getAdtag() {
        return adtag;
    }

    public void setAdtag(String adtag) {
        this.adtag = adtag;
    }

    public String getAdvertiser() {
        return advertiser;
    }

    public void setAdvertiser(String advertiser) {
        this.advertiser = advertiser;
    }

    public String getCamurl() {
        return camurl;
    }

    public void setCamurl(String camurl) {
        this.camurl = camurl;
    }

    public String getSeatid() {
        return seatid;
    }

    public void setSeatid(String seatid) {
        this.seatid = seatid;
    }

    public String getDealid() {
        return dealid;
    }

    public void setDealid(String dealid) {
        this.dealid = dealid;
    }

}
