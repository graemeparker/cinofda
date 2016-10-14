package com.adfonic.mopub;

import com.adfonic.util.CodedEnum;

/**
 * https://dev.twitter.com/mopub-demand/ad-formats/native-best-practices
 * 
 * Current bidders in MoPub’s native ads can map the data fields to the following asset object ids:
 * 1 = title
 * 2 = main image
 * 3 = icon
 * 4 = text
 * 5 = ctatext
 * 6 = starrating
 * 7 = VAST
 * 
 * The following assets are required when present in the bid request
 * 1 - Title
 * 2 - Main Image
 * 3 - Icon Image
 * 4 - Text
 * 
 * Mapping of native ad asset id <-> asset type, so we can check corresponding creative asset
 */
public enum MopubNativeAssetMap implements CodedEnum<Integer, MopubNativeAssetMap> {

    title(1), main_image(2), icon(3), text(4), ctatext(5), starrating(6);

    private Integer code;

    MopubNativeAssetMap(int code) {
        this.code = Integer.valueOf(code);
    }

    @Override
    public Integer getCode() {
        return code;
    }

    public static MopubNativeAssetMap valueOf(Integer code) {
        return CodedEnum.valueOf(code, MopubNativeAssetMap.class);
    }
}
