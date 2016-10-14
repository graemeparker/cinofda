package com.adfonic.adserver.rtb.open.v2;

import java.util.List;
import java.util.Set;

import com.adfonic.adserver.rtb.nativ.APIFramework;
import com.adfonic.util.CodedEnum;

/**
 * OpenRTB-API-Specification
 * 
 * 3.2.4 Object: Video
 */
public class VideoV2 {

    /**
     * Content MIME types supported. 
     * Popular MIME types may include “video/x-ms-wmv” for Windows Media and “video/x-flv” for Flash Video
     * 
     * string array; required
     */
    private List<String> mimes;

    /**
     * Minimum video ad duration in seconds.
     * 
     * integer; recommended
     */
    private Integer minduration;

    /**
     * Maximum video ad duration in seconds.
     * 
     * integer; recommended
     */
    private Integer maxduration;

    /**
     * NOTE: Use of protocols instead is highly recommended. 
     * Supported video bid response protocol. Refer to List 5.8. At least one supported protocol must be specified in either
     * the protocol or protocols attribute.
     * 
     * integer; DEPRECATED
     * 
     * @see VideoProtocol
     */
    @Deprecated
    private Integer protocol;

    /**
     * Array of supported video bid response protocols. 
     * Refer to List 5.8. At least one supported protocol must be specified in either the protocol or protocols attribute.
     * 
     * integer array; recommended
     * 
     * @see VideoProtocol
     */
    private Set<Integer> protocols;

    /**
     * Height of the video player in pixels.
     * 
     * integer; recommended
     */
    private Integer h;

    /**
     * Width of the video player in pixels.
     * 
     * integer; recommended
     */
    private Integer w;

    /**
     * Indicates the start delay in seconds for pre-roll, mid-roll, or 
     * post-roll ad placements. Refer to List 5.10 for additional generic values.
     * 
     * integer; recommended
     */
    private Integer startdelay;

    /**
     * Indicates if the impression must be linear, nonlinear, etc. 
     * If none specified, assume all are allowed. Refer to List 5.7.
     * 
     * @see VideoLinearity
     */
    private Integer linearity;

    /**
     * If multiple ad impressions are offered in the same bid request, 
     * the sequence number will allow for the coordinated delivery of multiple creatives.
     */
    private Integer sequence;

    /**
     * Blocked creative attributes. Refer to List 5.3
     * 
     * @see com.byyd.ortb.CreativeAttribute
     */
    private Set<Integer> battr;

    /**
     * Maximum extended video ad duration if extension is allowed.
     * If blank or 0, extension is not allowed. If -1, extension is
     * allowed, and there is no time limit imposed. If greater than 0,
     * then the value represents the number of seconds of extended
     * play supported beyond the maxduration value.
     */
    private Integer maxextended;

    /**
     * Minimum bit rate in Kbps. Exchange may set this dynamically
     * or universally across their set of publishers.
     */
    private Integer minbitrate;

    /**
     * Maximum bit rate in Kbps. Exchange may set this dynamically
     * or universally across their set of publishers.
     */
    private Integer maxbitrate;

    /**
     * Indicates if letter-boxing of 4:3 content into a 16:9 window is
     * allowed, where 0 = no, 1 = yes.
     */
    private Integer boxingallowed;

    /**
     * Allowed playback methods. If none specified, assume all are
     * allowed. Refer to List 5.9.
     * 
     * @see VideoPlayback
     */
    private List<Integer> playbackmethod;

    /**
     * Supported delivery methods (e.g., streaming, progressive). If
     * none specified, assume all are supported. Refer to List 5.13.
     * 
     *  @see VideoDelivery
     */
    private List<Integer> delivery;

    /**
     * Ad position on screen. Refer to List 5.4.
     */
    private Integer pos;

    /**
     * Array of Banner objects (Section 3.2.3) if companion ads are available.
     * 
     * @see VideoDelivery
     */
    private List<Banner> companionad;

    /**
     * List of supported API frameworks for this impression. Refer to
     * List 5.6. If an API is not explicitly listed, it is assumed not to be
     * supported.
     * 
     * @see APIFramework
     */
    private Set<APIFramework> api;

    /**
     * Supported VAST companion ad types. Refer to List 5.12.
     * Recommended if companion Banner objects are included via
     * the companionad array.
     * 
     * @see VideoCompanionType
     */
    private List<Integer> companiontype;

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

