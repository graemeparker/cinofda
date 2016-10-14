package com.adfonic.adserver.rtb.nativ;

import java.math.BigDecimal;
import java.util.Set;

import com.adfonic.domain.ContentForm;
import com.adfonic.domain.cache.dto.adserver.creative.CreativeDto;
import com.adfonic.ortb.nativead.NativeAdResponse.NativeAdResponseWrapper;

public class ByydBid {

    private String impid;

    private BigDecimal price;

    private String adomain;

    private String cid;

    private String crid;

    private String iabId;

    private String destination;

    private Set<Integer> attr;

    private String iurl;

    //sample image url incase of text
    private String txtIUrl;

    private String adm; // advertisement markup

    @Deprecated
    private String ext; // Only for Mopub 2.1 native ads

    private String nurl; // notification url

    private String adid;

    // Asset external id - for exchanges who has to have really unique creative id
    private String assetId;

    private String dealId; // PMP/Direct Deal

    private String seat; // PMP seat

    private String publisherCreativeId;

    private Integer duration; //video

    private CreativeDto creative;

    private ByydImp imp;

    private ContentForm contentForm;

    private NativeAdResponseWrapper nativeAdResponse;

    protected ByydBid() {
        //json
    }

    public ByydBid(ByydImp imp) {
        this.imp = imp;
        this.impid = imp.getImpid();
    }

    public String getImpid() {
        return impid;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getAdomain() {
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

    public String getIabId() {
        return iabId;
    }

    public void setIabId(String iabId) {
        this.iabId = iabId;
    }

    public String getAssetId() {
        return assetId;
    }

    public void setAssetId(String assetId) {
        this.assetId = assetId;
    }

    public String getTxtIUrl() {
        return txtIUrl;
    }

    public void setTxtIUrl(String txtIUrl) {
        this.txtIUrl = txtIUrl;
    }

    public String getDealId() {
        return dealId;
    }

    public void setDealId(String dealId) {
        this.dealId = dealId;
    }

    public ByydImp getImp() {
        return imp;
    }

    public String getPublisherCreativeId() {
        return publisherCreativeId;
    }

    public void setPublisherCreativeId(String publisherCreativeId) {
        this.publisherCreativeId = publisherCreativeId;
    }

    @Deprecated
    public String getExt() {
        return ext;
    }

    @Deprecated
    public void setExt(String ext) {
        this.ext = ext;
    }

    public String getSeat() {
        return seat;
    }

    public void setSeat(String seat) {
        this.seat = seat;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public CreativeDto getCreative() {
        return creative;
    }

    public void setCreative(CreativeDto creative) {
        this.creative = creative;
    }

    public void setContentForm(ContentForm contentForm) {
        this.contentForm = contentForm;
    }

    public ContentForm getContentForm() {
        return contentForm;
    }

    public void setNativeAdResponse(NativeAdResponseWrapper nativeAdResponse) {
        this.nativeAdResponse = nativeAdResponse;
    }

    public NativeAdResponseWrapper getNativeAdResponse() {
        return nativeAdResponse;
    }

}
