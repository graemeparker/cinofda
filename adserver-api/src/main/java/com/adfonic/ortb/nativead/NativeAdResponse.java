package com.adfonic.ortb.nativead;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * OpenRTB-Native-Ads-Specification-1_0-Final.pdf
 * 5.2 Native Object
 * 
 * The native object is the top level JSON object which identifies a native response. The native
 * object has following attributes
 */
public class NativeAdResponse {

    /**
     * List of native ad’s assets.
     */
    private List<NativeAdResponseAsset> assets;

    /**
     * Destination Link. This is default link object for the ad. 
     * Individual assets can also have a link object which applies if the asset is activated (clicked). 
     * If the asset doesn’t have a link object, the parent link object applies. See LinkObject Definition
     */
    private NativeAdLink link;

    /**
     * Array of impression tracking URLs, expected to return a 1x1 image or 204 response - typically only passed when using 3rd party trackers.
     */
    private List<String> imptrackers;

    /**
     * Optional JavaScript impression tracker. This is a valid HTML,
     * Javascript is already wrapped in <script> tags. It should be
     * executed at impression time where it can be supported.
     */
    private String jstracker;

    NativeAdResponse() {
        // marshalling
    }

    public NativeAdResponse(NativeAdLink link, List<NativeAdResponseAsset> assets, List<String> imptrackers) {
        this.link = link;
        this.assets = assets;
        this.imptrackers = imptrackers;
    }

    public List<NativeAdResponseAsset> getAssets() {
        return assets;
    }

    public void setAssets(List<NativeAdResponseAsset> assets) {
        this.assets = assets;
    }

    public NativeAdLink getLink() {
        return link;
    }

    public void setLink(NativeAdLink link) {
        this.link = link;
    }

    public List<String> getImptrackers() {
        return imptrackers;
    }

    public void setImptrackers(List<String> imptrackers) {
        this.imptrackers = imptrackers;
    }

    public String getJstracker() {
        return jstracker;
    }

    public void setJstracker(String jstracker) {
        this.jstracker = jstracker;
    }

    /**
     * OpenRTB-Native-Ads-Specification-1_0-Final.pdf
     * 
     * 5.8 Link Object
     * Used for ‘call to action’ assets, or other links from the Native ad. 
     *
     */
    public static class NativeAdLink {
        /**
         * Landing URL of the clickable link.
         */
        private String url;

        /**
         * Fallback URL for deeplink. To be used if the URL given in url is not supported by the device.
         */
        private String fallback;

        /**
         * List of third-party tracker URLs to be fired on click of the URL.
         */
        private List<String> clicktrackers;

        NativeAdLink() {
            // marshalling
        }

        public NativeAdLink(String url) {
            this(url, null);
        }

        public NativeAdLink(String url, List<String> clicktrackers) {
            this.url = url;
            this.clicktrackers = clicktrackers;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getFallback() {
            return fallback;
        }

        public void setFallback(String fallback) {
            this.fallback = fallback;
        }

        public List<String> getClicktrackers() {
            return clicktrackers;
        }

        public void setClicktrackers(List<String> clicktrackers) {
            this.clicktrackers = clicktrackers;
        }

    }

    /**
     * 5.1 Native Ad Creative JSON
     * The JSON returned in adm or in response to nurl is a JSON string with the following attributes:
     */
    public static class NativeAdResponseWrapper {

        /**
         * Top level Native object
         */
        @JsonProperty("native")
        private NativeAdResponse _native;

        NativeAdResponseWrapper() {
            // mashalling
        }

        public NativeAdResponseWrapper(NativeAdResponse _native) {
            this._native = _native;
        }

        public NativeAdResponse getNative() {
            return _native;
        }

        public void setNative(NativeAdResponse _native) {
            this._native = _native;
        }

    }
}
