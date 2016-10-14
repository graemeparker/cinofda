package com.adfonic.adserver.rtb.open.v2;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * OpenRTB-API-Specification
 * 
 * 3.2.2 Object: Imp
 *
 */
public class Imp {

    /**
     * A unique identifier for this impression within the context of
     * the bid request (typically, starts with 1 and increments.
     * 
     * string; required 
     */
    private String id;

    /**
     * A Banner object (Section 3.2.3); required if this impression is
     * offered as a banner ad opportunity.
     */
    private Banner banner;

    /**
     * A Video object (Section 3.2.4); required if this impression is
     * offered as a video ad opportunity.
     */
    private VideoV2 video;

    /**
     * A Native object (Section 3.2.5); required if this impression is
     * offered as a native ad opportunity
     */
    @JsonProperty("native")
    private RtbNative _native; // native is java keyword...

    /**
     * Name of ad mediation partner, SDK technology, or player
     * responsible for rendering ad (typically video or mobile). Used
     * by some ad servers to customize ad code by partner.
     * Recommended for video and/or apps.
     */
    private String displaymanager;

    /**
     * Version of ad mediation partner, SDK technology, or player
     * responsible for rendering ad (typically video or mobile). Used
     * by some ad servers to customize ad code by partner.
     * Recommended for video and/or apps.
     */
    private String displaymanagerver;

    /**
     * 1 if the ad is interstitial or full screen; else 0 (i.e., no).
     */
    private Integer instl;

    /**
     * Identifier for specific ad placement or ad tag that was used to initiate the auction.
     * This can be useful for debugging of any issues, or for optimization by the buyer.
     */
    private String tagid;

    /**
     * Minimum bid for this impression expressed in CPM.
     * 
     * float; default 0
     */
    private BigDecimal bidfloor;

    /**
     * Currency specified using ISO-4217 alpha codes. This may be
     * different from bid currency returned by bidder if this is
     * allowed by the exchange.
     * 
     * string; default “USD”
     */
    private String bidfloorcur;

    /**
     * Flag to indicate whether the impression requires secure HTTPS URL creative assets and markup. 
     * A value of “1” means that the impression requires secure assets. A value of "0" means non-secure assets. 
     * If this field is omitted the bidder should interpret the secure state is unknown and assume HTTP is supported.
     */
    private Integer secure;

    /**
     * This object is the private marketplace container for direct deals between buyers and sellers that may pertain to this impression. 
     */
    public PmpV2 pmp;

    /*
     * Unmapped v2 spec attributes
     * 
     * - tagid : Identifier for specific ad placement or ad tag that was used to initiate the auction.  
     * - iframebuster : Array of exchange-specific names of supported iframe busters.
     */

    public String getDisplaymanager() {
        return displaymanager;
    }

    public void setDisplaymanager(String displaymanager) {
        this.displaymanager = displaymanager;
    }

    public String getDisplaymanagerver() {
        return displaymanagerver;
    }

    public void setDisplaymanagerver(String displaymanagerver) {
        this.displaymanagerver = displaymanagerver;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public BigDecimal getBidfloor() {
        return bidfloor;
    }

    public void setBidfloor(BigDecimal bidfloor) {
        this.bidfloor = bidfloor;
    }

    public Banner getBanner() {
        return banner;
    }

    public void setBanner(Banner banner) {
        this.banner = banner;
    }

    public VideoV2 getVideo() {
        return video;
    }

    public void setVideo(VideoV2 video) {
        this.video = video;
    }

    public RtbNative getNative() {
        return _native;
    }

    public void setNative(RtbNative _native) {
        this._native = _native;
    }

    public String getBidfloorcur() {
        return bidfloorcur;
    }

    public void setBidfloorcur(String bidfloorcur) {
        this.bidfloorcur = bidfloorcur;
    }

    public PmpV2 getPmp() {
        return pmp;
    }

    public void setPmp(PmpV2 pmp) {
        this.pmp = pmp;
    }

    public Integer getSecure() {
        return secure;
    }

    public void setSecure(Integer secure) {
        this.secure = secure;
    }

    public boolean isSslRequired() {
        return secure != null && secure == 1;
    }

    public Integer getInstl() {
        return instl;
    }

    public void setInstl(Integer instl) {
        this.instl = instl;
    }

    public boolean isInterstitial() {
        return instl != null && instl == 1;
    }

    public String getTagid() {
        return tagid;
    }

    public void setTagid(String tagid) {
        this.tagid = tagid;
    }
}
