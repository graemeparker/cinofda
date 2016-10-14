package com.adfonic.ortb.nativead;

import java.util.List;

import com.adfonic.util.CodedEnum;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * https://github.com/openrtb/OpenRTB/blob/master/OpenRTB-Native-Ads-Specification-1_0-Final.pdf
 * 4.1 Native Markup Request Object
 * 
 * 
 * MoPub's implementation
 * https://dev.twitter.com/mopub-demand/ad-formats/native-best-practices
 * 
 * {"native":
 * {"assets":[
 * {"id":3,"img":{"hmin":80,"type":1,"wmin":80},"required":1},
 * {"id":2,"img":{"h":627,"type":3,"w":1200},"required":1},
 * {"data":{"len":100,"type":2},"id":4,"required":1},
 * {"id":1,"required":1,"title":{"len":25}}
 * ]}}
 * 
 * {"native":
 * {"assets":[
 * {"data":{"len":15,"type":12},"id":5,"required":0},
 * {"id":3,"img":{"hmin":80,"type":1,"wmin":80},"required":1},
 * {"id":2,"img":{"h":627,"type":3,"w":1200},"required":1},
 * {"data":{"len":100,"type":2},"id":4,"required":1},
 * {"id":1,"required":1,"title":{"len":25}}
 * ],
 * "layout":6}}
 * 
 * @author mvanek
 *
 */
public class NativeAdRequest {

    // unmapped: all attributes but assets and layout

    /**
     * An array of Asset Objects. Any bid must comply with the array of elements expressed by the Exchange.
     */
    private List<NativeAdRequestAsset> assets;

    /**
     * The Layout ID of the native ad unit. See the table of Native Layout IDs below.
     * @see NativeLayoutIDs
     */
    private Integer layout;

    public Integer getLayout() {
        return layout;
    }

    public void setLayout(Integer layout) {
        this.layout = layout;
    }

    public List<NativeAdRequestAsset> getAssets() {
        return assets;
    }

    public void setAssets(List<NativeAdRequestAsset> assets) {
        this.assets = assets;
    }

    public static class NativeAdRequestWrapper {

        @JsonProperty("native")
        private NativeAdRequest _native;

        public NativeAdRequest getNative() {
            return _native;
        }

        public void setNative(NativeAdRequest _native) {
            this._native = _native;
        }

    }

    public static enum NativeLayoutIDs implements CodedEnum<Integer, NativeLayoutIDs> {

        ContentWall(1), AppWall(2), NewsFeed(3), ChatList(4), Carousel(5), ContentStream(6), GridAdjoiningTheContent(7);

        private Integer code;

        private NativeLayoutIDs(int code) {
            this.code = Integer.valueOf(code);
        }

        @Override
        public Integer getCode() {
            return code;
        }

        public static NativeLayoutIDs valueOf(Integer code) {
            return CodedEnum.valueOf(code, NativeLayoutIDs.class);
        }
    }
}
