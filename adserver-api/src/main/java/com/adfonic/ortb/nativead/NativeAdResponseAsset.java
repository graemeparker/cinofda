package com.adfonic.ortb.nativead;

import com.adfonic.ortb.nativead.NativeAdResponse.NativeAdLink;

/**
 * OpenRTB-Native-Ads-Specification-1_0-Final.pdf
 * 5.3 Asset Object
 * 
 * Corresponds to the Asset Object in the request. The main container object for each asset
 * requested or supported by Exchange on behalf of the rendering client. Any object that is
 * required is to be flagged as such. Only one of the {title,img,video,data} objects should be
 * present in each object. All others should be null/absent. The id is to be unique within the
 * AssetObject array so that the response can be aligned. 
 * 
 */
public class NativeAdResponseAsset {

    /**
     * Unique asset ID, assigned by exchange, must match one of the asset IDs in request
     */
    private Integer id;

    /**
     * Set to 1 if asset is required. (bidder requires it to be displayed).
     */
    private Integer required;

    /**
     * Title object for title assets. See Title Object definition.
     */
    private TitleAsset title;

    /**
     * Image object for image assets. See Image Object definition
     */
    private ImageAsset img;

    /**
     * Video object for video assets. See the Video Object definition.
     * Note that in-stream video ads are not part of Native. 
     * Native ads may contain a video as the ad creative itself.
     */
    private VideoAsset video;

    /**
     * Data object for ratings, prices etc. See Data Object definition.
     */
    private DataAsset data;

    /**
     * Link object for call to actions. The link object applies if the asset item is activated (clicked).
     *  If there is no link object on the asset, the parent link object on the bid response applies.
     */
    private NativeAdLink link;

    NativeAdResponseAsset() {
        // marshalling
    }

    public NativeAdResponseAsset(Integer id, TitleAsset title) {
        this.id = id;
        this.title = title;
    }

    public NativeAdResponseAsset(Integer id, ImageAsset img) {
        this.id = id;
        this.img = img;
    }

    public NativeAdResponseAsset(Integer id, DataAsset data) {
        this.id = id;
        this.data = data;
    }

    public NativeAdResponseAsset(Integer id, VideoAsset video) {
        this.id = id;
        this.video = video;
    }

    /**
     * OpenRTB-Native-Ads-Specification-1_0-Final.pdf
     * 5.4 Title Object
     * 
     * Corresponds to the Title Object in the request, with the value filled in. 
     */
    public static class TitleAsset {
        /**
         * The text associated with the text element.
         */
        private String text;

        TitleAsset() {
            // marshall
        }

        public TitleAsset(String text) {
            this.text = text;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }
    }

    /**
     * OpenRTB-Native-Ads-Specification-1_0-Final.pdf
     * 5.5 Image Object
     * 
     * Corresponds to the Image Object in the request. The Image object to be used for all image
     * elements of the Native ad such as Icons, Main Image, etc.
     */
    public static class ImageAsset {
        /**
         * URL of the image asset.
         */
        private String url;

        private Integer w;

        private Integer h;

        ImageAsset() {
            // marshall
        }

        public ImageAsset(String url, Integer w, Integer h) {
            this.url = url;
            this.w = w;
            this.h = h;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String text) {
            this.url = text;
        }

        public Integer getW() {
            return w;
        }

        public void setW(Integer w) {
            this.w = w;
        }

        public Integer getH() {
            return h;
        }

        public void setH(Integer h) {
            this.h = h;
        }
    }

    /**
     * OpenRTB-Native-Ads-Specification-1_0-Final.pdf
     * 5.6 Data Object
     * 
     * Corresponds to the Data Object in the request, with the value filled in.
     */
    public static class DataAsset {

        /**
         * The optional formatted string name of the data type to be displayed. 
         */
        private String label;

        /**
         * The formatted string of data to be displayed. 
         * Can contain a formatted value such as “5 stars” or “$10” or “3.4 stars out of 5”.
         */
        private String value;

        DataAsset() {
            // marshall
        }

        public DataAsset(String value) {
            this.value = value;
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String text) {
            this.value = text;
        }

    }

    /**
     * OpenRTB-Native-Ads-Specification-1_0-Final.pdf
     * 5.7 Video Object
     * 
     * Corresponds to the Video Object in the request, yet containing a value of a conforming VAST tag as a value.
     */
    public static class VideoAsset {

        /**
         * VAST xml.
         */
        private String vasttag;

        public String getVasttag() {
            return vasttag;
        }

        public void setVasttag(String vasttag) {
            this.vasttag = vasttag;
        }

    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getRequired() {
        return required;
    }

    public void setRequired(Integer required) {
        this.required = required;
    }

    public TitleAsset getTitle() {
        return title;
    }

    public void setTitle(TitleAsset title) {
        this.title = title;
    }

    public ImageAsset getImg() {
        return img;
    }

    public void setImg(ImageAsset img) {
        this.img = img;
    }

    public VideoAsset getVideo() {
        return video;
    }

    public void setVideo(VideoAsset video) {
        this.video = video;
    }

    public DataAsset getData() {
        return data;
    }

    public void setData(DataAsset data) {
        this.data = data;
    }

    public NativeAdLink getLink() {
        return link;
    }

    public void setLink(NativeAdLink link) {
        this.link = link;
    }

}
