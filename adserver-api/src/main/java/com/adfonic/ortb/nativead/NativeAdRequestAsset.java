package com.adfonic.ortb.nativead;

import java.util.List;

import com.adfonic.util.CodedEnum;

/**
 * OpenRTB-Native-Ads-Specification-1_0-Final.pdf
 * 
 * 4.2 AssetObject
 *
 * !!! asset object may contain only one of title, img, data or video !!!
 */
public class NativeAdRequestAsset {

    /**
     * Unique asset ID, assigned by exchange. Typically a counter for the array.
     */
    private Integer id;

    /**
     * Set to 1 if asset is required (exchange will not accept a bid without it)
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

    /**
     * OpenRTB-Native-Ads-Specification-1_0-Final.pdf
     * 4.3 Title Object
     * 
     * The Title object is to be used for title element of the Native ad. 
     */
    public static class TitleAsset {
        /**
         * Maximum length of the text in the title element.
         */
        private Integer len;

        public Integer getLen() {
            return len;
        }

        public void setLen(Integer len) {
            this.len = len;
        }
    }

    /**
     * 7.4 Image Asset Types
     */
    public static enum ImageAssetType implements CodedEnum<Integer, ImageAssetType> {
        Icon(1), Logo(2), Main(3);

        private Integer code;

        private ImageAssetType(int code) {
            this.code = Integer.valueOf(code);
        }

        @Override
        public Integer getCode() {
            return code;
        }

        public static ImageAssetType valueOf(Integer code) {
            return CodedEnum.valueOf(code, ImageAssetType.class);
        }

    }

    /**
     * OpenRTB-Native-Ads-Specification-1_0-Final.pdf
     * 4.4 Image Object
     * 
     * The Image object to be used for all image elements of the Native ad such as Icons, Main Image, etc.
     * 
     */
    public static class ImageAsset {

        /**
         * Type ID of the image element supported by the publisher. 
         * The publisher can display this information in an appropriate format. 
         * See Table Image Asset Types for commonly used examples.
         * 
         *  @see ImageAssetType
         */
        private Integer type;

        /**
         * Width of the image in pixels. 
         */
        private Integer w;

        /**
         * The minimum requested width of the image in pixels. 
         * This option should be used for any rescaling of images by the client.
         * Either w or wmin should be transmitted. 
         * If only w is included, it should be considered an exact requirement.
         */
        private Integer wmin;

        /**
         * Height of the image in pixels. 
         */
        private Integer h;

        /**
         * The minimum requested height of the image in pixels. 
         * This option should be used for any rescaling of images by the client.
         *  Either h or hmin should be transmitted. 
         *  If only h is included, it should be considered an exact requirement.
         */
        private Integer hmin;

        /**
         * Whitelist of content MIME types supported. Popular MIME types include, but are not limited to “image/jpg” “image/gif”.
         * Each implementing Exchange should have their own list of supported types in the integration docs. 
         * See Wikipedia's MIME page for more information and links to all IETF RFCs.
         * If blank, assume all types are allowed.
         */
        private List<String> mimes;

        public Integer getType() {
            return type;
        }

        public void setType(Integer type) {
            this.type = type;
        }

        public Integer getW() {
            return w;
        }

        public void setW(Integer w) {
            this.w = w;
        }

        public Integer getWmin() {
            return wmin;
        }

        public void setWmin(Integer wmin) {
            this.wmin = wmin;
        }

        public Integer getH() {
            return h;
        }

        public void setH(Integer h) {
            this.h = h;
        }

        public Integer getHmin() {
            return hmin;
        }

        public void setHmin(Integer hmin) {
            this.hmin = hmin;
        }

        public List<String> getMimes() {
            return mimes;
        }

        public void setMimes(List<String> mimes) {
            this.mimes = mimes;
        }

    }

    /**
     * OpenRTB-Native-Ads-Specification-1_0-Final.pdf
     * 4.5 Video Object
     * 
     * The video object to be used for all video elements supported in the Native Ad. 
     * This corresponds to the Video object of OpenRTB 2.3. 
     * Exchange implementers can impose their own specific restrictions. 
     * Here are the required attributes of the Video Object. For optional attributes please refer to OpenRTB 2.3.
     * 
     */
    public static class VideoAsset {

        /**
         * Content MIME types supported. 
         * Popular MIME types include,but are not limited to “video/x-mswmv” for Windows Media, and “video/x-flv” for Flash Video.
         */
        private List<String> mimes;

        /**
         * Minimum video ad duration in seconds.
         */
        private Integer minduration;

        /**
         * Maximum video ad duration in seconds.
         */
        private Integer maxduration;

        /**
         * An array of video protocols the publisher can accept in the bid response. 
         * See OpenRTB 2.3 Table 5.8 Video Bid Response Protocols for a list of possible values.
         */
        private List<Integer> protocols;

        public List<String> getMimes() {
            return mimes;
        }

        public void setMimes(List<String> mimes) {
            this.mimes = mimes;
        }

        public Integer getMinduration() {
            return minduration;
        }

        public void setMinduration(Integer minduration) {
            this.minduration = minduration;
        }

        public Integer getMaxduration() {
            return maxduration;
        }

        public void setMaxduration(Integer maxduration) {
            this.maxduration = maxduration;
        }

        public List<Integer> getProtocols() {
            return protocols;
        }

        public void setProtocols(List<Integer> protocols) {
            this.protocols = protocols;
        }
    }

    /**
     * 7.3 Data Asset Types
     *
     */
    public static enum DataAssetType implements CodedEnum<Integer, DataAssetType> {
        sponsored(1), desc(2), rating(3), likes(4), downloads(5), price(6), saleprice(7), phone(8), address(9), desc2(10), displayurl(11), ctatext(12);

        private final Integer code;

        private DataAssetType(int code) {
            this.code = Integer.valueOf(code);
        }

        @Override
        public Integer getCode() {
            return code;
        }

        public static DataAssetType valueOf(Integer code) {
            return CodedEnum.valueOf(code, DataAssetType.class);
        }
    }

    /**
     * OpenRTB-Native-Ads-Specification-1_0-Final.pdf
     * 4.6 Data Object
     * 
     * The Data Object is to be used for all non-core elements of the native unit such as Ratings,
     * Review Count, Stars, Download count, descriptions etc. It is also generic for future of Native
     * elements not contemplated at the time of the writing of this document.
     * @author mvanek
     *
     */
    public static class DataAsset {

        /**
         * Type ID of the element supported by the publisher. 
         * The publisher can display this information in an appropriate format. 
         * See Table 7.3 Data Asset Types for commonly used examples.
         * 
         * @see DataAssetType 
         */
        private Integer type;

        /**
         * Maximum length of the text in the element’s response.
         */
        private Integer len;

        public Integer getType() {
            return type;
        }

        public void setType(Integer type) {
            this.type = type;
        }

        public Integer getLen() {
            return len;
        }

        public void setLen(Integer len) {
            this.len = len;
        }

    }
}
