package com.adfonic.adserver.rtb.open.v2;

import java.util.List;

import com.adfonic.adserver.rtb.nativ.BaseRequest;

public class BidRequest<T extends Imp> implements BaseRequest {

    private String id;

    /**
     * Array of Imp objects (Section 3.2.2) representing the impressions offered. 
     * At least 1 Imp object is required.
     */
    private List<T> imp;

    /**
     * Details via a Site object (Section 3.2.6) about the publisher’s website. 
     * Only applicable and recommended for websites.
     */
    private Site site;

    /**
     * Details via an App object (Section 3.2.7) about the publisher’s app (i.e., non-browser applications). 
     * Only applicable and recommended for apps.
     */
    private App app;

    /**
     * Details via a Device object (Section 3.2.11) about the user’s device to which the impression will be delivered.
     */
    private Device device;

    /**
     * Details via a User object (Section 3.2.13) about the human user of the device; the advertising audience.
     */
    private User user;

    /**
     * Maximum time in milliseconds to submit a bid to avoid timeout. This value is commonly communicated offline.
     */
    private Long tmax;

    /**
     * Indicator of test mode in which auctions are not billable, where 0 = live mode, 1 = test mode.
     */
    private Integer test;

    /**
     * Whitelist of buyer seats allowed to bid on this impression.
     * Seat IDs must be communicated between bidders and the exchange a priori. 
     * Omission implies no seat restrictions.
     */
    private List<String> wseat;

    /**
     * Array of allowed currencies for bids on this bid request using ISO-4217 alpha codes. 
     * Recommended only if the exchange accepts multiple currencies.
     */
    private List<String> cur;

    /**
     * Blocked advertiser categories using the IAB content categories. Refer to List 5.1.
     */
    private List<String> bcat;

    /**
     * Block list of advertisers by their domains (e.g., “ford.com”).
     */
    private List<String> badv;

    /**
     * A Regs object (Section 3.2.16) that specifies any industry, legal, 
     * or governmental regulations in force for this request.
     */
    private RtbRegs regs;

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

    public List<String> getBcat() {
        return bcat;
    }

    public void setBcat(List<String> bcat) {
        this.bcat = bcat;
    }

    public List<String> getBadv() {
        return badv;
    }

    public void setBadv(List<String> badv) {
        this.badv = badv;
    }

    public List<T> getImp() {
        return imp;
    }

    public void setImp(List<T> imp) {
        this.imp = imp;
    }

    public List<String> getCur() {
        return cur;
    }

    public void setCur(List<String> cur) {
        this.cur = cur;
    }

    public Integer getTest() {
        return test;
    }

    public void setTest(Integer test) {
        this.test = test;
    }

    public List<String> getWseat() {
        return wseat;
    }

    public void setWseat(List<String> wseat) {
        this.wseat = wseat;
    }

    public RtbRegs getRegs() {
        return regs;
    }

    public void setRegs(RtbRegs regs) {
        this.regs = regs;
    }
}
