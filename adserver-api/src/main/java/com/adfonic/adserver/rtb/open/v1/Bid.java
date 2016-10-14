package com.adfonic.adserver.rtb.open.v1;

import java.math.BigDecimal;
import java.util.Set;

public class Bid {

    /**
     * ID of the Imp object in the related bid request.
     */
    private String impid;

    /**
     * Bid price expressed as CPM although the actual transaction is
     * for a unit impression only. Note that while the type indicates 
     * float, integer math is highly recommended when handling 
     * currencies (e.g., BigDecimal in Java).
     */
    private BigDecimal price;

    /**
     * Advertiser domain for block list checking (e.g., “ford.com”).
     * This can be an array of for the case of rotating creatives.
     * Exchanges can mandate that only one domain is allowed.
     */
    private Object adomain;

    private String destination; //WTF ???

    private Set<Integer> attr;

    /**
     * ID of a preloaded ad to be served if the bid wins.
     */
    private String adid;

    /**
     * Win notice URL called by the exchange if the bid wins; 
     * optional means of serving ad markup.
     */
    private String nurl;

    /**
     * Optional means of conveying ad markup in case the bid wins;
     * supersedes the win notice if markup is included in both.
     */
    private String adm;

    /**
     * URL without cache-busting to an image that is representative
     * of the content of the campaign for ad quality/safety checking.
     */
    private String iurl;

    /**
     * Campaign ID to assist with ad quality checking; the collection
     * of creatives for which iurl should be representative.
     */
    private String cid;

    /**
     * Creative ID to assist with ad quality checking.
     */
    private String crid;

    /**
     * Reference to the deal.id from the bid request if this bid pertains to a private marketplace direct deal.
     */
    private String dealid;

    public String getImpid() {
        return impid;
    }

    public void setImpid(String impid) {
        this.impid = impid;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Object getAdomain() {
        return adomain;
    }

    public void setAdomain(String adomain) {
        this.adomain = adomain;
    }

    public String getCid() {
        return cid;
    }

    public void setCid(String cid) {
        this.cid = cid;
    }

    public String getCrid() {
        return crid;
    }

    public void setCrid(String crid) {
        this.crid = crid;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public Set<Integer> getAttr() {
        return attr;
    }

    public void setAttr(Set<Integer> attr) {
        this.attr = attr;
    }

    public String getIurl() {
        return iurl;
    }

    public void setIurl(String iurl) {
        this.iurl = iurl;
    }

    public String getAdm() {
        return adm;
    }

    public void setAdm(String adm) {
        this.adm = adm;
    }

    public String getNurl() {
        return nurl;
    }

    public void setNurl(String nurl) {
        this.nurl = nurl;
    }

    public String getAdid() {
        return adid;
    }

    public void setAdid(String adid) {
        this.adid = adid;
    }

    public String getDealid() {
        return dealid;
    }

    public void setDealid(String dealid) {
        this.dealid = dealid;
    }

}