    public Integer getProtocol() {
        return protocol;
    }

    public void setProtocol(Integer protocol) {
        this.protocol = protocol;
    }

    public Set<Integer> getProtocols() {
        return protocols;
    }

    public void setProtocols(Set<Integer> protocols) {
        this.protocols = protocols;
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

    public Integer getStartdelay() {
        return startdelay;
    }

    public void setStartdelay(Integer startdelay) {
        this.startdelay = startdelay;
    }

    public Integer getLinearity() {
        return linearity;
    }

    public void setLinearity(Integer linearity) {
        this.linearity = linearity;
    }

    public Integer getSequence() {
        return sequence;
    }

    public void setSequence(Integer sequence) {
        this.sequence = sequence;
    }

    public Set<Integer> getBattr() {
        return battr;
    }

    public void setBattr(Set<Integer> battr) {
        this.battr = battr;
    }

    public Integer getMaxextended() {
        return maxextended;
    }

    public void setMaxextended(Integer maxextended) {
        this.maxextended = maxextended;
    }

    public Integer getMinbitrate() {
        return minbitrate;
    }

    public void setMinbitrate(Integer minbitrate) {
        this.minbitrate = minbitrate;
    }

    public Integer getMaxbitrate() {
        return maxbitrate;
    }

    public void setMaxbitrate(Integer maxbitrate) {
        this.maxbitrate = maxbitrate;
    }

    public Integer getBoxingallowed() {
        return boxingallowed;
    }

    public void setBoxingallowed(Integer boxingallowed) {
        this.boxingallowed = boxingallowed;
    }

    public List<Integer> getPlaybackmethod() {
        return playbackmethod;
    }

    public void setPlaybackmethod(List<Integer> playbackmethod) {
        this.playbackmethod = playbackmethod;
    }

    public List<Integer> getDelivery() {
        return delivery;
    }

    public void setDelivery(List<Integer> delivery) {
        this.delivery = delivery;
    }

    public Integer getPos() {
        return pos;
    }

    public void setPos(Integer pos) {
        this.pos = pos;
    }

    public List<Banner> getCompanionad() {
        return companionad;
    }

    public void setCompanionad(List<Banner> companionad) {
        this.companionad = companionad;
    }

    public Set<APIFramework> getApi() {
        return api;
    }

    public void setApi(Set<APIFramework> api) {
        this.api = api;
    }

    public List<Integer> getCompaniontype() {
        return companiontype;
    }

    public void setCompaniontype(List<Integer> companiontype) {
        this.companiontype = companiontype;
    }

    /**
     * 5.7 Video Linearity
     * 
     * The following table indicates the options for video linearity. “In-stream” or “linear” video refers to preroll,
     * post-roll, or mid-roll video ads where the user is forced to watch ad in order to see the video
     * content. “Overlay” or “non-linear” refer to ads that are shown on top of the video content.
     * This field is optional. The following is the interpretation of the bidder based upon the presence or
     * absence of the field in the bid request:
     * - If no value is set, any ad (linear or not) can be present in the response.
     * - If a value is set, only ads of the corresponding type can be present in the response.
     * Note to the reader: This OpenRTB table has values derived from the IAB Quality Assurance Guidelines
     * (QAG). Practitioners should keep in sync with updates to the QAG values as published on IAB.net.
     *
     */
    public static enum VideoLinearity implements CodedEnum<Integer, VideoLinearity> {

        LINEAR(VideoLinearity.LINEAR_CODE), // Linear / In-Stream
        NON_LINEAR(VideoLinearity.NON_LINEAR_CODE); //Non-Linear / Overlay

        public static final int LINEAR_CODE = 1;
        public static final int NON_LINEAR_CODE = 2;

        private final int code;

        private VideoLinearity(int code) {
            this.code = code;
        }

        @Override
        public Integer getCode() {
            return code;
        }
    }

    /**
     * 5.8 Video Bid Response Protocols
     * 
     * The following table lists the options for video bid response protocols that could be supported by an exchange.
     * 
     * http://www.iab.net/vast/
     */
    public static enum VideoProtocol implements CodedEnum<Integer, VideoProtocol> {

        VAST_1_0(VideoProtocol.VAST_1_0_CODE), //
        VAST_2_0(VideoProtocol.VAST_2_0_CODE), //
        VAST_3_0(VideoProtocol.VAST_3_0_CODE), //
        VAST_1_0_WRAPPER(VideoProtocol.VAST_1_0_WRAPPER_CODE), //
        VAST_2_0_WRAPPER(VideoProtocol.VAST_2_0_WRAPPER_CODE), //
        VAST_3_0_WRAPPER(VideoProtocol.VAST_3_0_WRAPPER_CODE);

        public static final int VAST_1_0_CODE = 1;
        public static final int VAST_2_0_CODE = 2;
        public static final int VAST_3_0_CODE = 3; // <VAST> <Ad> <InLine> ...
        public static final int VAST_1_0_WRAPPER_CODE = 4;
        public static final int VAST_2_0_WRAPPER_CODE = 5;
        public static final int VAST_3_0_WRAPPER_CODE = 6; //<VAST> <Ad> <Wrapper> ... <VASTAdTagURI>

        private final int code;

        private VideoProtocol(int code) {
            this.code = code;
        }

        @Override
        public Integer getCode() {
            return code;
        }

    }

    /**
     * 5.9 Video Playback Methods
     * 
     * The following table lists the various video playback methods.
     */
    public static enum VideoPlayback implements CodedEnum<Integer, VideoPlayback> {

        AUTO_PLAY_SOUND_ON(VideoPlayback.AUTO_PLAY_SOUND_ON_CODE), //Auto-Play Sound On
        AUTO_PLAY_SOUND_OFF(VideoPlayback.AUTO_PLAY_SOUND_OFF_CODE), //Auto-Play Sound Off
        CLICK_TO_PLAY(VideoPlayback.CLICK_TO_PLAY_CODE), //Click-to-Play
        MOUSE_OVER(VideoPlayback.MOUSE_OVER_CODE);//Mouse-Over

        public static final int AUTO_PLAY_SOUND_ON_CODE = 1;
        public static final int AUTO_PLAY_SOUND_OFF_CODE = 2;
        public static final int CLICK_TO_PLAY_CODE = 3;
        public static final int MOUSE_OVER_CODE = 4;

        private final int code;

        private VideoPlayback(int code) {
            this.code = code;
        }

        @Override
        public Integer getCode() {
            return code;
        }
    }

    /**
     * 5.13 Content Delivery Methods
     * 
     * The following table lists the various options for the delivery of video content.
     */
    public static enum VideoDelivery implements CodedEnum<Integer, VideoDelivery> {

        STREAMING(VideoDelivery.STREAMING_CODE), //Streaming
        PROGRESSIVE(VideoDelivery.PROGRESSIVE_CODE); //Progressive  

        public static final int STREAMING_CODE = 1;
        public static final int PROGRESSIVE_CODE = 2;

        private final int code;

        private VideoDelivery(int code) {
            this.code = code;
        }

        @Override
        public Integer getCode() {
            return code;
        }
    }

    /**
     * 5.12 VAST Companion Types
     * 
     * The following table lists the options to indicate markup types allowed for video companion ads. This
     * table is derived from IAB VAST 2.0+. Refer to www.iab.net/vast/ for more information.
     */
    public static enum VideoCompanionType implements CodedEnum<Integer, VideoCompanionType> {

        STATIC_RESOURCE(VideoCompanionType.STATIC_RESOURCE_CODE), //Static Resource
        HTML_RESOURCE(VideoCompanionType.HTML_RESOURCE_CODE), //HTML Resource
        IFRAME_RESOURCE(VideoCompanionType.IFRAME_RESOURCE_CODE); //iframe Resource

        public static final int STATIC_RESOURCE_CODE = 1;
        public static final int HTML_RESOURCE_CODE = 2;
        public static final int IFRAME_RESOURCE_CODE = 3;

        private final int code;

        private VideoCompanionType(int code) {
            this.code = code;
        }

        @Override
        public Integer getCode() {
            return code;
        }
    }

    /**
     * 5.10 Video Start Delay
     * 
     * The following table lists the various options for the video start delay. If the start delay value is greater
     * than 0, then the position is mid-roll and the value indicates the start delay.
     *
    public static enum VideoStartDelay implements CodedEnum<Integer, VideoStartDelay> {

    }
    */
}
