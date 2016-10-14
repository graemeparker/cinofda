package com.adfonic.adserver.rtb.open.v2.ext.mopub;

import java.util.List;

import com.adfonic.ortb.nativead.NativeAdResponse.NativeAdResponseWrapper;

/**
 * https://dev.twitter.com/mopub-demand/overview/openrtb
 *
 */
public class MopubBidExt {

    // unmaped: nurls, data

    @Deprecated
    private AdmJson admjson; // Mopub OpenRTB 2.1

    /**
     * Recommended for native ads.  Pass the native ad json object directly into this field instead of serializing it in adm.
     * If using admnative do not pass adm (we will ignore)
     * Note: this is formerly ‘admjson’ in mopub native spec). 
     */
    private NativeAdResponseWrapper admnative;

    @Deprecated
    private String deal_id;// Mopub OpenRTB 2.1 - moved to standard OpenRTB location

    private List<String> imptrackers; // Mopub OpenRtb 2.3

    /**
     * Length of the video (if a video ad) in seconds
     */
    private Integer duration; // Mopub OpenRtb 2.3

    /**
     * This field can contain one of the following values depending on the type of creative: 
     * [{“VAST 2.0”,”VAST 3.0”, “MRAID 1.0”, “MRAID 2.0”, “Image Ad”, “HTML5”, “JS”,”native”}].
     */
    private String crtype; // Mopub OpenRtb 2.3

    @Deprecated
    private Video video;

    @Deprecated
    public String getDeal_id() {
        return deal_id;
    }

    @Deprecated
    public void setDeal_id(String deal_id) {
        this.deal_id = deal_id;
    }

    @Deprecated
    public AdmJson getAdmjson() {
        return admjson;
    }

    @Deprecated
    public void setAdmjson(AdmJson admjson) {
        this.admjson = admjson;
    }

    public Video getVideo() {
        return video;
    }

    public void setVideo(Video video) {
        this.video = video;
    }

    public String getCrtype() {
        return crtype;
    }

    public void setCrtype(String crtype) {
        this.crtype = crtype;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public List<String> getImptrackers() {
        return imptrackers;
    }

    public void setImptrackers(List<String> imptrackers) {
        this.imptrackers = imptrackers;
    }

    public NativeAdResponseWrapper getAdmnative() {
        return admnative;
    }

    public void setAdmnative(NativeAdResponseWrapper admnative) {
        this.admnative = admnative;
    }

    /**
     * Mopub OpenRTB 2.1
     * https://dev.twitter.com/mopub-demand/ad-formats/video-best-practices
     */
    @Deprecated
    public static class Video {

        private Integer duration;

        private String type;

        private Integer linearity;

        protected Video() {
            //marshalling
        }

        public Video(Integer duration) {
            this.type = "VAST 2.0";
            this.linearity = 1;
            this.duration = duration;
        }

        public Integer getDuration() {
            return duration;
        }

        public String getType() {
            return type;
        }

        public Integer getLinearity() {
            return linearity;
        }

    }
}
