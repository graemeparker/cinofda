package com.adfonic.adserver.rtb.nativ;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import com.adfonic.domain.BidType;
import com.adfonic.domain.ContentForm;
import com.adfonic.domain.DestinationType;
import com.adfonic.ortb.nativead.NativeAdRequest;
import com.google.common.collect.ImmutableSet;

public class ByydImp {

    public static final Set<ContentForm> CF_MOBILE_WEB = ImmutableSet.of(ContentForm.MOBILE_WEB);
    public static final Set<ContentForm> CF_MRAID_MOBWEB = ImmutableSet.of(ContentForm.MRAID_1_0, ContentForm.MOBILE_WEB);

    private String impid;

    private Set<AdType> btype;

    private Set<Integer> battr;

    private Integer h;

    private Integer w;

    /**
     * Flag to disable bidding 300x?? banner into 320x?? slot
     */
    private boolean strictBannerSize;

    private List<String> mimeTypeWhiteList;

    private boolean bypassCFRestrictions;

    private Set<ContentForm> contentFormWhiteList;

    private BigDecimal bidfloor;

    private String bidfloorcur;

    private static final BigDecimal NEAR_ZERO = new BigDecimal(0.0000001d);

    private Set<DestinationType> bDestTypes;

    private Set<BidType> bBidTypes;

    private boolean blockExtendedCreatives;

    private Set<String> blockedExtendedCreativeTypes;

    private AdObject adObject;

    private Integer minduration;

    private Integer maxduration;

    private Integer skipafter;

    private Set<Integer> videoProtocols;

    private boolean sslRequired = false;

    private boolean interstitial = false;

    private boolean nativeBrowserClick = false;

    private NativeAdRequest nativeAdRequest;

    private IntegrationTypeLookup integrationTypeDeriver = null;

    public ByydImp(String impid) {
        this.impid = impid;
    }

    ByydImp() {
        //marshalling
    }

    public String getImpid() {
        return impid;
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

    public void setBtypeDefault(Set<AdType> btype) {
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

    public boolean isStrictBannerSize() {
        return strictBannerSize;
    }

    public void setStrictBannerSize(boolean strictBannerSize) {
        this.strictBannerSize = strictBannerSize;
    }

    public List<String> getMimeTypeWhiteList() {
        return mimeTypeWhiteList;
    }

    public void setMimeTypeWhiteList(List<String> mimeTypeWhiteList) {
        this.mimeTypeWhiteList = mimeTypeWhiteList;
    }

    public BigDecimal getBidfloor() {
        return bidfloor != null ? bidfloor : NEAR_ZERO;
    }

    public void setBidfloor(BigDecimal bidfloor) {
        this.bidfloor = bidfloor;
    }

    public String getBidfloorcur() {
        return bidfloorcur;
    }

    public void setBidfloorcur(String bidfloorcur) {
        this.bidfloorcur = bidfloorcur;
    }

    public Set<ContentForm> getContentFormWhiteList() {
        return contentFormWhiteList;
    }

    public void setContentFormWhiteList(Set<ContentForm> contentFormWhiteList) {
        this.contentFormWhiteList = contentFormWhiteList;
    }

    /*
        public void setContentFormWhiteListToDefault() {
            this.contentFormWhiteList = DEF_ENABLED_CFS;
        }

        public static Set<ContentForm> getDefaultContentFormWhiteList() {
            //return Collections.EMPTY_SET;
            return DEF_ENABLED_CFS;
        }

        public Set<ContentForm> contentFormWhiteListInitializedToDefault() {
            return this.contentFormWhiteList = new LinkedHashSet<>(DEF_ENABLED_CFS);
        }
    */
    public Set<DestinationType> getbDestTypes() {
        return bDestTypes;
    }

    public void setbDestTypes(Set<DestinationType> bDestTypes) {
        this.bDestTypes = bDestTypes;
    }

    public Set<BidType> getbBidTypes() {
        return bBidTypes;
    }

    public void setbBidTypes(Set<BidType> bBidTypes) {
        this.bBidTypes = bBidTypes;
    }

    public boolean isBlockExtendedCreatives() {
        return blockExtendedCreatives;
    }

    public void setBlockExtendedCreatives(boolean blockExtendedCreatives) {
        this.blockExtendedCreatives = blockExtendedCreatives;
    }

    public Set<String> getBlockedExtendedCreativeTypes() {
        return blockedExtendedCreativeTypes;
    }

    public void setBlockedExtendedCreativeTypes(Set<String> blockedExtendedCreativeTypes) {
        this.blockedExtendedCreativeTypes = blockedExtendedCreativeTypes;
    }

    public IntegrationTypeLookup getIntegrationTypeDeriver() {
        return integrationTypeDeriver;
    }

    public void setIntegrationTypeDeriver(IntegrationTypeLookup integrationTypeDeriver) {
        this.integrationTypeDeriver = integrationTypeDeriver;
    }

    public boolean bypassCFRestrictions() {
        return bypassCFRestrictions;
    }

    public void bypassCFRestrictions(boolean bypassCFRestrictions) {
        this.bypassCFRestrictions = bypassCFRestrictions;
    }

    public AdObject getAdObject() {
        return adObject;
    }

    public void setAdObject(AdObject adObject) {
        this.adObject = adObject;
    }

    public Integer getMinduration() {
        return minduration;
    }

    public void setMinduration(Integer minduaration) {
        this.minduration = minduaration;
    }

    public Integer getMaxduration() {
        return maxduration;
    }

    public void setMaxduration(Integer maxduration) {
        this.maxduration = maxduration;
    }

    public void setSslRequired(boolean sslRequired) {
        this.sslRequired = sslRequired;
    }

    public boolean isSslRequired() {
        return sslRequired;
    }

    public void setInterstitial(boolean interstitial) {
        this.interstitial = interstitial;
    }

    public boolean isInterstitial() {
        return interstitial;
    }

    public void setNativeBrowserClick(boolean nativeBrowserClick) {
        this.nativeBrowserClick = nativeBrowserClick;
    }

    public boolean isNativeBrowserClick() {
        return nativeBrowserClick;
    }

    public NativeAdRequest getNativeAdRequest() {
        return nativeAdRequest;
    }

    public void setNativeAdRequest(NativeAdRequest nativeAdDetails) {
        this.nativeAdRequest = nativeAdDetails;
    }

    public Set<Integer> getVideoProtocols() {
        return videoProtocols;
    }

    public void setVideoProtocols(Set<Integer> videoProtocols) {
        this.videoProtocols = videoProtocols;
    }

    public Integer getSkipafter() {
        return skipafter;
    }

    public void setSkipafter(Integer skipafter) {
        this.skipafter = skipafter;
    }

}
